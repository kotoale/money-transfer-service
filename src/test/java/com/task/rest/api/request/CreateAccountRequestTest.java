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
public class CreateAccountRequestTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void testDeserializationFromJSON() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(new BigDecimal("100.00100000"));

        assertThat(MAPPER.readValue(fixture("fixtures/request/create-account-request.json"), CreateAccountRequest.class))
                .isEqualToComparingFieldByField(request);
    }

}