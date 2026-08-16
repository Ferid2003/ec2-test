package com.example.webapp;

import java.time.Instant;

public record ImageMetadata(String name, Instant lastUpdated, long sizeBytes, String extension) {
}
