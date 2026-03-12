package cc.allyapps.pastely.model.responses.app;

import cc.allyapps.pastely.Pastely;
import org.javawebstack.webutils.config.Config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppInfoResponse {

    public String customLogo;
    public String customName;
    public boolean aiEnabled = false;
    public Map<String, String> customFooter;
    public boolean loginRequiredForRead;
    public boolean loginRequiredForCreate;

    public boolean encryptionIsDefault;
    public boolean publicPastesEnabled;

    public AppInfoResponse(Pastely pastely) {
        Config config = pastely.getConfig();

        if (config.has("pastely.info.custom.logo"))
            customLogo = config.get("pastely.info.custom.logo");

        if (config.has("pastely.info.custom.name"))
            customName = config.get("pastely.info.custom.name");

        aiEnabled = pastely.aiEnabled();

        encryptionIsDefault = config.get("pastely.encryption.default", "false").toLowerCase(Locale.ROOT).equals("true");

        loginRequiredForRead = pastely.isLoginRequiredForRead();
        loginRequiredForCreate = pastely.isLoginRequiredForCreate();

        publicPastesEnabled = pastely.publicPastesEnabled();

        String footerString = config.get("pastely.info.custom.footer", "");
        customFooter = new HashMap<>();
        for (String str : footerString.split(",")) {
            if (str.contains("=")) {
                String[] split = str.split("=");
                customFooter.put(split[0], split[1]);
            }
        }
    }
}
