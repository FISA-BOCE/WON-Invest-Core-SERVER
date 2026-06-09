package com.woorifisa.won_invest_core_server.domain.autoinvest.provider;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class StubSweepFxRateProvider implements SweepFxRateProvider {

    @Override
    public BigDecimal getMonthlySweepUsdKrwRate(LocalDateTime sweepBaseTime) {
        return new BigDecimal("1500.00");
    }
}
