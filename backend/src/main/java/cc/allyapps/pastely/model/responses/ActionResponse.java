package cc.allyapps.pastely.model.responses;

public class ActionResponse {
    public boolean success = false;
    public String message;

    public ActionResponse() {
    }

    public ActionResponse(boolean success) {
        this.success = success;
    }

    public ActionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ActionResponse success() {
        return new ActionResponse(true);
    }

    public static ActionResponse success(String message) {
        return new ActionResponse(true, message);
    }

    public static ActionResponse error(String message) {
        return new ActionResponse(false, message);
    }
}
