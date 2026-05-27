// ETF 상품 마스터 upsert
/*
Channel 서버가 ETF 정보 전달
        ↓
Core 서버의 upsertEtfProduct() 실행
        ↓
이미 등록된 ETF인지 확인
        ↓
있으면 기존 상품 정보 수정
없으면 새 ETF 상품 저장
        ↓
etfId 포함해서 응답 반환
 */

package com.woorifisa.won_invest_core_server.domain.etf.service;

import com.woorifisa.won_invest_core_server.domain.etf.dto.request.EtfProductUpsertRequest;
import com.woorifisa.won_invest_core_server.domain.etf.dto.response.EtfProductUpsertResponse;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.type.EtfProductStatus;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvestEtfProductService {

    private final InvestEtfProductRepository investEtfProductRepository;

    @Transactional
    public EtfProductUpsertResponse upsertEtfProduct(EtfProductUpsertRequest request) {
        // DB에 이미 같은 ETF가 있는지 find
        InvestEtfProduct product = findExistingProduct(request);

        // 없다면 -> 새로 저장
        // Request DTO → Entity 생성 → Repository.save() → DB insert
        if (product == null) {
            InvestEtfProduct savedProduct = investEtfProductRepository.save(
                    InvestEtfProduct.builder()
                            .externalProvider(request.externalProvider())
                            .externalEtfId(request.externalEtfId())
                            .ticker(request.ticker())
                            .isin(request.isin())
                            .etfName(request.etfName())
                            .market(request.market())
                            .currency(request.currency() != null ? request.currency() : EtfCurrency.USD)
                            .productStatus(request.productStatus() != null ? request.productStatus() : EtfProductStatus.INACTIVE)
                            .isFractionalAvailable(request.isFractionalAvailable())
                            .isTradeAvailable(request.isTradeAvailable())
                            .lastSyncedAt(LocalDateTime.now())
                            .build()
            );

            //  DB에 저장된 Entity를 응답 DTO로 바꿔서 반환
            return EtfProductUpsertResponse.from(savedProduct);
        }


        // 있다면 -> 수정
        product.updateProductInfo(
                request.externalEtfId(),
                request.isin(),
                request.etfName(),
                request.market(),
                request.currency(),
                request.productStatus(),
                request.isFractionalAvailable(),
                request.isTradeAvailable(),
                LocalDateTime.now()
        );

        // 수정된 Entity를 응답 DTO로 반환
        return EtfProductUpsertResponse.from(product);
    }

    // 기존 상품 찾는 기준 (EtfId 기준)
    private InvestEtfProduct findExistingProduct(EtfProductUpsertRequest request) {
        if (request.externalEtfId() != null && !request.externalEtfId().isBlank()) {
            return investEtfProductRepository
                    // 1순위 : externalProvider + externalEtfId 있는지
                    .findByExternalProviderAndExternalEtfId(
                            request.externalProvider(),
                            request.externalEtfId()
                    )
                    .orElse(null);
        }

        return investEtfProductRepository
                // 2순위 : externalProvider + ticker 있는지 (externalEtfId 없는 경우)
                .findByExternalProviderAndTicker(
                        request.externalProvider(),
                        request.ticker()
                )
                .orElse(null);
    }
}