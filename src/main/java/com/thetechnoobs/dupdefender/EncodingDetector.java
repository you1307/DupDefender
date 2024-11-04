package com.thetechnoobs.dupdefender;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.mozilla.universalchardet.UniversalDetector;

public class EncodingDetector {

    /**
     * Detects the encoding of a file.
     *
     * @param filePath The path to the file.
     * @return The detected Charset, or UTF-8 if the encoding cannot be determined.
     * @throws IOException If an I/O error occurs.
     */
    public static Charset detectFileEncoding(String filePath) throws IOException {
        byte[] buf = new byte[4096];
        Charset detectedCharset = null;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            // (1) Create a UniversalDetector object
            UniversalDetector detector = new UniversalDetector(null);

            // (2) Feed data to the detector in chunks
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }

            // (3) Notify the detector that no more data is coming
            detector.dataEnd();

            // (4) Get the detected encoding
            String encoding = detector.getDetectedCharset();
            if (encoding != null) {
                System.out.println("Detected encoding = " + encoding);
                detectedCharset = Charset.forName(encoding);
            } else {
                System.out.println("No encoding detected, defaulting to UTF-8");
                detectedCharset = Charset.forName("UTF-8");
            }

            // (5) Reset the detector
            detector.reset();
        }

        return detectedCharset;
    }
}

