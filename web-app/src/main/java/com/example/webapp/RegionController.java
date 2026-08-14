package com.example.webapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
public class RegionController {

    private static final String TOKEN_URL = "http://169.254.169.254/latest/api/token";
    private static final String AZ_URL = "http://169.254.169.254/latest/meta-data/placement/availability-zone";
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    @GetMapping("/region")
    public String region() {
        try {
            String token = fetchToken();
            String availabilityZone = fetchAvailabilityZone(token);
            String region = availabilityZone.substring(0, availabilityZone.length() - 1);
            return "Region: " + region + ", Availability Zone: " + availabilityZone;
        } catch (Exception e) {
            return "Region information unavailable (not running on EC2 or metadata service unreachable): " + e.getMessage();
        }
    }

    private String fetchToken() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .timeout(TIMEOUT)
                .header("X-aws-ec2-metadata-token-ttl-seconds", "21600")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String fetchAvailabilityZone(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AZ_URL))
                .timeout(TIMEOUT)
                .header("X-aws-ec2-metadata-token", token)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
