package com.example.eventmanagement.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

//planned', 'in progress', 'cancelled', 'completed'
//'запланировано', 'проходит', 'отменено', 'завершено'
//final запрещает менять строковое значение КОНСТАНТ enum (это хорошо!)
//Но вы можете менять ЗНАЧЕНИЕ ПОЛЯ в сущности Event:
//event.setStatus(EventStatus.ONGOING); // Меняем на другую константу
public enum EventStatus {
    PLANNED("запланировано"),
    ONGOING("проходит"),
    CANCELED("отменено"),
    COMPLETED("завершено");

    private final String str;

    EventStatus(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }


}
