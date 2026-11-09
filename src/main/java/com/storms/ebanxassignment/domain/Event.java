package com.storms.ebanxassignment.domain;

import java.math.BigDecimal;

public sealed interface Event permits Deposit, Withdraw, Transfer {

    BigDecimal amount();
}
