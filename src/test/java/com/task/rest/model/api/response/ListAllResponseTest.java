package com.task.rest.model.api.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.rest.model.dbo.Account;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class ListAllResponseTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void testSerializationToJSON() throws Exception {
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account(1L, new BigDecimal("100.10000000")));
        accounts.add(new Account(2L, new BigDecimal("100.00100000")));
        ListAllResponse response = new ListAllResponse(accounts);

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/response/list-all-response.json"), ListAllResponse.class));

        assertThat(MAPPER.writeValueAsString(response)).isEqualTo(expected);
    }

}