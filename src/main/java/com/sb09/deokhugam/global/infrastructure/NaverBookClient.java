package com.sb09.deokhugam.global.infrastructure;

import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    return new NaverBookDto(
        (String) item.get("title"),
        (String) item.get("author"),
        (String) item.get("description"),
        (String) item.get("publisher"),
        LocalDate.parse(pubdate, DateTimeFormatter.ofPattern("yyyyMMdd")),
        (String) item.get("isbn"),
        (String) item.get("image")
    );
  }
}