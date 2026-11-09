package com.storms.ebanxassignment.repository;

import com.storms.ebanxassignment.domain.Account;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(balances.get(id))
                .map(balance -> new Account(id, balance));
    }

    @Override
    public Account save(Account account) {
        balances.put(account.id(), account.balance());
        return account;
    }
}
