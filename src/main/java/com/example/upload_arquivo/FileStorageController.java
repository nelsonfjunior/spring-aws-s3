package com.example.upload_arquivo;

import java.io.IOException;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;

@Controller
@RequestMapping("/api/files")
public class FileStorageController {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public FileStorageController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws AwsServiceException, SdkClientException, IOException { //passar o arquivo direto a requisição
        String fileName = StringUtils.cleanPath(file.getOriginalFilename()); //nome do arquivo original limpo

        try {
            // Upload para S3
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileDownloadUri = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);

            return ResponseEntity.ok("File uploaded successfully. Download link: " + fileDownloadUri);
        } catch (S3Exception | IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body("File upload failed.");
        }
    }
    
}
