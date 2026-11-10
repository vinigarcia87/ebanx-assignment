package com.storms.ebanxassignment.service;

import com.storms.ebanxassignment.domain.Account;
import com.storms.ebanxassignment.exception.AccountNotFoundException;
import com.storms.ebanxassignment.domain.Deposit;
import com.storms.ebanxassignment.domain.Event;
import com.storms.ebanxassignment.domain.EventResult;
import com.storms.ebanxassignment.domain.Transfer;
import com.storms.ebanxassignment.domain.Withdraw;
import com.storms.ebanxassignment.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;

    public EventResult event(Event event) {
        return switch (event) {
            case Deposit deposit -> deposit(deposit);
            case Withdraw withdraw -> withdraw(withdraw);
            case Transfer transfer -> transfer(transfer);
        };
    }

    public EventResult deposit(Deposit deposit) {
        Account account = accountRepository.findById(deposit.destination())
                .orElseGet(() -> Account.empty(deposit.destination()));

        Account updated = accountRepository.save(account.deposit(deposit.amount()));
        return EventResult.withDestination(updated);
    }

    public EventResult withdraw(Withdraw withdraw) {
        Account account = accountRepository.findById(withdraw.origin())
                .orElseThrow(() -> new AccountNotFoundException(withdraw.origin()));

        Account updated = accountRepository.save(account.withdraw(withdraw.amount()));
        return EventResult.withOrigin(updated);
    }

    public EventResult transfer(Transfer transfer) {
        throw new UnsupportedOperationException("Transfer not implemented yet");
    }
}
