// '/internal/etf-products/sync' 내부 API가 보안 필터까지 포함해서 제대로 동작하는지 검증하는 Controller 테스트 코드
/*
1. 내부 인증 헤더가 없으면 401로 차단되는지
2. API Key가 틀리면 401로 차단되는지
3. 인증 헤더가 맞으면 ETF 상품 동기화가 성공하고 공통 응답 형식으로 반환되는지
 */

package com.woorifisa.won_invest_core_server.domain.etf.api;

import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ActiveProfiles("test")
@SpringBootTest
// MockMvc - 실제 서버를 브라우저처럼 띄우지 않아도, 테스트 코드 안에서 HTTP 요청을 보낼 수 있게 해주는 도구
@AutoConfigureMockMvc
class InternalEtfProductApiTest {

    private static final String INTERNAL_SYNC_URL = "/internal/etf-products/sync";
    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvestEtfProductRepository investEtfProductRepository;

    // 각 테스트 끝날때마다 DB 초기화
    @AfterEach
    void tearDown() {
        investEtfProductRepository.deleteAll();
    }

    // 테스트1 - 헤더 없이 요청하면 차단되는지 확인
    @Test
    @DisplayName("test1: 내부 인증 헤더가 없으면 ETF 상품 동기화 요청을 401로 차단한다")
    void upsertEtfProduct_withoutInternalAuthHeaders_returnsUnauthorized() throws Exception {
        // given
        String requestBody = """
                {
                  "externalProvider": "KIS",
                  "externalEtfId": "VOO",
                  "ticker": "VOO",
                  "isin": "US9229083632",
                  "etfName": "Vanguard S&P 500 ETF",
                  "market": "NYSE",
                  "currency": "USD",
                  "productStatus": "ACTIVE",
                  "isFractionalAvailable": true,
                  "isTradeAvailable": true
                }
                """;

        // when & then - 기대결과
        mockMvc.perform(post(INTERNAL_SYNC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));

        // DB에 아무것도 저장되지 않았는지 확인
        assertThat(investEtfProductRepository.count()).isZero();
    }

    //테스트2 - API Key 틀림
    @Test
    @DisplayName("test2: 내부 API Key가 틀리면 ETF 상품 동기화 요청을 401로 차단한다")
    void upsertEtfProduct_withInvalidApiKey_returnsUnauthorized() throws Exception {
        // given
        String requestBody = """
                {
                  "externalProvider": "KIS",
                  "externalEtfId": "VOO",
                  "ticker": "VOO",
                  "isin": "US9229083632",
                  "etfName": "Vanguard S&P 500 ETF",
                  "market": "NYSE",
                  "currency": "USD",
                  "productStatus": "ACTIVE",
                  "isFractionalAvailable": true,
                  "isTradeAvailable": true
                }
                """;

        // when & then - 기대 결과
        mockMvc.perform(post(INTERNAL_SYNC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SERVICE_ID_HEADER, "won-invest-channel")
                        .header(API_KEY_HEADER, "wrong-api-key")
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));

        // DB에 아무것도 저장되지 않았는지 확인
        assertThat(investEtfProductRepository.count()).isZero();
    }

    //테스트3 - 인증 성공
    @Test
    @DisplayName("success: 내부 인증 헤더가 유효하면 ETF 상품을 동기화하고 공통 성공 응답을 반환한다")
    void upsertEtfProduct_withValidInternalAuthHeaders_returnsSuccess() throws Exception {
        // given
        String requestBody = """
                {
                  "externalProvider": "KIS",
                  "externalEtfId": "VOO",
                  "ticker": "VOO",
                  "isin": "US9229083632",
                  "etfName": "Vanguard S&P 500 ETF",
                  "market": "NYSE",
                  "currency": "USD",
                  "productStatus": "ACTIVE",
                  "isFractionalAvailable": true,
                  "isTradeAvailable": true
                }
                """;

        // when & then
        mockMvc.perform(post(INTERNAL_SYNC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SERVICE_ID_HEADER, "won-invest-channel")
                        .header(API_KEY_HEADER, "test-internal-api-key")
                        .content(requestBody))
                .andExpect(status().isOk())
                // 성공 응답이 공통 ApiResponse 형식인지 확인
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("ETF_200_001"))
                .andExpect(jsonPath("$.message").value("ETF 상품 마스터 동기화가 완료되었습니다."))
                // 실제 data 안에 ETF 정보가 들어있는지 확인
                .andExpect(jsonPath("$.data.etfId").exists())
                .andExpect(jsonPath("$.data.externalProvider").value("KIS"))
                .andExpect(jsonPath("$.data.externalEtfId").value("VOO"))
                .andExpect(jsonPath("$.data.ticker").value("VOO"))
                .andExpect(jsonPath("$.data.etfName").value("Vanguard S&P 500 ETF"))
                // 상품 상태와 거래 가능 여부 확인
                .andExpect(jsonPath("$.data.productStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.isFractionalAvailable").value(true))
                .andExpect(jsonPath("$.data.isTradeAvailable").value(true));

        // DB 저장 여부 확인 - 1개 행
        assertThat(investEtfProductRepository.count()).isEqualTo(1);
    }

    // 테스트4 - 필수 요청값 누락 시 - 400
    @Test
    @DisplayName("test4: 필수 요청값이 누락되면 ETF 상품 동기화 요청을 400으로 차단한다")
    void upsertEtfProduct_withInvalidRequest_returnsBadRequest() throws Exception {
        // given
        String requestBody = """
            {
              "externalProvider": "",
              "externalEtfId": "VOO",
              "ticker": "",
              "isin": "US9229083632",
              // 필수값 누락
              "etfName": "",
              "market": "NYSE",
              "currency": "USD",
              "productStatus": "ACTIVE",
              "isFractionalAvailable": true,
              "isTradeAvailable": true
            }
            """;

        // when & then
        mockMvc.perform(post(INTERNAL_SYNC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SERVICE_ID_HEADER, "won-invest-channel")
                        .header(API_KEY_HEADER, "test-internal-api-key")
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COM_400_001"))
                .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않습니다."));

        assertThat(investEtfProductRepository.count()).isZero();
    }
}
