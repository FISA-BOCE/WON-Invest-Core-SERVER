package com.woorifisa.won_invest_core_server.domain.admin.service;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.OrderStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.AutoInvestSweepLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.service.projection.AdminAutoInvestExecutionView;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminAutoInvestExecutionServiceTest {

    private AutoInvestSweepLedgerRepository autoInvestSweepLedgerRepository;
    private AdminAutoInvestExecutionService service;

    @BeforeEach
    void setUp() {
        autoInvestSweepLedgerRepository = mock(AutoInvestSweepLedgerRepository.class);
        service = new AdminAutoInvestExecutionService(autoInvestSweepLedgerRepository);
    }

    @Test
    @DisplayName("자동투자 실행 현황을 화면 상태값으로 매핑해서 조회한다")
    void getExecutions() {
        AdminAutoInvestExecutionView view = new AdminAutoInvestExecutionView(
                1L,
                10L,
                UUID.fromString("00000000-0000-4000-8000-000000000001"),
                100L,
                "QQQ",
                AutoInvestExecutionStatus.COMPLETED,
                null,
                null,
                20L,
                OrderStatus.COMPLETED,
                30L,
                BigDecimal.valueOf(1350),
                LocalDateTime.parse("2026-06-10T01:00:00"),
                LocalDateTime.parse("2026-06-10T01:05:00"),
                LocalDateTime.parse("2026-06-10T01:00:00"),
                LocalDateTime.parse("2026-06-10T01:05:00")
        );

        when(autoInvestSweepLedgerRepository.findAdminExecutions(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(view)));
        when(autoInvestSweepLedgerRepository.countAdminExecutions(any(), any(), any(), any(), any()))
                .thenReturn(1L);
        when(autoInvestSweepLedgerRepository.countAdminExchangeCompletedExecutions(any(), any(), any(), any()))
                .thenReturn(1L);
        when(autoInvestSweepLedgerRepository.countAdminOrderFailedExecutions(any(), any(), any(), any()))
                .thenReturn(0L);

        var response = service.getExecutions("COMPLETED", null, null, null, null, 0, 20);

        assertThat(response.summary().totalCount()).isEqualTo(1L);
        assertThat(response.summary().exchangeCompletedCount()).isEqualTo(1L);
        assertThat(response.summary().orderFailedCount()).isZero();
        assertThat(response.summary().completedCount()).isEqualTo(1L);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).executionStatus()).isEqualTo("COMPLETED");
        assertThat(response.items().get(0).fxStatus()).isEqualTo("COMPLETED");
        assertThat(response.items().get(0).orderStatus()).isEqualTo("FILLED");
    }

    @Test
    @DisplayName("사용자 UUID 형식이 올바르지 않으면 예외가 발생한다")
    void invalidUserUuid() {
        assertThatThrownBy(() -> service.getExecutions("ALL", "invalid-uuid", null, null, null, 0, 20))
                .isInstanceOf(BusinessException.class);
    }
}
