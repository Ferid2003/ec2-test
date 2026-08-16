package com.example.webapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesReadsAndDeletesAnImage() throws IOException {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "cat.png", "image/png",
                "hello".getBytes(StandardCharsets.UTF_8));

        service.store(file);

        ImageMetadata meta = service.metadata("cat.png");
        assertEquals("cat.png", meta.name());
        assertEquals("png", meta.extension());
        assertEquals(5, meta.sizeBytes());
        assertNotNull(meta.lastUpdated());

        assertEquals(meta, service.randomMetadata());

        service.delete("cat.png");
        assertThrows(NoSuchFileException.class, () -> service.metadata("cat.png"));
    }

    @Test
    void rejectsFileNamesThatEscapeTheImagesDirectory() throws IOException {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        assertThrows(IllegalArgumentException.class, () -> service.metadata("../secret.txt"));
        assertThrows(IllegalArgumentException.class, () -> service.metadata("sub/dir.png"));
    }

    @Test
    void randomMetadataFailsWhenNoImagesExist() {
        assertThrows(NoSuchFileException.class, () -> new ImageStorageService(tempDir.toString()).randomMetadata());
    }
}
