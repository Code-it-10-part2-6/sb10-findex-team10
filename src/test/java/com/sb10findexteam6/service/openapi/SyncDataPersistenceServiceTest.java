package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.entity.SyncJob;
import com.sb10findexteam6.repository.IndexDataRepository;
import com.sb10findexteam6.repository.IndexInfoRepository;
import com.sb10findexteam6.repository.SyncJobRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
// import org.springframework.transaction.annotation.Transactional; // <--- 제거합니다!

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
// @Transactional <-- 제거합니다! (REQUIRES_NEW 테스트를 위해)
@DisplayName("DB 트랜잭션 통합 테스트: SyncDataPersistenceService")
class SyncDataPersistenceServiceTest {

    @Autowired private SyncDataPersistenceService syncDataPersistenceService;
    @Autowired private IndexDataRepository indexDataRepository;
    @Autowired private SyncJobRepository syncJobRepository;
    @Autowired private IndexInfoRepository indexInfoRepository;

    private IndexInfo testIndexInfo;

    @BeforeEach
    void setUp() {
        testIndexInfo = new IndexInfo(
            "KOSPI시리즈", "코스피", 839,
            LocalDate.of(1980, 1, 4), new BigDecimal("100"),
            SourceType.OPEN_API, false
        );
        // 트랜잭션이 없으므로 여기 저장된 데이터는 즉시 실제(인메모리) DB에 커밋됩니다!
        indexInfoRepository.save(testIndexInfo);
    }

    @AfterEach
    void tearDown() {
        // @Transactional을 제거했으므로, 매 테스트가 끝난 후 다음 테스트에 영향을 주지 않도록
        // 직접 자식 테이블부터 부모 테이블 순으로 DB를 싹 비워줍니다.
        syncJobRepository.deleteAllInBatch();
        indexDataRepository.deleteAllInBatch();
        indexInfoRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("DB에 기존 데이터가 없으면 신규 저장(Insert)하고 성공 이력을 남긴다.")
    void saveOneDayDataAndSuccessJob_Insert() {
        // given
        LocalDate targetDate = LocalDate.of(2024, 7, 31);
        IndexData fetchedData = createMockData(targetDate, "2770.69");

        // when
        syncDataPersistenceService.saveOneDayDataAndSuccessJob(testIndexInfo, fetchedData, targetDate, "TESTER");

        // then: 데이터 저장 검증
        List<IndexData> savedData = indexDataRepository.findAll();
        assertThat(savedData).hasSize(1);
        assertThat(savedData.get(0).getClosingPrice()).isEqualByComparingTo("2770.69");

        // then: 성공 이력 저장 검증
        List<SyncJob> savedJobs = syncJobRepository.findAll();
        assertThat(savedJobs).hasSize(1);
        assertThat(savedJobs.get(0).getResult()).isEqualTo(Result.SUCCESS);
    }

    @Test
    @DisplayName("DB에 기존 데이터가 이미 존재하면 중복 에러가 발생하지 않고 값을 수정(Update)한다.")
    void saveOneDayDataAndSuccessJob_Update() {
        // given 1
        LocalDate targetDate = LocalDate.of(2024, 7, 31);
        IndexData oldData = createMockData(targetDate, "2700.00");
        indexDataRepository.save(oldData);

        // given 2
        IndexData newData = createMockData(targetDate, "2800.00");

        // when
        syncDataPersistenceService.saveOneDayDataAndSuccessJob(testIndexInfo, newData, targetDate, "TESTER");

        // then
        List<IndexData> savedData = indexDataRepository.findAll();
        assertThat(savedData).hasSize(1);
        assertThat(savedData.get(0).getClosingPrice()).isEqualByComparingTo("2800.00");

        List<SyncJob> savedJobs = syncJobRepository.findAll();
        assertThat(savedJobs).hasSize(1);
    }

    @Test
    @DisplayName("실패 처리 시 실패 사유와 함께 FAILED 상태의 이력을 독립적으로 기록한다.")
    void saveOneDayFailedJob() {
        // given
        LocalDate targetDate = LocalDate.of(2024, 7, 31);
        String failReason = "API 서버 500 에러";

        // when: REQUIRES_NEW가 이제 완벽하게 작동합니다!
        syncDataPersistenceService.saveOneDayFailedJob(testIndexInfo, targetDate, "TESTER", failReason);

        // then
        List<SyncJob> savedJobs = syncJobRepository.findAll();
        assertThat(savedJobs).hasSize(1);
        assertThat(savedJobs.get(0).getResult()).isEqualTo(Result.FAILED);
        assertThat(savedJobs.get(0).getWorker()).isEqualTo("TESTER");
    }

    private IndexData createMockData(LocalDate date, String closingPrice) {
        return new IndexData(
            testIndexInfo, date, SourceType.OPEN_API,
            new BigDecimal("2000.00"), new BigDecimal(closingPrice),
            new BigDecimal("2100.00"), new BigDecimal("1900.00"),
            new BigDecimal("10.00"), new BigDecimal("0.5"),
            1000L, 5000000L, 100000000L
        );
    }
}