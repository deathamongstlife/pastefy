package de.interaapps.pastely.helper.minio;

import de.interaapps.pastely.Pastely;
import io.minio.MinioClient;

public class MinioFolderHelper {
    public static void createFolderIfNotExists() {
        MinioClient minioClient = Pastely.getInstance().getMinioClient();
    }
}
