package com.app.util;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.util.Base64;

public class FaceRecognitionService {

    private CascadeClassifier faceDetector;

    public FaceRecognitionService() {
        try {
            OpenCV.loadLocally();
            
            // Load Haar Cascade from classpath
            java.io.InputStream is = getClass().getResourceAsStream("/com/app/cascades/haarcascade_frontalface_alt.xml");
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
        System.out.println("📸 Starting camera...");
        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) return null;

        Mat frame = new Mat();
        Mat face = new Mat();
        
        try {
            // Warm up
            for(int i = 0; i < 5; i++) capture.read(frame);
            
            if (frame.empty()) return null;

            MatOfRect faceDetections = new MatOfRect();
            if (faceDetector != null && !faceDetector.empty()) {
                faceDetector.detectMultiScale(frame, faceDetections);
                for (Rect rect : faceDetections.toArray()) {
                    face = new Mat(frame, rect);
                    break;
                }
            } else {
                // Fallback: just use the whole frame if detector is missing
                face = frame;
            }

            if (face.empty()) return null;

            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".jpg", face, buffer);
            return Base64.getEncoder().encodeToString(buffer.toArray());

        } finally {
            capture.release();
        }
    }

    public boolean verifyFace(String scannedBase64, String storedBase64) {
        if (scannedBase64 == null || storedBase64 == null) return false;

        try {
            Mat scannedImg = decodeToMat(scannedBase64);
            Mat storedImg = decodeToMat(storedBase64);

            if (scannedImg.empty() || storedImg.empty()) return false;

            Mat histScanned = new Mat();
            Mat histStored = new Mat();
            
            Imgproc.calcHist(java.util.Arrays.asList(scannedImg), new MatOfInt(0), new Mat(), histScanned, new MatOfInt(256), new MatOfFloat(0, 256));
            Imgproc.calcHist(java.util.Arrays.asList(storedImg), new MatOfInt(0), new Mat(), histStored, new MatOfInt(256), new MatOfFloat(0, 256));

            Core.normalize(histScanned, histScanned, 0, 1, Core.NORM_MINMAX);
            Core.normalize(histStored, histStored, 0, 1, Core.NORM_MINMAX);

            double result = Imgproc.compareHist(histScanned, histStored, Imgproc.CV_COMP_CORREL);
            System.out.println("🔍 Match result: " + result);
            return result > 0.8;
        } catch (Exception e) {
            return false;
        }
    }

    private Mat decodeToMat(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);
        return Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_GRAYSCALE);
    }
}
