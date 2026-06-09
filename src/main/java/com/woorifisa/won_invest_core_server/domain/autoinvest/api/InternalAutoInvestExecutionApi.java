package com.woorifisa.won_invest_core_server.domain.autoinvest.api;

import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request.AutoInvestExecutionRequest;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.response.AutoInvestExecutionResponse;
import com.woorifisa.won_invest_core_server.domain.autoinvest.service.AutoInvestExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Internal Auto Invest", description = "자동투자 스윕 실행 내부 API")
@RestController
@RequestMapping("/internal/invest/sweep-executions")
@RequiredArgsConstructor
public class InternalAutoInvestExecutionApi {

    private final AutoInvestExecutionService autoInvestExecutionService;

    @Operation(summary = "자동투자 스윕 실행", description = "Card Channel에서 발행한 SWEEP_REQUESTED 이벤트를 기준으로 자동투자 ETF 매수를 실행합니다.")
    @PostMapping
    public AutoInvestExecutionResponse execute(@Valid @RequestBody AutoInvestExecutionRequest request) {
        return autoInvestExecutionService.execute(request);
    }
}
