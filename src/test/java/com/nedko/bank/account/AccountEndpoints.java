package com.nedko.bank.account;

public final class AccountEndpoints {
    
    public static final String ACCOUNTS = "/accounts";
    public static final String ACCOUNT_BY_ID = "/accounts/{id}";
    public static final String DEPOSIT = "/accounts/{id}/deposit";
    public static final String TRANSFER = "/accounts/{id}/transfer/{toId}";

    private AccountEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }
}
