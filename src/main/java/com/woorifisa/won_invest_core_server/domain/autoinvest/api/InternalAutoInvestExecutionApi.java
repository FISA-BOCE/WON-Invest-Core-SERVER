package com.woorifisa.won_invest_core_server.domain.autoinvest.api;

import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request.AutoInvestExecutionRequest;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.response.AutoInvestExecutionResponse;
import com.woorifisa.won_invest_core_server.domain.autoinvest.service.AutoInvestExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/invest/sweep-executions")
@RequiredArgsConstructor
public class InternalAutoInvestExecutionApi {

    private final AutoInvestExecutionService autoInvestExecutionService;

    @PostMapping
    public AutoInvestExecutionResponse execute(@Valid @RequestBody AutoInvestExecutionRequest request) {
        return autoInvestExecutionService.execute(request);
    }
}
