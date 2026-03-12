package cc.allyapps.pastely.helper.minio;

import cc.allyapps.pastely.Pastely;
import io.minio.MinioClient;

public class MinioFolderHelper {
    public static void createFolderIfNotExists() {
        MinioClient minioClient = Pastely.getInstance().getMinioClient();
    }
}
