package com.task.rest.api.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.math.BigDecimal;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CrudAccountResponseTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void testSerializationToJSON() throws Exception {
        CrudAccountResponse response = new CrudAccountResponse(1L, new BigDecimal("100.00100000"), OperationStatus.UPDATED);

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/response/crud-account-response.json"), CrudAccountResponse.class));

        assertThat(MAPPER.writeValueAsString(response)).isEqualTo(expected);
    }

}