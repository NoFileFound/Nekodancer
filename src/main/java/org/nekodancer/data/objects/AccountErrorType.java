package org.nekodancer.data.objects;

public enum AccountErrorType {
    ALREADY_CONNECTED(1),
    WRONG_PASSWORD(2),
    USERNAME_TAKEN(3),
    INVALID_CREDENTIALS(4),
    ACCOUNT_CREATION_COOLDOWN(5),
    INTERNAL_ERROR(6),
    INVALID_CAPTCHA(7),
    INVALID_BETA_KEY(8),
    EMAIL_ACCOUNT_LIMIT_REACHED(10);

    private final int id;
    AccountErrorType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}