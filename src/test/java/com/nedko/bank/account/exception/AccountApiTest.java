package com.nedko.bank.account.exception;

import static com.nedko.bank.account.AccountEndpoints.ACCOUNTS;
import static com.nedko.bank.account.AccountEndpoints.DEPOSIT;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nedko.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@WebMvcTest
class AccountApiTest {

    private static final String ACCOUNT_ID = "00000000-0000-0000-0000-000000000001";

    private final MockMvc mockMvc;
    @MockitoBean
    private final AccountService service;

    @Test
    void rejectsNegativeDeposit() throws Exception {
        mockMvc.perform(post(DEPOSIT, ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void rejectsAccountCreationWithoutCurrency() throws Exception {
        mockMvc.perform(post(ACCOUNTS)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currency").exists());
    }

    @Test
    void preservesMultipleValidationMessagesForOneField() throws Exception {
        mockMvc.perform(post(ACCOUNTS)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"currency\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currency", hasSize(2)))
                .andExpect(jsonPath("$.errors.currency",
                    hasItems("must not be blank",
                        "must be a three-letter uppercase currency code")));
    }

    @Test
    void rejectsDepositWithoutAmount() throws Exception {
        mockMvc.perform(post(DEPOSIT, ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void rejectsNullDepositAmount() throws Exception {
        mockMvc.perform(post(DEPOSIT, ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }
}
