package com.storms.ebanxassignment.repository;

import com.storms.ebanxassignment.domain.Account;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findById(String id);

    Account save(Account account);

    void deleteAll();
}
