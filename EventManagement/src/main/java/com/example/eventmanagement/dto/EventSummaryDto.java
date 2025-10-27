package com.example.eventmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventSummaryDto( //другие Dto потом
                               Long id,
                               String name,
                               LocalDate date,
                               BigDecimal ticketPrice
) {
}
