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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.AccountApi;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.service.AccountService;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unchecked") // This class implements autogenerated interface without proper return values generated
@RestController
@AllArgsConstructor
@Api(value = "v1", description = "Provides access to the account information", tags = {"Account Information Service (AIS)"})
public class AccountController implements AccountApi {

    private final HttpServletRequest request;
    private final AccountService accountService;
    private final ResponseMapper responseMapper;
    private final AccountModelMapper accountModelMapper;
    private final ResponseErrorMapper responseErrorMapper;

    @Override
    public ResponseEntity getAccountList(UUID xRequestID, String consentID, Boolean withBalance, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aAccountListHolder> accountList = accountService.getAccountList(consentID, Optional.ofNullable(withBalance).orElse(false));
        return accountList.hasError()
                   ? responseErrorMapper.generateErrorResponse(accountList.getError())
                   : responseMapper.ok(accountList, accountModelMapper::mapToAccountList);
    }

    @Override
    public ResponseEntity readAccountDetails(String accountId, UUID xRequestID, String consentID, Boolean withBalance, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aAccountDetails> accountDetails = accountService.getAccountDetails(consentID, accountId, Optional.ofNullable(withBalance).orElse(false));
        return accountDetails.hasError()
                   ? responseErrorMapper.generateErrorResponse(accountDetails.getError())
                   : responseMapper.ok(accountDetails, accountModelMapper::mapToAccountDetails);
    }

    @Override
    public ResponseEntity getBalances(String accountId, UUID xRequestID, String consentID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aBalancesReport> balancesReport = accountService.getBalancesReport(consentID, accountId);
        return balancesReport.hasError()
                   ? responseErrorMapper.generateErrorResponse(balancesReport.getError())
                   : responseMapper.ok(balancesReport, accountModelMapper::mapToBalance);
    }

    @Override
    public ResponseEntity getTransactionList(String accountId, String bookingStatus, UUID xRequestID, String consentID, LocalDate dateFrom, LocalDate dateTo, String entryReferenceFrom, Boolean deltaList, Boolean withBalance, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aTransactionsReport> transactionsReport =
            accountService.getTransactionsReportByPeriod(consentID, accountId, request.getHeader("accept"), BooleanUtils.isTrue(withBalance), dateFrom, dateTo, BookingStatus.forValue(bookingStatus));

        if (transactionsReport.hasError()) {
            return responseErrorMapper.generateErrorResponse(transactionsReport.getError());
        } else if (transactionsReport.getBody().isResponseContentTypeJson()) {
            return responseMapper.ok(transactionsReport, accountModelMapper::mapToTransactionsResponse200Json);
        } else {
            return responseMapper.ok(transactionsReport, accountModelMapper::mapToTransactionsResponseRaw);
        }
    }

    @Override
    public ResponseEntity getTransactionDetails(String accountId, String resourceId, UUID xRequestID, String consentID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Transactions> transactionDetails = accountService.getTransactionDetails(consentID, accountId, resourceId);
        return transactionDetails.hasError()
                   ? responseErrorMapper.generateErrorResponse(transactionDetails.getError())
                   : responseMapper.ok(transactionDetails, accountModelMapper::mapToTransactionDetails);

    }
}
