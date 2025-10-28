package org.example.domain.config.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 파일 업로드
    public String saveFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 확장자 추출
        String uuidFileName = UUID.randomUUID().toString() + extension; //uuid로 파일 이름 중복 방지 - prefix

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        log.info("🚀 S3 업로드 시작: {}", multipartFile.getOriginalFilename());
        amazonS3.putObject(bucket, uuidFileName, multipartFile.getInputStream(), metadata);
        return amazonS3.getUrl(bucket, uuidFileName).toString();
    }

    // 파일 다운로드
    public ResponseEntity<UrlResource> downloadImage(String originalFilename) {
        UrlResource urlResource = new UrlResource(amazonS3.getUrl(bucket, originalFilename));

        String contentDisposition = "attachment; filename=\"" +  originalFilename + "\"";

        // header에 CONTENT_DISPOSITION 설정을 통해 클릭 시 다운로드 진행
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(urlResource);

    }

    // 파일 삭제
    public void deleteImage(String originalFilename) {
        String key = originalFilename.contains("amazonaws.com")
                ? originalFilename.substring(originalFilename.indexOf(".com/") + 5)
                : originalFilename;

        amazonS3.deleteObject(bucket, key);
    }
}
