package com.woorifisa.won_invest_core_server.domain.autoinvest.provider;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class StubSweepEtfPriceProvider implements SweepEtfPriceProvider {

    @Override
    public BigDecimal getMonthlySweepEtfExecutionPrice(String ticker, LocalDateTime sweepBaseTime) {
        return switch (ticker) {
            case "VOO" -> new BigDecimal("375.40");
            case "QQQ" -> new BigDecimal("420.10");
            case "SCHD" -> new BigDecimal("78.25");
            default -> new BigDecimal("100.00");
        };
    }
}
