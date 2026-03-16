package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.common.config.properties.OpenApiProperties;
import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.repository.IndexInfoRepository;
import com.sb10findexteam6.repository.SyncJobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class) // 스프링 없이 Mockito만 사용하여 초고속으로 테스트합니다.
@DisplayName("비즈니스 로직 단위 테스트: SyncTargetService")
class SyncTargetServiceTest {

    @Mock // 가짜(Mock) 객체 생성
    private IndexInfoRepository indexInfoRepository;

    @Mock
    private SyncJobRepository syncJobRepository;

    @Mock
    private OpenApiProperties openApiProperties;

    @InjectMocks // 위의 가짜 객체들을 이 서비스에 주입
    private SyncTargetService syncTargetService;

    @Test
    @DisplayName("최근 성공 이력이 있다면, 그 다음 날짜(+1일)를 연동 시작일로 반환한다.")
    void calculateNextSyncDate_WithSuccessHistory() {
        // given
        IndexInfo mockIndexInfo = mock(IndexInfo.class);
        LocalDate lastSuccessDate = LocalDate.of(2024, 7, 31);

        // 가짜 DB(SyncJobRepository)가 7월 31일을 반환하도록 조작
        given(syncJobRepository.findLatestTargetDate(eq(mockIndexInfo), eq(JobType.INDEX_DATA), eq(Result.SUCCESS)))
            .willReturn(Optional.of(lastSuccessDate));

        // when
        LocalDate nextSyncDate = syncTargetService.calculateNextSyncDate(mockIndexInfo);

        // then: 7월 31일의 다음 날인 8월 1일이 나와야 함
        assertThat(nextSyncDate).isEqualTo(LocalDate.of(2024, 8, 1));
    }

    @Test
    @DisplayName("성공 이력이 전혀 없다면, 설정(yaml)된 defaultSyncDays 만큼 과거 날짜를 반환한다.")
    void calculateNextSyncDate_WithoutHistory() {
        // given
        IndexInfo mockIndexInfo = mock(IndexInfo.class);

        // 가짜 DB가 이력이 없다고(Optional.empty) 반환하도록 조작
        given(syncJobRepository.findLatestTargetDate(any(), any(), any()))
            .willReturn(Optional.empty());

        // 가짜 프로퍼티가 365일을 설정값으로 반환하도록 조작
        given(openApiProperties.getDefaultSyncDays()).willReturn(365);

        // when
        LocalDate nextSyncDate = syncTargetService.calculateNextSyncDate(mockIndexInfo);

        // then: 오늘 날짜 기준으로 정확히 365일 전인지 검증
        LocalDate expectedDate = LocalDate.now().minusDays(365);
        assertThat(nextSyncDate).isEqualTo(expectedDate);
    }
}