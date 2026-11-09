package com.storms.ebanxassignment.service;

import com.storms.ebanxassignment.domain.Deposit;
import com.storms.ebanxassignment.domain.Event;
import com.storms.ebanxassignment.domain.Transfer;
import com.storms.ebanxassignment.domain.Withdraw;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    public void event(Event event) {
        switch (event) {
            case Deposit deposit -> deposit(deposit);
            case Withdraw withdraw -> withdraw(withdraw);
            case Transfer transfer -> transfer(transfer);
        }
    }

    public void deposit(Deposit deposit) {
    }

    public void withdraw(Withdraw withdraw) {
    }

    public void transfer(Transfer transfer) {
    }
}
