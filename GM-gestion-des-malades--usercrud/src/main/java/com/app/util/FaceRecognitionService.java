package com.app.util;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FaceRecognitionService {

    private CascadeClassifier faceDetector;
    private static final String API_KEY = "RA4cE8NH11DqY3ND-W1Ua7UAK00BfS26";
    private static final String API_SECRET = "qEWJXIkwkXg-ue408s6DWPGuvYZ-E4cG";
    private static final String API_URL = "https://api-us.faceplusplus.com/facepp/v3/compare";

    public FaceRecognitionService() {
        try {
            OpenCV.loadLocally();

            // Load Haar Cascade from classpath
            java.io.InputStream is = getClass()
                    .getResourceAsStream("/com/app/cascades/haarcascade_frontalface_alt.xml");
            if (is != null) {
                File tempFile = File.createTempFile("haarcascade", ".xml");
                tempFile.deleteOnExit();
                try (java.io.FileOutputStream os = new java.io.FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                faceDetector = new CascadeClassifier(tempFile.getAbsolutePath());
                System.out.println("✅ Haar Cascade loaded from resources.");
            } else {
                System.err.println("❌ Haar Cascade XML not found in resources!");
                // Fallback to absolute path just in case
                String cascadePath = "C:/opencv/sources/data/haarcascades/haarcascade_frontalface_alt.xml";
                if (new File(cascadePath).exists()) {
                    faceDetector = new CascadeClassifier(cascadePath);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ OpenCV initialization failed: " + e.getMessage());
        }
    }

    public String captureFaceTemplate() {
        System.out.println("📸 Face Recognition: Initializing camera (Index 0)...");
        VideoCapture capture = new VideoCapture(0);

        if (!capture.isOpened()) {
            System.err.println("❌ Face Recognition ERROR: Could not open camera. Is it being used by another app?");
            return null;
        }

        Mat frame = new Mat();
        Mat face = new Mat();

        try {
            // Increase warm-up to allow auto-focus/exposure to settle
            System.out.println("⏳ Warming up camera...");
            for (int i = 0; i < 20; i++) {
                capture.read(frame);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }

            // Try capturing a non-empty frame with retries
            boolean captured = false;
            for (int attempt = 1; attempt <= 10; attempt++) {
                if (capture.read(frame) && !frame.empty()) {
                    captured = true;
                    System.out.println("✅ Frame captured on attempt " + attempt);
                    break;
                }
                System.out.println("⚠️ Frame empty, retrying... (" + attempt + "/10)");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
            }

            if (!captured || frame.empty()) {
                System.err.println("❌ Face Recognition ERROR: Camera returned empty frames after 10 retries.");
                return null;
            }

            System.out.println("🔍 Detecting face in " + frame.width() + "x" + frame.height() + " frame...");
            MatOfRect faceDetections = new MatOfRect();

            if (faceDetector != null && !faceDetector.empty()) {
                faceDetector.detectMultiScale(frame, faceDetections, 1.1, 3, 0, new Size(100, 100), new Size());

                Rect[] rects = faceDetections.toArray();
                if (rects.length > 0) {
                    System.out.println("👤 Found " + rects.length + " face(s). Processing best match...");
                    Rect rect = rects[0];

                    // Zoom out slightly for API requirements but keep it safe
                    int dw = (int) (rect.width * 0.15);
                    int dh = (int) (rect.height * 0.15);
                    int x = Math.max(0, rect.x - dw);
                    int y = Math.max(0, rect.y - dh);
                    int w = Math.min(frame.width() - x, rect.width + 2 * dw);
                    int h = Math.min(frame.height() - y, rect.height + 2 * dh);

                    Rect expandedRect = new Rect(x, y, w, h);
                    face = new Mat(frame, expandedRect);
                } else {
                    System.out.println("⚠️ Warning: No face detected by OpenCV. Falling back to full frame.");
                    face = frame;
                }
            } else {
                System.err.println("⚠️ Warning: Face detector is not initialized. Using full frame.");
                face = frame;
            }

            if (face.empty()) {
                System.err.println("❌ Face Recognition ERROR: Final face matrix is empty.");
                return null;
            }

            System.out.println("💾 Encoding image to Base64...");
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".jpg", face, buffer);
            String base64 = Base64.getEncoder().encodeToString(buffer.toArray());
            System.out.println("✨ Success! Image encoded (" + base64.length() + " chars).");
            return base64;

        } catch (Exception e) {
            System.err.println("❌ Face Recognition ERROR during capture: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            capture.release();
            System.out.println("🔌 Camera released.");
        }
    }

    public Rect detectFace(Mat frame) {
        if (faceDetector == null || faceDetector.empty() || frame.empty())
            return null;
        MatOfRect faceDetections = new MatOfRect();
        // Relaxed parameters: 1.1 scale, 2 neighbors, minSize 50x50
        faceDetector.detectMultiScale(frame, faceDetections, 1.1, 2, 0, new Size(50, 50), new Size());
        Rect[] rects = faceDetections.toArray();
        if (rects.length > 0)
            return rects[0];
        return null;
    }

    public String encodeFace(Mat frame, Rect rect) {
        if (frame.empty())
            return null;

        Mat face;
        if (rect != null) {
            // Zoom out slightly for API requirements but keep it safe
            int dw = (int) (rect.width * 0.15);
            int dh = (int) (rect.height * 0.15);
            int x = Math.max(0, rect.x - dw);
            int y = Math.max(0, rect.y - dh);
            int w = Math.min(frame.width() - x, rect.width + 2 * dw);
            int h = Math.min(frame.height() - y, rect.height + 2 * dh);
            face = new Mat(frame, new Rect(x, y, w, h));
        } else {
            // FALLBACK: If no face detected, crop the center square (most likely location)
            int width = frame.width();
            int height = frame.height();
            int size = Math.min(width, height);
            int x = (width - size) / 2;
            int y = (height - size) / 2;
            face = new Mat(frame, new Rect(x, y, size, size));
            System.out.println("⚠️ Fallback: No face detected by OpenCV, using center crop.");
        }

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", face, buffer);
        String result = Base64.getEncoder().encodeToString(buffer.toArray());
        face.release();
        return result;
    }

    public boolean verifyFace(String scannedBase64, String storedBase64) {
        if (scannedBase64 == null || storedBase64 == null)
            return false;

        try {
            double confidence = compareFaceWithFacePP(scannedBase64, storedBase64);
            System.out.println("🔍 Face++ Confidence: " + confidence);
            // Lowered threshold to 70.0 for better real-world success rates
            return confidence > 70.0;
        } catch (Exception e) {
            System.err.println("❌ Face++ Comparison Failed: " + e.getMessage());
            return false;
        }
    }

    private double compareFaceWithFacePP(String base64Image1, String base64Image2) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String boundary = "---" + UUID.randomUUID().toString();

        Map<String, String> params = new HashMap<>();
        params.put("api_key", API_KEY);
        params.put("api_secret", API_SECRET);
        params.put("image_base64_1", base64Image1);
        params.put("image_base64_2", base64Image2);

        byte[] requestBody = buildMultipartData(params, boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            if (json.has("confidence")) {
                return json.getDouble("confidence");
            }
        } else {
            System.err.println("Face++ API Error: " + response.body());
        }

        return 0;
    }

    private byte[] buildMultipartData(Map<String, String> params, String boundary) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            baos.write((entry.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        baos.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return baos.toByteArray();
    }

    public javafx.scene.image.Image mat2Image(Mat frame) {
        if (frame.empty())
            return null;

        // Crop to square center before encoding
        int width = frame.width();
        int height = frame.height();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        Mat squareFrame = new Mat(frame, new Rect(x, y, size, size));

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", squareFrame, buffer);
        javafx.scene.image.Image image = new javafx.scene.image.Image(new ByteArrayInputStream(buffer.toArray()));

        // Clean up temporary square matrix
        squareFrame.release();

        return image;
    }
}
