/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.PaymentCancellationLinks;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentCancellationAspectTest {
    private static final String HTTP_URL = "http://base.url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String ERROR_TEXT = "Error occurred while processing";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private CancelPaymentResponse response;

    @InjectMocks
    private PaymentCancellationAspect aspect;

    @Mock
    private MessageService messageService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

    private AspspSettings aspspSettings;
    private ResponseObject responseObject;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);

        when(cancellationScaNeededDecider.isScaRequired(true)).thenReturn(true);

        when(authorisationMethodDecider.isExplicitMethod(false, false)).thenReturn(false);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        response = new CancelPaymentResponse();
        response.setStartAuthorisationRequired(true);
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setPaymentId(PAYMENT_ID);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(PaymentType.SINGLE);
        response.setPsuData(PSU_DATA);
        response.setTransactionStatus(TransactionStatus.ACCP);
    }

    @Test
    public void cancelPayment_status_RJCT() {
        // Given
        response.setTransactionStatus(TransactionStatus.RJCT);

        responseObject = ResponseObject.<CancelPaymentResponse>builder()
                             .body(response)
                             .build();
        // When
        ResponseObject actualResponse = aspect.cancelPayment(responseObject, PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false);

        // Then
        assertFalse(actualResponse.hasError());
        assertEquals(actualResponse.getBody(), response);
    }

    @Test
    public void cancelPayment_success() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        PaymentCancellationLinks links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, false);

        responseObject = ResponseObject.<CancelPaymentResponse>builder()
                             .body(response)
                             .build();
        ResponseObject<CancelPaymentResponse>  actualResponse = aspect.cancelPayment(responseObject, PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false);

        verify(aspspProfileService, times(1)).getAspspSettings();

        assertFalse(actualResponse.hasError());
        assertEquals(actualResponse.getBody().getLinks(), links);
    }

    @Test
    public void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // Given
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        // When
        responseObject = ResponseObject.builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.cancelPayment(responseObject, PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }
}
