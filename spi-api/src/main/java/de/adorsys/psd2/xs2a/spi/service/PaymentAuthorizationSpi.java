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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PaymentAuthorizationSpi extends AuthorisationSpi {
    @Override
    default SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiPsuData psuData, String password,
                                                             AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiAuthorisationStatus>builder()
            .fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(@NotNull SpiPsuData psuData,
                                                                       AspspConsentData aspspConsentData) {
        return SpiResponse.<List<SpiScaMethod>>builder()
            .fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    @NotNull
    default SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiPsuData psuData,
                                                                             @NotNull SpiScaMethod scaMethod,
                                                                             @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiAuthorizationCodeResult>builder()
            .fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    @NotNull
    default SpiResponse<SpiResponse.VoidResponse> verifyAuthorisationCode(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiResponse.VoidResponse>builder()
            .fail(SpiResponseStatus.NOT_SUPPORTED);
    }
}
