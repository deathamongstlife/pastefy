package cc.allyapps.pastely.model.responses.ai;

import com.google.gson.annotations.SerializedName;

public class AIStatusResponse {
    public boolean enabled;

    @SerializedName("gateway_connected")
    public boolean gatewayConnected;

    @SerializedName("agent_id")
    public String agentId;

    public String message;

    public AIStatusResponse(boolean enabled, boolean gatewayConnected, String agentId, String message) {
        this.enabled = enabled;
        this.gatewayConnected = gatewayConnected;
        this.agentId = agentId;
        this.message = message;
    }
}
