package com.storms.ebanxassignment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void balanceOfNonExistingAccountReturnsNotFound() throws Exception {
        mockMvc.perform(get("/balance").param("account_id", "1234"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("0"));
    }

    @Test
    void depositCreatesAccountWithInitialBalance() throws Exception {
        postEvent("""
                {"type":"deposit", "destination":"100", "amount":10}
                """, """
                {"destination": {"id":"100", "balance":10}}
                """);
    }

    @Test
    void depositIntoExistingAccountAddsToBalance() throws Exception {
        postEvent("""
                {"type":"deposit", "destination":"100", "amount":10}
                """, """
                {"destination": {"id":"100", "balance":10}}
                """);

        postEvent("""
                {"type":"deposit", "destination":"100", "amount":10}
                """, """
                {"destination": {"id":"100", "balance":20}}
                """);
    }

    @Test
    void balanceOfExistingAccountReturnsCurrentBalance() throws Exception {
        postEvent("""
                {"type":"deposit", "destination":"100", "amount":10}
                """, """
                {"destination": {"id":"100", "balance":10}}
                """);

        postEvent("""
                {"type":"deposit", "destination":"100", "amount":10}
                """, """
                {"destination": {"id":"100", "balance":20}}
                """);

        mockMvc.perform(get("/balance").param("account_id", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("20"));
    }

    @Test
    void withdrawFromNonExistingAccountReturnsNotFound() throws Exception {
        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"withdraw", "origin":"200", "amount":10}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("0"));
    }

    @Test
    void withdrawFromExistingAccountSubtractsFromBalance() throws Exception {
        postEvent("""
                {"type":"deposit", "destination":"100", "amount":10}
                """, """
                {"destination": {"id":"100", "balance":10}}
                """);

        postEvent("""
                {"type":"deposit", "destination":"100", "amount":10}
                """, """
                {"destination": {"id":"100", "balance":20}}
                """);

        postEvent("""
                {"type":"withdraw", "origin":"100", "amount":5}
                """, """
                {"origin": {"id":"100", "balance":15}}
                """);
    }

    @Test
    void transferFromExistingAccountMovesBalance() throws Exception {
        postEvent("""
                {"type":"deposit", "destination":"100", "amount":15}
                """, """
                {"destination": {"id":"100", "balance":15}}
                """);

        postEvent("""
                {"type":"transfer", "origin":"100", "amount":15, "destination":"300"}
                """, """
                {"origin": {"id":"100", "balance":0}, "destination": {"id":"300", "balance":15}}
                """);
    }

    @Test
    void transferFromNonExistingAccountReturnsNotFound() throws Exception {
        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"transfer", "origin":"200", "amount":15, "destination":"300"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("0"));
    }

    private void postEvent(String request, String expectedResponse) throws Exception {
        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }
}
