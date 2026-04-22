package com.sb09.deokhugam.global.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
// @RequiredArgsConstructor
public class S3Service {

  //private final S3Client s3Client;
  @Autowired(required = false)   // ← S3 모드일 때만 주입, 없어도 오류 안남
  private S3Client s3Client;

  @Value("${deokhugam.storage.type:local}")
  private String storageType;

  @Value("${deokhugam.storage.local.root-path:../storage}")
  private String localRootPath;

  @Value("${deokhugam.storage.s3.bucket:}")
  private String bucketName;

  @Value("${deokhugam.storage.s3.region:ap-northeast-2}")
  private String region;

  public String upload(MultipartFile file) {
    if ("s3".equals(storageType)) {
      return uploadToS3(file);
    }
    return uploadToLocal(file);
  }

  // S3 업로드
  private String uploadToS3(MultipartFile file) {
    try {
      String fileName = generateFileName(file.getOriginalFilename());

      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucketName)
          .key("thumbnails/" + fileName)
          .contentType(file.getContentType())
          .build();

      s3Client.putObject(request,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      String url =
          "https://" + bucketName + ".s3." + region + ".amazonaws.com/thumbnails/" + fileName;
      log.info("S3 업로드 완료: {}", url);
      return url;

    } catch (IOException e) {
      log.error("S3 업로드 실패", e);
      throw new RuntimeException("파일 업로드에 실패했습니다.", e);
    }
  }

  // 로컬 저장
  private String uploadToLocal(MultipartFile file) {
    try {
      String fileName = generateFileName(file.getOriginalFilename());
      Path uploadPath = Paths.get(localRootPath, "thumbnails").toAbsolutePath().normalize();

      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      Path filePath = uploadPath.resolve(fileName);
      file.transferTo(filePath.toFile());

      String url = "/storage/thumbnails/" + fileName;
      log.info("로컬 저장 완료: {}", url);
      return url;

    } catch (IOException e) {
      log.error("로컬 파일 저장 실패", e);
      throw new RuntimeException("파일 저장에 실패했습니다.", e);
    }
  }

  // 파일명 중복 방지 - UUID 사용
  private String generateFileName(String originalFilename) {
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    return UUID.randomUUID() + extension;
  }
}