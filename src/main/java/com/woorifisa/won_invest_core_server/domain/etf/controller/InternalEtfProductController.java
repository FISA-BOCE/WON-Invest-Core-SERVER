// 내부 서버끼리 ETF 상품 정보를 동기화하는 API

package com.woorifisa.won_invest_core_server.domain.etf.controller;

import com.woorifisa.won_invest_core_server.domain.etf.dto.request.EtfProductUpsertRequest;
import com.woorifisa.won_invest_core_server.domain.etf.dto.response.EtfProductUpsertResponse;
import com.woorifisa.won_invest_core_server.domain.etf.service.InvestEtfProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class InternalEtfProductController {

    private final InvestEtfProductService investEtfProductService;

    // Channel 서버의 ETF 동기화 요청을 받아서
    @PostMapping("/internal/etf-products/sync")
    public ResponseEntity<EtfProductUpsertResponse> upsertEtfProduct(
            @RequestBody EtfProductUpsertRequest request
    ) {
        // Core의 ETF 상품 마스터 저장/수정 Service로 넘기고
        EtfProductUpsertResponse response = investEtfProductService.upsertEtfProduct(request);
        // 처리 결과를 JSON으로 돌려줌
        return ResponseEntity.ok(response);
    }
}