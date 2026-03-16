package com.sb10findexteam6.mapper;

import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import com.sb10findexteam6.dto.PagingResponse;
import org.springframework.stereotype.Component;

@Component
public class PagingMapper {

  public <T> PagingResponse<T> toResponse(
      List<T> results,
      int size,
      long totalElements,
      Function<T, Long> idExtractor
  ) {
    boolean hasNext = results.size() > size;
    List<T> content = hasNext ? results.subList(0, size) : results;

    Long nextIdAfter = null;
    String nextCursor = null;
    if (hasNext) {
      nextIdAfter = idExtractor.apply(content.get(content.size() - 1));
      nextCursor = Base64.getEncoder()
          .encodeToString(("{\"id\":" + nextIdAfter + "}").getBytes());
    }

    return new PagingResponse<>(
        content,
        nextCursor,
        nextIdAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  public Long resolveIdAfter(String cursor, Long idAfter) {
    if (cursor != null && !cursor.isBlank()) {
      String decoded = new String(Base64.getDecoder().decode(cursor));
      return Long.parseLong(decoded.replaceAll("[^0-9]", ""));
    }
    return idAfter;
  }


}
