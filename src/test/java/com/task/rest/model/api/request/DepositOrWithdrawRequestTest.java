package com.task.rest.model.api.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.math.BigDecimal;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class DepositOrWithdrawRequestTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void testDeserializationFromJSON() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, new BigDecimal("100.00100000"));

        assertThat(MAPPER.readValue(fixture("fixtures/request/deposit-or-withdraw-request.json"), DepositOrWithdrawRequest.class))
                .isEqualToComparingFieldByField(request);
    }

}