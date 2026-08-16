package com.example.webapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@Service
public class ImageStorageService {

    private final Path root;

    public ImageStorageService(@Value("${images.dir:./images-data}") String dir) throws IOException {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public void store(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }
        file.transferTo(resolve(name));
    }

    public Resource load(String name) throws IOException {
        Path path = existing(name);
        return new UrlResource(path.toUri());
    }

    public void delete(String name) throws IOException {
        existing(name);
        Files.delete(resolve(name));
    }

    public ImageMetadata metadata(String name) throws IOException {
        return toMetadata(existing(name));
    }

    public ImageMetadata randomMetadata() throws IOException {
        try (Stream<Path> files = Files.list(root)) {
            List<Path> all = files.filter(Files::isRegularFile).toList();
            if (all.isEmpty()) {
                throw new NoSuchFileException("No images available");
            }
            return toMetadata(all.get(ThreadLocalRandom.current().nextInt(all.size())));
        }
    }

    private Path existing(String name) throws NoSuchFileException {
        Path path = resolve(name);
        if (!Files.isRegularFile(path)) {
            throw new NoSuchFileException(name);
        }
        return path;
    }

    private ImageMetadata toMetadata(Path path) throws IOException {
        String name = path.getFileName().toString();
        long size = Files.size(path);
        Instant lastUpdated = Files.getLastModifiedTime(path).toInstant();
        int dot = name.lastIndexOf('.');
        String extension = dot >= 0 ? name.substring(dot + 1) : "";
        return new ImageMetadata(name, lastUpdated, size, extension);
    }

    // ponytail: filenames are trusted as simple names, not full paths; reject
    // anything that could escape the images directory.
    private Path resolve(String name) {
        if (name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name: " + name);
        }
        Path path = root.resolve(name).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Invalid file name: " + name);
        }
        return path;
    }
}
