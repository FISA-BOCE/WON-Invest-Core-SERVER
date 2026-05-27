package com.woorifisa.won_invest_core_server.domain.etf.repository;

import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestEtfProductRepository extends JpaRepository<InvestEtfProduct, Long> {

    // externalProvider + externalEtfId 기준으로 기존 상품인지 확인
    Optional<InvestEtfProduct> findByExternalProviderAndExternalEtfId(
            String externalProvider,
            String externalEtfId
    );

    // 외부 API에서 externalEtfId가 비어 있을 수도 있으니까 -> 보조로 externalProvider + ticker 조회
    Optional<InvestEtfProduct> findByExternalProviderAndTicker(
            String externalProvider,
            String ticker
    );
}