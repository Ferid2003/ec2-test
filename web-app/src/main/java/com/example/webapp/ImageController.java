package com.example.webapp;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageStorageService storage;

    public ImageController(ImageStorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        storage.store(file);
        return ResponseEntity.ok("Uploaded: " + file.getOriginalFilename());
    }

    @GetMapping("/{name}")
    public ResponseEntity<Resource> download(@PathVariable String name) throws IOException {
        Resource resource = storage.load(name);
        String contentType = Files.probeContentType(resource.getFile().toPath());
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/random/metadata")
    public ImageMetadata randomMetadata() throws IOException {
        return storage.randomMetadata();
    }

    @GetMapping("/{name}/metadata")
    public ImageMetadata metadata(@PathVariable String name) throws IOException {
        return storage.metadata(name);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name) throws IOException {
        storage.delete(name);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<String> notFound(NoSuchFileException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found: " + e.getFile());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
