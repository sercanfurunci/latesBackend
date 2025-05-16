package com.example.senior_project.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/price-suggestion")
    public ResponseEntity<Map<String, Object>> suggestPrice(@RequestBody Map<String, Object> dto) {
        String prompt = String.format(
                "Bir e-ticaret sitesinde satılacak ürün için fiyat öner: \n" +
                        "Ürün adı: %s\nAçıklama: %s\nKategori ID: %s\nStok: %s\n" +
                        "Sadece sayısal bir fiyat öner (TL cinsinden).",
                dto.get("title"), dto.get("description"), dto.get("categoryId"), dto.get("stock"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(
                Map.of("role", "system", "content", "Sen bir fiyat tahmin asistanısın. Sadece sayısal fiyat öner."));
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);
        requestBody.put("max_tokens", 20);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(
                    openaiApiUrl, request, Map.class);
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    String content = message.get("content");
                    // Sadece sayısal kısmı al
                    String priceStr = content.replaceAll("[^0-9.]", "");
                    try {
                        double price = Double.parseDouble(priceStr);
                        if (price <= 0) {
                            return ResponseEntity.status(400)
                                    .body(Map.of("error", "Yapay zeka bu ürün için fiyat öneremedi."));
                        }
                        return ResponseEntity.ok(Map.of("suggestedPrice", price, "currency", "TL"));
                    } catch (Exception ex) {
                        return ResponseEntity.status(400)
                                .body(Map.of("error", "Yapay zeka geçerli bir fiyat öneremedi. AI yanıtı: " + content));
                    }
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "OpenAI hatası: " + e.getMessage()));
        }
        return ResponseEntity.status(500).body(Map.of("error", "Fiyat önerisi alınamadı"));
    }
}