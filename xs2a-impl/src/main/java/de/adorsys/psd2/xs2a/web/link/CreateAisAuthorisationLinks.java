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

import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;

public class CreateAisAuthorisationLinks extends AbstractLinks {

    public CreateAisAuthorisationLinks(String httpUrl, CreateConsentAuthorizationResponse response,
                                       ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder) {
        super(httpUrl);

        String consentId = response.getConsentId();
        String authorisationId = response.getAuthorisationId();

        setScaStatus(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorisationId));

        if (scaApproachResolver.getInitiationScaApproach(authorisationId) == REDIRECT) {
            setScaRedirect(redirectLinkBuilder.buildConsentScaRedirectLink(consentId, authorisationId));
        } else {
            setUpdatePsuAuthentication(buildPath(UrlHolder.AIS_AUTHORISATION_URL, consentId, authorisationId));
        }
    }
}
