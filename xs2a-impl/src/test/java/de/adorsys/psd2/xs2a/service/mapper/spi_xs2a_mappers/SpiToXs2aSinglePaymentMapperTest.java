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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = {SpiToXs2aSinglePaymentMapperImpl.class, SpiToXs2aAmountMapperImpl.class,
        SpiToXs2aAddressMapperImpl.class, SpiToXs2aAccountReferenceMapperImpl.class})
public class SpiToXs2aSinglePaymentMapperTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGzsJuT-jYHNOMR1D7n69vIF46RgFd7Zn_=_bS6p6XvTWI";
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();

    @Autowired
    private SpiToXs2aSinglePaymentMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapToXs2aSinglePayment() {
        SinglePayment singlePayment = mapper.mapToXs2aSinglePayment(buildSpiSinglePayment());

        SinglePayment expectedSinglePayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-single-payment.json", SinglePayment.class);
        expectedSinglePayment.setRequestedExecutionDate(OFFSET_DATE_TIME.toLocalDate());
        expectedSinglePayment.setRequestedExecutionTime(OFFSET_DATE_TIME);
        expectedSinglePayment.setStatusChangeTimestamp(OFFSET_DATE_TIME);
        assertEquals(expectedSinglePayment, singlePayment);
    }

    @Test
    public void mapToXs2aSinglePaymentList() {
        List<SinglePayment> singlePayments = mapper.mapToXs2aSinglePaymentList(Collections.singletonList(buildSpiSinglePayment()));

        SinglePayment expectedSinglePayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-single-payment.json", SinglePayment.class);
        expectedSinglePayment.setRequestedExecutionDate(OFFSET_DATE_TIME.toLocalDate());
        expectedSinglePayment.setRequestedExecutionTime(OFFSET_DATE_TIME);
        expectedSinglePayment.setStatusChangeTimestamp(OFFSET_DATE_TIME);

        assertEquals(1, singlePayments.size());
        assertEquals(expectedSinglePayment, singlePayments.get(0));
    }

    @Test
    public void mapToXs2aSinglePaymentList_nullValue() {
        List<SinglePayment> singlePayments = mapper.mapToXs2aSinglePaymentList(null);
        assertTrue(singlePayments.isEmpty());

        singlePayments = mapper.mapToXs2aSinglePaymentList(Collections.emptyList());
        assertTrue(singlePayments.isEmpty());
    }

    @Test
    public void mapToXs2aSinglePayment_nullValue() {
        SinglePayment singlePayment = mapper.mapToXs2aSinglePayment(null);
        assertNull(singlePayment);
    }

    private SpiSinglePayment buildSpiSinglePayment() {
        SpiSinglePayment payment = new SpiSinglePayment(PAYMENT_PRODUCT);
        payment.setPaymentId(PAYMENT_ID);
        payment.setPaymentStatus(TransactionStatus.ACCP);
        SpiAccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        payment.setCreditorAccount(accountReference);
        payment.setCreditorAgent("BCENECEQ");
        payment.setCreditorName("Telekom");
        payment.setCreditorAddress(jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-address.json", SpiAddress.class));
        payment.setDebtorAccount(accountReference);
        payment.setEndToEndIdentification("RI-123456789");
        payment.setInstructedAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal("1000.00")));
        payment.setRemittanceInformationUnstructured("Ref. Number TELEKOM-1222");
        payment.setRequestedExecutionDate(OFFSET_DATE_TIME.toLocalDate());
        payment.setRequestedExecutionTime(OFFSET_DATE_TIME);
        payment.setPsuDataList(Collections.singletonList(new SpiPsuData("psuId", "", "", "")));
        payment.setStatusChangeTimestamp(OFFSET_DATE_TIME);
        return payment;
    }
}
