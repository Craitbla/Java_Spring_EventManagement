package com.example.eventmanagement.enums;

//planned', 'in progress', 'cancelled', 'completed'
//'запланировано', 'проходит', 'отменено', 'завершено'
//final запрещает менять строковое значение КОНСТАНТ enum (это хорошо!)
//Но вы можете менять ЗНАЧЕНИЕ ПОЛЯ в сущности Event:
//event.setStatus(EventStatus.ONGOING); // Меняем на другую константу

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventStatus {
    PLANNED("запланировано"),
    ONGOING("проходит"),
    CANCELED("отменено"),
    COMPLETED("завершено");

    private final String str;

    EventStatus(String str) {
        this.str = str;
    }

    @JsonValue
    public String getStr() {
        return str;
    }

    @JsonCreator
    public static EventStatus fromString(String str) {
        if (str == null) return null;
        for (EventStatus status : EventStatus.values()) {
            if (status.str.equals(str)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + str);
    }

    public boolean isBookable() { return this == PLANNED; }
}