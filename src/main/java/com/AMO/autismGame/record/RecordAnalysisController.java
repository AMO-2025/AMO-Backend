package com.AMO.autismGame.record;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/record")
public class RecordAnalysisController {

    private final RecordAnalysisService recordAnalysisService;

    @Autowired
    public RecordAnalysisController(RecordAnalysisService recordAnalysisService) {
        this.recordAnalysisService = recordAnalysisService;
    }

    @PostMapping("/analysis")
    public ResponseEntity<String> analyzeRecording(@RequestBody Map<String, String> payload) {
        String audioBase64 = payload.get("audio_base64");
        String scenario = payload.get("scenario");

        if (audioBase64 == null || scenario == null || audioBase64.isEmpty() || scenario.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"'audio_base64' and 'scenario' are required.\"}");
        }

        try {
            String analysisResultJson = recordAnalysisService.invokeSageMakerEndpoint(audioBase64, scenario);
            return ResponseEntity.ok(analysisResultJson);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Analysis failed: " + e.getMessage() + "}");
        }
    }
}
