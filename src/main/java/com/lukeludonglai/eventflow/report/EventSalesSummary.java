package com.lukeludonglai.eventflow.report;

import com.lukeludonglai.eventflow.service.SalesReportService;

import java.math.BigDecimal;
import java.util.UUID;

public record EventSalesSummary (
        UUID eventId,
        String eventTitle,
        int ticketsSold,
        BigDecimal revenue
){
}
