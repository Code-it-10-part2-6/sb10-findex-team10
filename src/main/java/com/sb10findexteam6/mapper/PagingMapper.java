package com.sb10findexteam6.mapper;

import com.sb10findexteam6.dto.PagingResponse;
import java.util.List;
import java.util.function.Function;

public class PagingMapper {
  /**
   * 커서 기반 페이지네이션 응답 생성
   * size+1개 조회 후 hasNext 판단, 실제 size개만 반환
   */
  public static <T>PagingResponse<T> toDto(
      List<T> content,    // DB 에서 조회한 데이터 리스트
      int size, // 한번에 몇개의 데이터를 로드하는 사이즈
      Function<T, Long> id )// indexInfo, indexData 양쪽에서 호출할수 있어서 Function타입
  {
    boolean hasNext = content.size() > size;
    List<T> result = hasNext ? content.subList(0, size) : content;
    Long nextCursor = result.isEmpty()
        ? null
        : id.apply(result.get(result.size() - 1));

    return new PagingResponse<>(result, nextCursor, hasNext);
  }

}
