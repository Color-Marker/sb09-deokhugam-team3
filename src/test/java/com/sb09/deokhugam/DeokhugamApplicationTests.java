package com.sb09.deokhugam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "NAVER_CLIENT_ID=test-client-id",
    "NAVER_CLIENT_SECRET=test-client-secret",
    "OCR_API_KEY=test-ocr-key",
    "AWS_S3_ACCESS_KEY=test-access-key",
    "AWS_S3_SECRET_KEY=test-secret-key",
    "AWS_S3_REGION=ap-northeast-2",
    "AWS_S3_BUCKET=test-bucket",
    "STORAGE_TYPE=local"
})
class DeokhugamApplicationTests {

  @Test
  void contextLoads() {
  }

}
