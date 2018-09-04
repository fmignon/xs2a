/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.consent.AisConsentMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateAisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus.RECEIVED;

@Service
@RequiredArgsConstructor
public class ConsentService { //TODO change format of consentRequest to mandatory obtain PSU-Id and only return data which belongs to certain PSU tobe changed upon v1.1
    private final AisConsentMapper aisConsentMapper;
    private final ConsentSpi consentSpi;
    private final ConsentMapper consentMapper;
    private final AisAuthorizationService authorizationService;
    private final AisConsentService aisConsentService;
    private final AspspProfileService aspspProfileService;

    /**
     * @param request body of create consent request carrying such parameters as AccountAccess, validity terms etc.
     * @param psuId   String representing PSU identification at ASPSP
     * @return CreateConsentResponse representing the complete response to create consent request
     * Performs create consent operation either by filling the appropriate AccountAccess fields with corresponding
     * account details or by getting account details from ASPSP by psuId and filling the appropriate fields in
     * AccountAccess determined by availableAccounts or allPsd2 variables
     */
    public ResponseObject<CreateConsentResponse> createAccountConsentsWithResponse(CreateConsentReq request, String psuId) {
        if (isInvalidBankOfferConsent(request)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PARAMETER_NOT_SUPPORTED))).build();
        }
        if (!isValidExpirationDate(request.getValidUntil())) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PERIOD_INVALID))).build();
        }

        if (isNotSupportedGlobalConsentForAllPsd2(request)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PARAMETER_NOT_SUPPORTED))).build();
        }
ddddddddddddddddddddddddddddddddddddddddddd
        CreateConsentReq checkedRequest = new CreateConsentReq();
        checkedRequest.setAccess(request.getAccess());
        checkedRequest.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        checkedRequest.setRecurringIndicator(request.isRecurringIndicator());
        checkedRequest.setFrequencyPerDay(request.getFrequencyPerDay());
        checkedRequest.setValidUntil(request.getValidUntil());

        String tppId = "This is a test TppId"; //TODO v1.1 add corresponding request header
        SpiCreateAisConsentRequest createAisConsentRequest = aisConsentMapper.mapToSpiCreateAisConsentRequest(request, psuId, tppId, new AspspConsentData("zzzzzzzzzzzzzz".getBytes()));
        String consentId = consentSpi.createConsent(createAisConsentRequest);

        dddddddddddddsdsdsdssssssssssssssssss
        String consentId = isNotEmptyAccess(checkedRequest.getAccess())
                               ? aisConsentService.createConsent(checkedRequest, psuId, tppId)
                               : null;


        //TODO v1.1 Add balances support
        //TODO v1.2 Add embedded approach specfic links
        return !StringUtils.isBlank(consentId)
                   ? ResponseObject.<CreateConsentResponse>builder().body(new CreateConsentResponse(RECEIVED.getValue(), consentId, null, null)).build()
                   : ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_400))).build();
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return ConsentStatus
     * Returns status of requested consent
     */
    public ResponseObject<ConsentStatusResponse> getAccountConsentsStatusById(String consentId) {
        return aisConsentMapper.mapToConsentStatus(consentSpi.getAccountConsentStatusById(consentId))
                   .map(status -> ResponseObject.<ConsentStatusResponse>builder().body(new ConsentStatusResponse(status)).build())
                   .orElseGet(() -> ResponseObject.<ConsentStatusResponse>builder()
                                        .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400)))
                                        .build());
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return VOID
     * Revokes account consent on PSU request
     */
    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        if (consentSpi.getAccountConsentById(consentId) != null) {
            consentSpi.revokeConsent(consentId);
            return ResponseObject.<Void>builder().build();
        }

        return ResponseObject.<Void>builder()
                   .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build();
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return AccountConsent requested by consentId
     */
    public ResponseObject<AccountConsent> getAccountConsentById(String consentId) {
        AccountConsent consent = aisConsentMapper.mapToAccountConsent(consentSpi.getAccountConsentById(consentId));
        return consent == null
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    ResponseObject<AccountAccess> getValidatedConsent(String consentId) {
        AccountConsent consent = aisConsentMapper.mapToAccountConsent(consentSpi.getAccountConsentById(consentId));
        if (consent == null) {
            return ResponseObject.<AccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build();
        }
        if (!consent.isValidStatus()) {
            return ResponseObject.<AccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_EXPIRED))).build();
        }
        if (!consent.isValidFrequency()) {
            return ResponseObject.<AccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.ACCESS_EXCEEDED))).build();
        }
        return ResponseObject.<AccountAccess>builder().body(consent.getAccess()).build();
    }

    boolean isValidAccountByAccess(String iban, Currency currency, List<AccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> a.getIban().equals(iban)
                                             && a.getCurrency() == currency);
    }

    private boolean isValidExpirationDate(LocalDate validUntil) {
        int consentLifetime = Math.abs(aspspProfileService.getConsentLifetime());
        return validUntil.isAfter(LocalDate.now()) && isValidConsentLifetime(consentLifetime, validUntil);
    }

    private boolean isValidConsentLifetime(int consentLifetime, LocalDate validUntil) {
        return consentLifetime == 0 || validUntil.isBefore(LocalDate.now().plusDays(consentLifetime));
    }

    private Boolean isNotEmptyAccess(AccountAccess access) {
        return Optional.ofNullable(access)
                   .map(AccountAccess::isNotEmpty)
                   .orElse(false);
    }

    private boolean isNotSupportedGlobalConsentForAllPsd2(CreateConsentReq request) {
        return request.getAccess().getAllPsd2() == AccountAccessType.ALL_ACCOUNTS &&
                   CollectionUtils.isEmpty(request.getAccountReferences())
                   && !aspspProfileService.getAllPsd2Support();
    }

    public ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(String psuId, String consentId) {
        return authorizationService.createConsentAuthorization(psuId, consentId)
                   .map(resp -> ResponseObject.<CreateConsentAuthorizationResponse>builder().body(resp).build())
                   .orElseGet(() -> ResponseObject.<CreateConsentAuthorizationResponse>builder().fail(new MessageError(MessageErrorCode.CONSENT_UNKNOWN_400)).build());
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData) {
        return Optional.ofNullable(aisConsentService.getAccountConsentAuthorizationById(updatePsuData.getAuthorizationId()))
                   .map(consentAuthorization ->
                            getUpdateConsentPsuDataResponse(updatePsuData, consentAuthorization))
                   .orElseGet(() -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                        .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_404))
                                        .build());
    }

    private ResponseObject<UpdateConsentPsuDataResponse> getUpdateConsentPsuDataResponse(UpdateConsentPsuDataReq updatePsuData, SpiAccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = aisConsentService.updateConsentAuthorization(updatePsuData, consentAuthorization);
        return Optional.ofNullable(response)
                   .map(s -> ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response).build())
                   .orElseGet(() -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                        .fail(new MessageError(MessageErrorCode.FORMAT_ERROR))
                                        .build());
    }

    private boolean isInvalidBankOfferConsent(CreateConsentReq request) {
        return !aspspProfileService.isBankOfferedConsentSupported()
                   && !isNotEmptyAccess(request.getAccess());
    }
}
