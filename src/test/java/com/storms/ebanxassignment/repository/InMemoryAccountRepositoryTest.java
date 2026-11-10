package com.storms.ebanxassignment.repository;

import com.storms.ebanxassignment.domain.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAccountRepositoryTest {

    private final InMemoryAccountRepository repository = new InMemoryAccountRepository();

    @Test
    void returnsEmptyWhenAccountDoesNotExist() {
        assertThat(repository.findById("100")).isEmpty();
    }

    @Test
    void findsSavedAccount() {
        repository.save(new Account("100", new BigDecimal("10")));

        assertThat(repository.findById("100")).contains(new Account("100", new BigDecimal("10")));
    }

    @Test
    void saveOverwritesExistingBalance() {
        repository.save(new Account("100", new BigDecimal("10")));
        repository.save(new Account("100", new BigDecimal("25")));

        assertThat(repository.findById("100")).contains(new Account("100", new BigDecimal("25")));
    }

    @Test
    void deleteAllRemovesEveryAccount() {
        repository.save(new Account("100", new BigDecimal("10")));
        repository.save(new Account("300", new BigDecimal("15")));

        repository.deleteAll();

        assertThat(repository.findById("100")).isEmpty();
        assertThat(repository.findById("300")).isEmpty();
    }
}
