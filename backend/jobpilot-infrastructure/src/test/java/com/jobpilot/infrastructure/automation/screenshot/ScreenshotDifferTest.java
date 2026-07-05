package com.jobpilot.infrastructure.automation.screenshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ScreenshotDifferTest {

    private ScreenshotDiffer differ;

    @BeforeEach
    void setUp() {
        differ = new ScreenshotDiffer();
    }

    @Test
    @DisplayName("Should detect similar screenshots")
    void shouldDetectSimilar() throws IOException {
        var img = createTestImage(100, 100, 255, 0, 0);
        var bytes1 = imageToBytes(img);
        var bytes2 = imageToBytes(img);

        var result = differ.compareScreenshots(bytes1, bytes2);
        assertFalse(result.hasChanged());
        assertTrue(result.similarity() > 0.9);
    }

    @Test
    @DisplayName("Should detect different screenshots")
    void shouldDetectDifferent() throws IOException {
        var img1 = createTestImage(100, 100, 255, 0, 0);
        var img2 = createTestImage(100, 100, 0, 0, 255);

        var result = differ.compareScreenshots(imageToBytes(img1), imageToBytes(img2));
        assertTrue(result.hasChanged());
    }

    @Test
    @DisplayName("Should handle null inputs")
    void shouldHandleNull() {
        var result = differ.compareScreenshots(null, new byte[0]);
        assertFalse(result.hasChanged());
        assertEquals("Missing screenshot data", result.message());
    }

    @Test
    @DisplayName("Should convert to/from base64")
    void shouldConvertBase64() {
        var original = new byte[]{1, 2, 3, 4, 5};
        var base64 = differ.toBase64(original);
        var decoded = differ.fromBase64(base64);
        assertArrayEquals(original, decoded);
    }

    @Test
    @DisplayName("Should verify normal submission screenshot")
    void shouldVerifyNormalSubmission() throws IOException {
        var img = createTestImage(200, 200, 128, 128, 128);
        var result = differ.verifySubmission(imageToBytes(img));
        assertNotNull(result);
        assertNotNull(result.message());
    }

    @Test
    @DisplayName("Should handle null verification screenshot")
    void shouldHandleNullVerification() {
        var result = differ.verifySubmission(null);
        assertFalse(result.hasChanged());
    }

    private BufferedImage createTestImage(int width, int height, int r, int g, int b) {
        var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }

    private byte[] imageToBytes(BufferedImage img) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
