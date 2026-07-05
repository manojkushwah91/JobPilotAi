package com.jobpilot.infrastructure.automation.screenshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class ScreenshotDiffer {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotDiffer.class);

    private static final double SIMILARITY_THRESHOLD = 0.85;

    public DiffResult compareScreenshots(byte[] before, byte[] after) {
        if (before == null || after == null) {
            return new DiffResult(false, 0.0, "Missing screenshot data");
        }

        try {
            var beforeImg = ImageIO.read(new ByteArrayInputStream(before));
            var afterImg = ImageIO.read(new ByteArrayInputStream(after));

            if (beforeImg == null || afterImg == null) {
                return new DiffResult(false, 0.0, "Could not read image data");
            }

            var similarity = calculateSimilarity(beforeImg, afterImg);
            var hasChanged = similarity < SIMILARITY_THRESHOLD;

            log.debug("Screenshot diff: similarity={}, changed={}", similarity, hasChanged);

            return new DiffResult(hasChanged, similarity,
                hasChanged ? "Screenshots differ significantly" : "Screenshots are similar");

        } catch (IOException e) {
            log.error("Failed to compare screenshots: {}", e.getMessage());
            return new DiffResult(false, 0.0, "Error comparing: " + e.getMessage());
        }
    }

    public DiffResult verifySubmission(byte[] screenshotAfterSubmit) {
        if (screenshotAfterSubmit == null) {
            return new DiffResult(false, 0.0, "No screenshot provided");
        }

        try {
            var img = ImageIO.read(new ByteArrayInputStream(screenshotAfterSubmit));
            if (img == null) {
                return new DiffResult(false, 0.0, "Could not read image");
            }

            var dominantColor = getDominantColor(img);
            var brightness = calculateBrightness(img);

            var looksLikeSuccess = brightness > 0.1 && brightness < 0.95;

            return new DiffResult(looksLikeSuccess, looksLikeSuccess ? 0.9 : 0.3,
                looksLikeSuccess ? "Screenshot looks normal" : "Screenshot may indicate error state");

        } catch (IOException e) {
            return new DiffResult(false, 0.0, "Error analyzing screenshot: " + e.getMessage());
        }
    }

    public String toBase64(byte[] screenshot) {
        return Base64.getEncoder().encodeToString(screenshot);
    }

    public byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    private double calculateSimilarity(BufferedImage img1, BufferedImage img2) {
        var width = Math.min(img1.getWidth(), img2.getWidth());
        var height = Math.min(img1.getHeight(), img2.getHeight());

        long totalPixels = (long) width * height;
        long matchingPixels = 0;

        for (int x = 0; x < width; x += 2) {
            for (int y = 0; y < height; y += 2) {
                var rgb1 = img1.getRGB(x, y);
                var rgb2 = img2.getRGB(x, y);

                var r1 = (rgb1 >> 16) & 0xFF;
                var g1 = (rgb1 >> 8) & 0xFF;
                var b1 = rgb1 & 0xFF;

                var r2 = (rgb2 >> 16) & 0xFF;
                var g2 = (rgb2 >> 8) & 0xFF;
                var b2 = rgb2 & 0xFF;

                var diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                if (diff < 50) {
                    matchingPixels++;
                }
            }
        }

        return (double) matchingPixels / (totalPixels / 4);
    }

    private int getDominantColor(BufferedImage img) {
        long r = 0, g = 0, b = 0;
        int count = 0;

        for (int x = 0; x < img.getWidth(); x += 4) {
            for (int y = 0; y < img.getHeight(); y += 4) {
                var rgb = img.getRGB(x, y);
                r += (rgb >> 16) & 0xFF;
                g += (rgb >> 8) & 0xFF;
                b += rgb & 0xFF;
                count++;
            }
        }

        return (int) (((r / count) << 16) | ((g / count) << 8) | (b / count));
    }

    private double calculateBrightness(BufferedImage img) {
        long totalBrightness = 0;
        int count = 0;

        for (int x = 0; x < img.getWidth(); x += 4) {
            for (int y = 0; y < img.getHeight(); y += 4) {
                var rgb = img.getRGB(x, y);
                var r = ((rgb >> 16) & 0xFF) / 255.0;
                var g = ((rgb >> 8) & 0xFF) / 255.0;
                var b = (rgb & 0xFF) / 255.0;

                totalBrightness += (0.299 * r + 0.587 * g + 0.114 * b);
                count++;
            }
        }

        return (double) totalBrightness / count;
    }

    public record DiffResult(boolean hasChanged, double similarity, String message) {}
}
