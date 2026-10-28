package com.nedko.bank.account.integration;

import static com.nedko.bank.account.AccountEndpoints.ACCOUNT_BY_ID;
import static com.nedko.bank.account.AccountEndpoints.ACCOUNTS;
import static com.nedko.bank.account.AccountEndpoints.DEPOSIT;
import static com.nedko.bank.account.AccountEndpoints.TRANSFER;
import static com.nedko.bank.TestCurrencies.GBP;
import static com.nedko.bank.TestCurrencies.INVALID_CURRENCY;
import static com.nedko.bank.TestCurrencies.USD;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.nedko.bank.TestProfileResolver;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles(resolver = TestProfileResolver.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor
@SpringBootTest
@Sql(statements = "DELETE FROM accounts", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@TestConstructor(autowireMode = AutowireMode.ALL)
class AccountIntegrationTest {

    private final MockMvc mockMvc;

    @Test
    void readsDepositsAndTransfersFundsThroughHttp() throws Exception {
        final UUID sourceId = createAccount("100.00", GBP);
        final UUID destinationId = createAccount("0.00", GBP);
        final double depositAmount = 25.5;
        final double transferAmount = 30.25;
        final double destinationBalanceAfterTransfer = 55.75;

        mockMvc.perform(get(ACCOUNT_BY_ID, sourceId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(sourceId.toString()))
                        .andExpect(jsonPath("$.balance").value(100.0))
                        .andExpect(jsonPath("$.currency").value(GBP));

        mockMvc.perform(post(DEPOSIT, destinationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": %.2f}
                                """.formatted(depositAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(destinationId.toString()))
                .andExpect(jsonPath("$.balance").value(depositAmount))
                .andExpect(jsonPath("$.currency").value(GBP));

        mockMvc.perform(post(TRANSFER, sourceId, destinationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": %.2f}
                                """.formatted(transferAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(transferAmount))
                .andExpect(jsonPath("$.from.id").value(sourceId.toString()))
                .andExpect(jsonPath("$.from.balance").value(69.75))
                .andExpect(jsonPath("$.to.id").value(destinationId.toString()))
                .andExpect(jsonPath("$.to.balance").value(destinationBalanceAfterTransfer));

        mockMvc.perform(get(ACCOUNT_BY_ID, destinationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(destinationBalanceAfterTransfer));
    }

    @Test
    void rejectsInsufficientFundsAndLeavesBothAccountsUnchanged() throws Exception {
        final UUID sourceId = createAccount("11.00", GBP);
        final UUID destinationId = createAccount("4.00", GBP);
        final double transferAmount = 11.01;

        mockMvc.perform(post(TRANSFER, sourceId, destinationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": %.2f}
                                """.formatted(transferAmount)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("Insufficient funds")));

        mockMvc.perform(get(ACCOUNT_BY_ID, sourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(11.0));
        mockMvc.perform(get(ACCOUNT_BY_ID, destinationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(4.0));
    }

    @Test
    void rejectsTransfersBetweenDifferentCurrencies() throws Exception {
        final UUID poundAccountId = createAccount("12.00", GBP);
        final UUID dollarAccountId = createAccount("1.00", USD);

        mockMvc.perform(post(TRANSFER, poundAccountId, dollarAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 2.00}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("currencies")));
    }

    @Test
    void returnsValidationErrorsForInvalidRequests() throws Exception {
        mockMvc.perform(post(ACCOUNTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"initialBalance": -1.00, "currency": "%s"}
                                """.formatted(INVALID_CURRENCY)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request validation failed."))
                .andExpect(jsonPath("$.errors.initialBalance").exists())
                .andExpect(jsonPath("$.errors.currency").exists());
    }

    @Test
    void returnsNotFoundForUnknownAccount() throws Exception {
        mockMvc.perform(get(ACCOUNT_BY_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("Account")));
    }

    private UUID createAccount(final String initialBalance, final String currency) throws Exception {
        final MvcResult result = mockMvc.perform(post(ACCOUNTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"initialBalance": %s, "currency": "%s"}
                                """.formatted(initialBalance, currency)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }
}
