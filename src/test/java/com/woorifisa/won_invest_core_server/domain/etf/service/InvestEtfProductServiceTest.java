package com.woorifisa.won_invest_core_server.domain.etf.service;

import com.woorifisa.won_invest_core_server.domain.etf.dto.request.EtfProductUpsertRequest;
import com.woorifisa.won_invest_core_server.domain.etf.dto.response.EtfProductUpsertResponse;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfProductStatus;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class InvestEtfProductServiceTest {

    // 테스트 대상 주입
    @Autowired
    private InvestEtfProductService investEtfProductService;

    // 테스트 결과용 주입
    @Autowired
    private InvestEtfProductRepository investEtfProductRepository;

    // 각 테스트가 끝날 때마다 실행
    @AfterEach
    void tearDown() {
        investEtfProductRepository.deleteAll();
    }

    // 테스트1
    @Test
    @DisplayName("신규 ETF 상품 정보를 저장하고 Core 기준 etfId를 반환한다")
    void upsertEtfProduct_createsNewProduct() {
        // given
        EtfProductUpsertRequest request = new EtfProductUpsertRequest(
                "KIS",
                "VOO",
                "VOO",
                "US9229083632",
                "Vanguard S&P 500 ETF",
                "NYSE",
                EtfCurrency.USD,
                EtfProductStatus.ACTIVE,
                true,
                true
        );

        // when - 실제 Service 메서드 실행
        EtfProductUpsertResponse response = investEtfProductService.upsertEtfProduct(request);

        // then -  결과 검증
        assertThat(response.etfId()).isNotNull();
        assertThat(response.externalProvider()).isEqualTo("KIS");
        assertThat(response.externalEtfId()).isEqualTo("VOO");
        assertThat(response.ticker()).isEqualTo("VOO");
        assertThat(response.etfName()).isEqualTo("Vanguard S&P 500 ETF");
        assertThat(response.productStatus()).isEqualTo(EtfProductStatus.ACTIVE);
        assertThat(response.isFractionalAvailable()).isTrue();
        assertThat(response.isTradeAvailable()).isTrue();

        assertThat(investEtfProductRepository.count()).isEqualTo(1);
    }

    // 테스트2
    @Test
    @DisplayName("이미 존재하는 ETF 상품이면 새로 저장하지 않고 기존 상품 정보를 갱신한다")
    void upsertEtfProduct_updatesExistingProduct() {
        // given1 - 저장되어있을 데이터
        EtfProductUpsertRequest firstRequest = new EtfProductUpsertRequest(
                "KIS",
                "VOO",
                "VOO",
                "US9229083632",
                "Old ETF Name",
                "NYSE",
                EtfCurrency.USD,
                EtfProductStatus.INACTIVE,
                false,
                false
        );

        // 데이터 저장 -> DB에 1행 생성
        EtfProductUpsertResponse firstResponse =
                investEtfProductService.upsertEtfProduct(firstRequest);

        // given2 - 같은 ETF 업데이트(etfName, EtfProductStatus, isFractionalAvailable, isTradeAvailable)
        // externalProvider와 externalEtfId 는 같은 상태
        EtfProductUpsertRequest updateRequest = new EtfProductUpsertRequest(
                "KIS",
                "VOO",
                "VOO",
                "US9229083632",
                "Vanguard S&P 500 ETF",
                "NYSE",
                EtfCurrency.USD,
                EtfProductStatus.ACTIVE,
                true,
                true
        );

        // when - 업데이트 요청 실행
        EtfProductUpsertResponse updateResponse =
                investEtfProductService.upsertEtfProduct(updateRequest);

        // then1 - 같은 ETF인지 확인
        assertThat(updateResponse.etfId()).isEqualTo(firstResponse.etfId());
        // then2 - DB row 여전히 1개인지 확인
        assertThat(investEtfProductRepository.count()).isEqualTo(1);

        // then3 =  실제 DB 값 바뀌었는지 확인
        InvestEtfProduct product = investEtfProductRepository
                .findById(updateResponse.etfId())
                .orElseThrow();

        assertThat(product.getEtfName()).isEqualTo("Vanguard S&P 500 ETF");
        assertThat(product.getProductStatus()).isEqualTo(EtfProductStatus.ACTIVE);
        assertThat(product.getIsFractionalAvailable()).isTrue();
        assertThat(product.getIsTradeAvailable()).isTrue();
    }
}
