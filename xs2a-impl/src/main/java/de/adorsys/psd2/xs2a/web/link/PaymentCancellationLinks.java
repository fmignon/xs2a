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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RJCT;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

public class PaymentCancellationLinks extends AbstractLinks {
    private ScaApproachResolver scaApproachResolver;
    private RedirectLinkBuilder redirectLinkBuilder;
    private boolean isExplicitMethod;

    public PaymentCancellationLinks(String httpUrl, ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder,
                                    CancelPaymentResponse response, boolean isExplicitMethod) {
        super(httpUrl);
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.isExplicitMethod = isExplicitMethod;

        buildCancellationLinks(response);
    }

    private void buildCancellationLinks(CancelPaymentResponse body) {
        if (RJCT == body.getTransactionStatus()) {
            return;
        }
        String paymentId = body.getPaymentId();
        String paymentService = body.getPaymentType().getValue();
        String paymentProduct = body.getPaymentProduct();

        setSelf(buildPath(UrlHolder.PAYMENT_LINK_URL, paymentService, paymentProduct, paymentId));
        setStatus(buildPath(UrlHolder.PAYMENT_STATUS_URL, paymentService, paymentProduct, paymentId));

        ScaApproach scaApproach = scaApproachResolver.resolveScaApproach();
        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(scaApproach)) {
            addEmbeddedDecoupledRelatedLinks(body);
        } else if (scaApproach == REDIRECT) {
            addRedirectRelatedLinks(body);
        } else if (scaApproach == OAUTH) {
            setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
    }

    private void addEmbeddedDecoupledRelatedLinks(CancelPaymentResponse body) {
        String paymentId = body.getPaymentId();
        String paymentService = body.getPaymentType().getValue();
        String paymentProduct = body.getPaymentProduct();
        String authorisationId = body.getAuthorizationId();

        if (isExplicitMethod) {
            if (body.getPsuData().isEmpty()) {
                setStartAuthorisationWithPsuIdentification(buildPath(UrlHolder.START_PIS_CANCELLATION_AUTH_URL, paymentService, paymentProduct, paymentId));
            } else {
                setStartAuthorisationWithPsuAuthentication(buildPath(UrlHolder.START_PIS_CANCELLATION_AUTH_URL, paymentService, paymentProduct, paymentId));
            }
        } else {
            setScaStatus(
                buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));

            if (body.getPsuData().isEmpty()) {
                setUpdatePsuIdentification(
                    buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
            } else {
                setUpdatePsuAuthentication(
                    buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
            }
        }
    }

    private void addRedirectRelatedLinks(CancelPaymentResponse body) {
        String paymentId = body.getPaymentId();
        String paymentService = body.getPaymentType().getValue();
        String paymentProduct = body.getPaymentProduct();
        String authorisationId = body.getAuthorizationId();

        if (isExplicitMethod) {
            setStartAuthorisation(buildPath(UrlHolder.START_PIS_CANCELLATION_AUTH_URL, paymentService, paymentProduct, paymentId));
        } else {
            String scaRedirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink(paymentId, authorisationId);
            setScaRedirect(scaRedirectLink);
            setScaStatus(
                buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
        }
    }
}
