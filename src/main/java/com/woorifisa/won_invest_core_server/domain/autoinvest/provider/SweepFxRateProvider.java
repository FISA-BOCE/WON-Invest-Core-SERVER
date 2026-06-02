package com.woorifisa.won_invest_core_server.domain.autoinvest.provider;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SweepFxRateProvider {
    BigDecimal getMonthlySweepUsdKrwRate(LocalDateTime sweepBaseTime);
}
