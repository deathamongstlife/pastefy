package de.interaapps.pastely.model.plugins;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PastelyPlugin {
    public PastelyPluginConfig config;
    public PastelyBackendPlugin backendPlugin;
    public String path;

    public PastelyPlugin(String folder) throws IOException {
        this.path = folder;

        File configFile = new File(path + "/plugin.json");
        config = new Gson().fromJson(FileUtils.readFileToString(configFile), PastelyPluginConfig.class);
    }

    public String getPublicFolder() {
        return "/assets/plugins/" + config.name;
    }
}
