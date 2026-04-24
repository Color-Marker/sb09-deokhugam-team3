package com.sb09.deokhugam.global.infrastructure;

import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
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
      // 헤더 설정 (변경)
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 폼 데이터 형식으로 변경

      // base64로 이미지 인코딩
      byte[] imageBytes = compressImageIfNeeded(image);
      String base64Image = "data:image/jpeg;base64,"
          + Base64.getEncoder().encodeToString(imageBytes);

      // 요청 파라미터 설정 (String 타입만 받도록 변경)
      MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
      body.add("apikey", apiKey);
      body.add("language", "kor");
      body.add("isOverlayRequired", "false"); // boolean 대신 String으로
      body.add("base64Image", base64Image);

      // API 호출
      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
      //-------------------------------------------------------------------------------------------------------------
      // 응답에서 텍스트 추출
      Map responseBody = response.getBody();
      log.info("OCR API 응답 전체: {}", responseBody);
      List<Map> parsedResults = (List<Map>) responseBody.get("ParsedResults");

      if (parsedResults == null || parsedResults.isEmpty()) {
        throw new CustomException(ErrorCode.INVALID_REQUEST);
      }

      String parsedText = (String) parsedResults.get(0).get("ParsedText");
      log.info("OCR 인식 텍스트: {}", parsedText);

      // 텍스트에서 ISBN 추출
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

  // 이미지를 1MB 이하로 압축 (OCR.space 용량 제한 대응)
  private byte[] compressImageIfNeeded(MultipartFile image) throws Exception {
    byte[] originalBytes = image.getBytes();

    // 1MB 이하면 그대로 사용
    if (originalBytes.length <= 1024 * 1024) {
      return originalBytes;
    }

    // 1MB 초과 시 리사이징
    BufferedImage originalImage = ImageIO.read(image.getInputStream());
    if (originalImage == null) {
      return originalBytes; // 읽기 실패 시 원본 반환
    }

    // 가로 최대 1500px로 비율 유지하며 축소
    int maxWidth = 1500;
    int width = originalImage.getWidth();
    int height = originalImage.getHeight();

    if (width > maxWidth) {
      height = (int) ((double) height * maxWidth / width);
      width = maxWidth;
    }

    BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = resized.createGraphics();
    g2d.drawImage(originalImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH), 0, 0,
        null);
    g2d.dispose();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(resized, "jpeg", baos);

    log.info("이미지 압축: {}KB → {}KB", originalBytes.length / 1024, baos.toByteArray().length / 1024);
    return baos.toByteArray();
  }
}