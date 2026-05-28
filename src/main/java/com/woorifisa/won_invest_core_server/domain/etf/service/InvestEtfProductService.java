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
동시에 같은 ETF가 저장되려는 경우 DB 유니크 제약 충돌을 잡아서 다시 조회 후 수정
        ↓
etfId 포함해서 응답 반환
 */

package com.woorifisa.won_invest_core_server.domain.etf.service;

import com.woorifisa.won_invest_core_server.domain.etf.dto.request.EtfProductUpsertRequest;
import com.woorifisa.won_invest_core_server.domain.etf.dto.response.EtfProductUpsertResponse;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvestEtfProductService {

    private final InvestEtfProductRepository investEtfProductRepository;

    @Transactional
    public EtfProductUpsertResponse upsertEtfProduct(EtfProductUpsertRequest request) {
        // 1. DB에 이미 같은 ETF가 있는지 find
        InvestEtfProduct product = findExistingProduct(request);

        // 없다면 -> 새로 저장
        // Request DTO → Entity 생성 → Repository.saveAndFlush() → DB insert
        // saveAndFlush(): 이 시점에 바로 DB에 insert를 날려서, 유니크 제약 충돌이 있으면 여기서 바로 예외가 남 -> catch 에서 예외 처리
        // (save()는 DB 반영 시간이 늦어질 수 있음)
        if (product == null) {
            try {
                InvestEtfProduct savedProduct = investEtfProductRepository.saveAndFlush(
                        InvestEtfProduct.builder()
                                .externalProvider(request.externalProvider())
                                .externalEtfId(request.externalEtfId())
                                .ticker(request.ticker())
                                .isin(request.isin())
                                .etfName(request.etfName())
                                .market(request.market())
                                .currency(request.currency())
                                .productStatus(request.productStatus())
                                .isFractionalAvailable(request.isFractionalAvailable())
                                .isTradeAvailable(request.isTradeAvailable())
                                .lastSyncedAt(LocalDateTime.now())
                                .build()
                );

                //  DB에 저장된 Entity를 응답 DTO로 바꿔서 반환
                return EtfProductUpsertResponse.from(savedProduct);

        // 동시성 충돌 처리
        } catch(DataIntegrityViolationException exception) {
                product = findExistingProduct(request);

                if (product == null) {
                    throw exception;
                }
            }
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

    // 기존 상품 찾는 기준: externalProvider + externalEtfId 우선, 없으면 externalProvider + ticker
    private InvestEtfProduct findExistingProduct(EtfProductUpsertRequest request) {
        if (StringUtils.hasText(request.externalEtfId())) {
            return investEtfProductRepository
                    // 1순위 : externalProvider + externalEtfId 있는지
                    .findByExternalProviderAndExternalEtfId(
                            request.externalProvider(),
                            request.externalEtfId()
                    )
                    .orElseGet(() -> findByProviderAndTicker(request));
        }
        return findByProviderAndTicker(request);
    }

    private InvestEtfProduct findByProviderAndTicker(EtfProductUpsertRequest request) {
        return investEtfProductRepository
                .findByExternalProviderAndTicker(
                        request.externalProvider(),
                        request.ticker()
                )
                .orElse(null);
    }
}