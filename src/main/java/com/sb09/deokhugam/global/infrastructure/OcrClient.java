package com.sb09.deokhugam.global.infrastructure;

import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrClient {

  @Value("${ocr.api.key}")
  private String apiKey;

  @Value("${ocr.api.url}")
  private String apiUrl;

  private final RestTemplate restTemplate;

  // ISBN 패턴 (978 또는 979로 시작하는 13자리 숫자)
  private static final Pattern ISBN_PATTERN =
      Pattern.compile("(?:ISBN[:\\s]*)?(97[89][\\s-]?(?:\\d[\\s-]?){9}\\d)");

  public String extractIsbn(MultipartFile image) {
    try {
      // 1. 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      // 2. 요청 파라미터 설정
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("apikey", apiKey);
      body.add("language", "kor");
      body.add("isOverlayRequired", false);
      body.add("file", image.getResource());

      // 3. API 호출
      HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

      // 4. 응답에서 텍스트 추출
      Map responseBody = response.getBody();
      List<Map> parsedResults = (List<Map>) responseBody.get("ParsedResults");

      if (parsedResults == null || parsedResults.isEmpty()) {
        throw new CustomException(ErrorCode.INVALID_REQUEST);
      }

      String parsedText = (String) parsedResults.get(0).get("ParsedText");
      log.info("OCR 인식 텍스트: {}", parsedText);

      // 5. 텍스트에서 ISBN 추출
      Matcher matcher = ISBN_PATTERN.matcher(parsedText);
      if (matcher.find()) {
        String isbn = matcher.group(1).replaceAll("[\\s-]", "");
        log.info("ISBN 인식 성공: {}", isbn);
        return isbn;
      }

      throw new CustomException(ErrorCode.INVALID_REQUEST);

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("OCR 처리 중 오류 발생", e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}