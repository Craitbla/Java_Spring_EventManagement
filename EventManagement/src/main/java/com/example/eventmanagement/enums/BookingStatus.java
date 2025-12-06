package com.example.eventmanagement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookingStatus {
    CONFIRMED("подтверждено"),
    CANCELED("отменено"),
    PENDING_CONFIRMATION("ожидает подтверждения");

    private final String str;

    BookingStatus(String str) {
        this.str = str;
    }

    @JsonValue
    public String getStr() {
        return str;
    }

    @JsonCreator
    public static BookingStatus fromString(String str) {
        if (str == null) return null;
        for (BookingStatus status : BookingStatus.values()) {
            if (status.str.equals(str)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + str);
    }
}