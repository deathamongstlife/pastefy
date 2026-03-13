package cc.allyapps.pastely.model.responses.ai;

public class AITextResponse {
    public String result;
    public boolean success;

    public AITextResponse(String result) {
        this.result = result;
        this.success = true;
    }
}
