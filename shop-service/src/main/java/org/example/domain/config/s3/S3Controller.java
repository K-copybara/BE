package org.example.domain.config.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3UploadService s3UploadService;

    // 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestPart("file") MultipartFile file) throws IOException {
        String url = s3UploadService.saveFile(file);
        return ResponseEntity.ok(url);
    }

    // 파일 url 다운로드
    @GetMapping("/download")
    public ResponseEntity<UrlResource> download(@RequestParam String filename) {
        return s3UploadService.downloadImage(filename);
    }

    // 파일 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam String filename) {
        s3UploadService.deleteImage(filename);
        return ResponseEntity.noContent().build();
    }
}
