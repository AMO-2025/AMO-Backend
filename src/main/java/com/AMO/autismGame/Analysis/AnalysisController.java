package com.AMO.autismGame.Analysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

 @RestController
 @RequestMapping("/api/analysis") // 📌 API 경로 예시
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/face")
    public ResponseEntity<String> analyzeFaceImage(
            // 📌 프론트엔드에서 'image'라는 키로 FormData를 보내야 합니다.
            @RequestParam("image") MultipartFile imageFile) {

        System.out.println("=========================================");
        System.out.println("=== 1. CONTROLLER /api/analysis/face HIT ===");
        System.out.println("=========================================");
        
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Image file is empty\"}");
        }

        try {
            // Service를 호출하여 SageMaker 분석 실행
            String analysisResultJson = analysisService.invokeSageMakerEndpoint(
                imageFile.getBytes(), 
                imageFile.getContentType()
            );
            
            // SageMaker에서 받은 JSON 응답을 프론트엔드로 그대로 전달
            return ResponseEntity.ok(analysisResultJson); 

        } catch (IOException e) {
            // 파일 읽기 오류
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error reading image file: " + e.getMessage() + "}");
        } catch (Exception e) {
            // SageMaker 호출 오류 또는 기타 서버 오류
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Analysis failed: " + e.getMessage() + "}");
        }
    }
}
