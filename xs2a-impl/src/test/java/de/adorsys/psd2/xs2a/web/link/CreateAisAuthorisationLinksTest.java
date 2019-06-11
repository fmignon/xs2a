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
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateAisAuthorisationLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String CONSENT_ID = "9mp1PaotpXSToNCi";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

    private CreateAisAuthorisationLinks links;
    private CreateConsentAuthorizationResponse response;

    private Links expectedLinks;

    @Before
    public void setUp() {
        expectedLinks = new Links();

        response = new CreateConsentAuthorizationResponse();
        response.setConsentId(CONSENT_ID);
        response.setAuthorisationId(AUTHORISATION_ID);
    }

    @Test
    public void isScaStatusMethodAuthenticated_redirectScaApproach() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(eq(CONSENT_ID), eq(AUTHORISATION_ID))).thenReturn(REDIRECT_LINK);

        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder);

        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setScaRedirect(REDIRECT_LINK);
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticated_embeddedScaApproach() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder);

        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticated_decoupledScaApproach() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder);

        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticated_oauthScaApproach() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.OAUTH);

        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder);

        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }
}
