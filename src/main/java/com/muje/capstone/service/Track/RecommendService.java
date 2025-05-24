package com.muje.capstone.service.Track;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muje.capstone.dto.Track.JobPostingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserKeywordService keywordService;

    public List<JobPostingResponse> getRecommendedJobs(String userId, String sessionId) {
        List<String> keywords = keywordService.getLatestKeywordCombo(userId, sessionId);
        log.info("[추천 키워드 조합] {}", keywords);

        if (keywords.isEmpty()) return Collections.emptyList();

        try {
            String joined = String.join(" ", keywords);
            String encoded = URLEncoder.encode(joined, StandardCharsets.UTF_8);
            String url = "http://localhost:4040/crawl?keyword=" + encoded;
            log.info("[크롤링 요청 URL] {}", url);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), new TypeReference<List<JobPostingResponse>>() {});
        } catch (Exception e) {
            log.error("[크롤링 요청 실패]", e);
            return Collections.emptyList();
        }
    }
}
