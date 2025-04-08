package com.AMO.autismGame.Emotion;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final MemberRepository memberRepository;
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final java.util.List<String> VALID_EMOTIONS = Arrays.asList("happy", "sad", "angry", "surprised");

    public Map<String, String> saveEmotionPhoto(String userIdentifier, MultipartFile file, String emotion) {
        Map<String, String> response = new HashMap<>();

        // 1. 사용자 확인
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        // 2. 감정 유효성 검사
        if (!VALID_EMOTIONS.contains(emotion.toLowerCase())) {
            response.put("status", "error");
            response.put("message", "Invalid emotion type");
            return response;
        }

        // 3. 파일 유효성 검사
        if (file.isEmpty()) {
            response.put("status", "error");
            response.put("message", "File is empty");
            return response;
        }

        try {
            // 4. 파일 저장 로직
            String savedFileUrl = saveFile(file, userIdentifier, emotion);

            response.put("status", "success");
            response.put("message", "Photo saved with emotion tag");
            response.put("fileUrl", savedFileUrl);
            return response;

        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "Failed to save photo");
            return response;
        }
    }

    private String saveFile(MultipartFile file, String userIdentifier, String emotion) throws IOException {
        // 파일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // 파일명 생성 (userId_timestamp_emotion_uuid.확장자)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = String.format("%s/%s_%s_%s_%s%s",
                emotion,
                userIdentifier,
                timestamp,
                emotion,
                UUID.randomUUID().toString().substring(0, 8),
                extension);

        // S3에 파일 업로드
        amazonS3Client.putObject(new PutObjectRequest(
                bucket,
                filename,
                file.getInputStream(),
                metadata
        ));

        // 업로드된 파일의 URL 반환
        return amazonS3Client.getUrl(bucket, filename).toString();
    }
} 