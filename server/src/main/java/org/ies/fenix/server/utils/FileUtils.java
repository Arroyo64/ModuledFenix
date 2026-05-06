package org.ies.fenix.server.utils;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class FileUtils {

    public static String getContentType(byte[] fileBytes, String filenameWithExtension) throws IOException {
        TikaConfig config = TikaConfig.getDefaultConfig();
        Detector detector = config.getDetector();
        TikaInputStream stream = TikaInputStream.get(new ByteArrayInputStream(fileBytes));
        Metadata metadata = new Metadata();
        metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, filenameWithExtension);
        return detector.detect(stream, metadata).toString();
    }
}