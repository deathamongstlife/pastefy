package de.interaapps.pastely.model.plugins;

import de.interaapps.pastely.Pastely;
import org.javawebstack.webutils.config.Mapping;

public abstract class PastelyBackendPlugin {
    public PastelyBackendPlugin(Pastely pastely) {}

    public abstract void init();
    public abstract void setupModels();
    public abstract void setupPassport();
    public abstract void setupServer();
    public abstract void beforeRoutes();
    public void customConfigMapping(Mapping mapping) {}
}
