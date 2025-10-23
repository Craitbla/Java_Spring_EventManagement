package com.example.eventmanagement.enums;
public enum BookingStatus {
    CONFIRMED("подтверждено"),
    CANCELED("отменено"),
    PENDING_CONFIRMATION("ожидает подтверждения");
    private final String str;

    BookingStatus(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

}

