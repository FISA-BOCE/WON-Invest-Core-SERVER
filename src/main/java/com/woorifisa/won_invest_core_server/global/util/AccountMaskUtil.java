package com.woorifisa.won_invest_core_server.global.util;

import java.util.Objects;

public class AccountMaskUtil {

    private AccountMaskUtil() {}

    public static String mask(String accountNo) {
        Objects.requireNonNull(accountNo, "accountNo must not be null");
        if (!accountNo.matches("\\d{12}")) {
            throw new IllegalArgumentException("accountNo must be exactly 12 digits, but was: " + accountNo);
        }
        return accountNo.substring(0, 3) + "-***-***" + accountNo.substring(9, 12);
    }
}