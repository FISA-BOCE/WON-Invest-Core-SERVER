package com.woorifisa.won_invest_core_server.global.util;

public class AccountMaskUtil {

    private AccountMaskUtil() {}

    /**
     * 12자리 계좌번호를 "123-***-***456" 형태로 마스킹합니다.
     * 앞 3자리, 뒤 3자리만 노출하고 중간 6자리를 마스킹합니다.
     */
    public static String mask(String accountNo) {
        return accountNo.substring(0, 3) + "-***-***" + accountNo.substring(9);
    }
}