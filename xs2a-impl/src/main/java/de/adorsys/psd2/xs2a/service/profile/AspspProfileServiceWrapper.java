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

package de.adorsys.psd2.xs2a.service.profile;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AspspProfileServiceWrapper {
    private final AspspProfileService aspspProfileService;

    /**
     * Gets a map with payment types and products allowed by current ASPSP from ASPSP profile service
     *
     * @return Map with payment types and  products supported by current ASPSP
     */
    public Map<PaymentType, Set<String>> getSupportedPaymentTypeAndProductMatrix() {
        return readAspspSettings().getSupportedPaymentTypeAndProductMatrix();
    }

    /**
     * Reads list of sca approaches from ASPSP profile service
     *
     * @return List of Available SCA approaches for tpp
     */
    public List<ScaApproach> getScaApproaches() {
        return aspspProfileService.getScaApproaches();
    }

    /**
     * Reads requirement of tpp signature from ASPSP profile service
     *
     * @return 'true' if tpp signature is required, 'false' if not
     */
    public Boolean getTppSignatureRequired() {
        return readAspspSettings().isTppSignatureRequired();
    }

    /**
     * Reads get PIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getPisRedirectUrlToAspsp() {
        return readAspspSettings().getPisRedirectUrlToAspsp();
    }

    /**
     * Reads get AIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getAisRedirectUrlToAspsp() {
        return readAspspSettings().getAisRedirectUrlToAspsp();
    }

    /**
     * Retrieves list of supported Xs2aAccountReference fields from ASPSP profile service
     *
     * @return List of supported fields
     */
    public List<SupportedAccountReferenceField> getSupportedAccountReferenceFields() {
        List<de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField> supportedAccountReferenceFields = readAspspSettings().getSupportedAccountReferenceFields();
        return supportedAccountReferenceFields.stream()
                   .map(reference -> SupportedAccountReferenceField.valueOf(reference.name()))
                   .collect(Collectors.toList());
    }

    /**
     * Reads value of maximum consent lifetime
     *
     * @return int value of maximum consent lifetime
     */
    public int getConsentLifetime() {
        return readAspspSettings().getConsentLifetime();
    }

    /**
     * Reads value of AllPsd2Support from ASPSP profile service
     *
     * @return true if ASPSP supports Global consents, false if doesn't
     */
    public Boolean getAllPsd2Support() {
        return readAspspSettings().isAllPsd2Support();
    }

    /**
     * Reads value BankOfferedConsentSupported
     *
     * @return boolean representation of support of Bank Offered Consent
     */
    public boolean isBankOfferedConsentSupported() {
        return readAspspSettings().isBankOfferedConsentSupport();
    }

    /**
     * Reads value of transactions without balances supported from ASPSP profile service
     *
     * @return true if ASPSP transactions without balances supported, false if doesn't
     */
    public boolean isTransactionsWithoutBalancesSupported() {
        return readAspspSettings().isTransactionsWithoutBalancesSupported();
    }

    /**
     * Reads if signing basket supported from ASPSP profile service
     *
     * @return true if ASPSP supports signing basket , false if doesn't
     */
    public boolean isSigningBasketSupported() {
        return readAspspSettings().isSigningBasketSupported();
    }

    /**
     * Reads if is payment cancellation authorization mandated from ASPSP profile service
     *
     * @return true if payment cancellation authorization is mandated, false if doesn't
     */
    public boolean isPaymentCancellationAuthorizationMandated() {
        return readAspspSettings().isPaymentCancellationAuthorizationMandated();
    }

    /**
     * Reads if piis consent is supported
     *
     * @return true if piis consent is supported, false if doesn't
     */
    public boolean isPiisConsentSupported() {
        return readAspspSettings().isPiisConsentSupported();
    }

    /**
     * Reads redirect url expiration time in milliseconds
     *
     * @return long value of redirect url expiration time
     */
    public long getRedirectUrlExpirationTimeMs() {
        return readAspspSettings().getRedirectUrlExpirationTimeMs();
    }

    /**
     * Reads get PIS payment cancellation redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getPisPaymentCancellationRedirectUrlToAspsp() {
        return readAspspSettings().getPisPaymentCancellationRedirectUrlToAspsp();
    }

    /**
     * Reads if available accounts for a consent are supported from ASPSP profile service
     *
     * @return true if ASPSP supports available accounts for consent
     */
    public boolean isAvailableAccountsConsentSupported() {
        return readAspspSettings().isAvailableAccountsConsentSupported();
    }

    /**
     * Reads if ASPSP requires usage of SCA to validate a one-time available accounts consent
     *
     * @return true if ASPSP requires usage of SCA to validate a one-time available accounts consent
     */
    public boolean isScaByOneTimeAvailableAccountsConsentRequired() {
        return readAspspSettings().isScaByOneTimeAvailableAccountsConsentRequired();
    }

    /**
     * Reads if ASPSP requires PSU in initial request for payment initiation or establishing consent
     *
     * @return true if ASPSP requires PSU in initial request for payment initiation or establishing consent
     */
    public boolean isPsuInInitialRequestMandated() {
        return readAspspSettings().isPsuInInitialRequestMandated();
    }

    /**
     * Reads if links shall be generated with the base URL set by `xs2aBaseUrl`
     *
     * @return true if ASPSP requires that links shall be generated with the base URL set by `xs2aBaseUrl` property
     */
    public boolean isForceXs2aBaseLinksUrl() {
        return readAspspSettings().isForceXs2aBaseUrl();
    }

    /**
     * Reads the url, which is used as base url for TPP Links in case when `forceXs2aBaseLinksUrl` property is set to "true"
     *
     * @return String value of the url
     */
    public String getXs2aBaseUrl() {
        return readAspspSettings().getXs2aBaseUrl();
    }

    /**
     * Reads whether a payment initiation service will be addressed in the same "session" or not
     *
     * @return true if a payment initiation service will be addressed in the same session
     */
    public boolean isCombinedServiceIndicator() {
        return readAspspSettings().isCombinedServiceIndicator();
    }

    /**
     * Reads if 'deltaList' parameter in transaction report is supported from ASPSP profile service
     *
     * @return true if ASPSP supports 'deltaList' parameter in transaction report
     */
    public boolean isDeltaListSupported() {
        return readAspspSettings().isDeltaListSupported();
    }

    /**
     * Reads if 'entryReferenceFrom' parameter in transaction report is supported from ASPSP profile service
     *
     * @return true if ASPSP supports 'entryReferenceFrom' parameter in transaction report
     */
    public boolean isEntryReferenceFromSupported() {
        return readAspspSettings().isEntryReferenceFromSupported();
    }

    /**
     * Retrieves a list of available booking statuses from the ASPSP profile service
     *
     * @return list of available booking statuses
     */
    public List<BookingStatus> getAvailableBookingStatuses() {
        return readAspspSettings().getAvailableBookingStatuses();
    }

    /**
     * Reads the mode of authorisation from the ASPSP profile service.
     *
     * @return String with the selected mode.
     */
    public StartAuthorisationMode getStartAuthorisationMode() {
        return readAspspSettings().getStartAuthorisationMode();
    }

    private AspspSettings readAspspSettings() {
        return aspspProfileService.getAspspSettings();
    }
}
