package cc.allyapps.pastely.model.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BulkOperationResponse {
    public List<String> successful;
    public List<String> failed;

    @SerializedName("total_processed")
    public Integer totalProcessed;

    @SerializedName("success_count")
    public Integer successCount;

    @SerializedName("failed_count")
    public Integer failedCount;
}
