package com.sb09.deokhugam.global.infrastructure;

import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverBookClient {

  @Value("${naver.api.client-id}")
  private String clientId;

  @Value("${naver.api.client-secret}")
  private String clientSecret;

  @Value("${naver.api.book-search-url}")
  private String bookSearchUrl;

  private final RestTemplate restTemplate;

  public NaverBookDto searchByIsbn(String isbn) {
    // 1. 헤더 설정
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Naver-Client-Id", clientId);
    headers.set("X-Naver-Client-Secret", clientSecret);

    // 2. URL 생성 (ISBN으로 검색)
    String url = UriComponentsBuilder.fromHttpUrl(bookSearchUrl)
        .queryParam("query", isbn)
        .queryParam("d_isbn", isbn)
        .toUriString();

    // 3. API 호출
    ResponseEntity<Map> response = restTemplate.exchange(
        url, HttpMethod.GET, new HttpEntity<>(headers), Map.class
    );

    // 4. 응답 파싱
    Map body = response.getBody();
    List<Map> items = (List<Map>) body.get("items");

    if (items == null || items.isEmpty()) {
      throw new CustomException(ErrorCode.BOOK_NOT_FOUND);
    }

    Map item = items.get(0);
    String pubdate = (String) item.get("pubdate"); // "20230101" 형식

    // 설명 1000자 초과 시 마지막 문장 단위로 자르기
    String rawDescription = (String) item.get("description");
    String description = rawDescription;
    if (rawDescription != null && rawDescription.length() > 1000) {
      String truncated = rawDescription.substring(0, 1000);
      int lastPeriod = Math.max(
          truncated.lastIndexOf('.'),
          Math.max(truncated.lastIndexOf('!'), truncated.lastIndexOf('?'))
      );
      description = lastPeriod > 0
          ? truncated.substring(0, lastPeriod + 1)
          : truncated.substring(0, 997) + "...";
    }

    // 썸네일 이미지 URL → base64 인코딩
    String imageUrl = (String) item.get("image");
    String thumbnailImage = null;
    if (imageUrl != null && !imageUrl.isEmpty()) {
      try {
        byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
        if (imageBytes != null) {
          thumbnailImage = Base64.getEncoder().encodeToString(imageBytes);
        }
      } catch (Exception e) {
        log.warn("썸네일 이미지 다운로드 실패: {}", e.getMessage());
      }
    }

    return new NaverBookDto(
        (String) item.get("title"),
        (String) item.get("author"),
        description,
        (String) item.get("publisher"),
        LocalDate.parse(pubdate, DateTimeFormatter.ofPattern("yyyyMMdd")),
        (String) item.get("isbn"),
        thumbnailImage
    );
  }
}