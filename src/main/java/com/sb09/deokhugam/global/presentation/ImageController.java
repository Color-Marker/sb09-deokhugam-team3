package com.sb09.deokhugam.global.presentation;

import com.sb09.deokhugam.global.infrastructure.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

  private final S3Service s3Service;

  // POST /api/images/upload
  @PostMapping("/upload")
  public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
    // 1. 서비스에게 사진을 넘겨줌 (로컬/S3 여부는 환경변수에 따라 Service가 알아서 판단!)
    String imageUrl = s3Service.upload(file);

    // 2. 업로드된 이미지의 접속 URL을 프론트엔드(Postman)에게 반환
    return ResponseEntity.ok(imageUrl);
  }
}