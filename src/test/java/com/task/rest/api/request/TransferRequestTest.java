package com.task.rest.api.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.math.BigDecimal;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class TransferRequestTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void testDeserializationFromJSON() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.00100000"));

        assertThat(MAPPER.readValue(fixture("fixtures/request/transfer-request.json"), TransferRequest.class))
                .isEqualToComparingFieldByField(request);
    }

}