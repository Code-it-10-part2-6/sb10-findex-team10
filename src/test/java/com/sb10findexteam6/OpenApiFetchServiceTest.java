package com.sb10findexteam6;

import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
import com.sb10findexteam6.service.openapi.OpenApiFetchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"dev", "local"}) // application-dev.yaml 및 application-local.yaml(인증키) 설정을 읽어옵니다.
class OpenApiFetchServiceTest {

    @Autowired
    private OpenApiFetchService openApiFetchService;

    @Test
    @DisplayName("공공데이터포털 주가지수시세 OpenAPI 정상 연동 테스트")
    void fetchStockMarketIndexTest() {
        // given (테스트 준비)
        String targetDate = "20240731"; // 공공데이터포털 가이드에 있던 샘플 날짜
        int numOfRows = 5;
        int pageNo = 1;

        // when (실제 서비스 로직 실행)
        FscIndexResponseDto responseDto = openApiFetchService.fetchStockMarketIndex(targetDate, numOfRows, pageNo);

        // then (결과 검증)
        // 1. 응답 DTO 자체가 null이 아닌지 확인
        assertThat(responseDto).isNotNull();

        // 2. OpenAPI 정상 응답 코드("00") 확인
        assertThat(responseDto.response().header().resultCode()).isEqualTo("00");

        // 3. 데이터가 들어있는지 확인
        assertThat(responseDto.response().body().items().item()).isNotEmpty();

        // 눈으로 직접 데이터 확인을 위한 콘솔 출력
        System.out.println("====== [API 호출 결과 확인] ======");
        System.out.println("결과 메시지: " + responseDto.response().header().resultMsg());
        System.out.println("가져온 데이터 개수: " + responseDto.response().body().items().item().size());

        FscIndexResponseDto.Item firstItem = responseDto.response().body().items().item().get(0);
        System.out.println("첫 번째 지수명: " + firstItem.idxNm());
        System.out.println("첫 번째 지수 종가: " + firstItem.clpr());
        System.out.println("==================================");
    }
}