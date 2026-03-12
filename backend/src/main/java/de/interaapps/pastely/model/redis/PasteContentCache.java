package de.interaapps.pastely.model.redis;

import de.interaapps.pastely.Pastely;
import de.interaapps.pastely.model.database.Paste;

public class PasteContentCache {
    public static String getCachedContent(Paste paste) {
        if (!Pastely.getInstance().isRedisEnabled()) return null;
        try {
            return Pastely.getInstance().getRedisClient().get("paste:" + paste.getId());
        } catch (Exception e) {
            System.out.println("Failed to get cached content for paste " + paste.getId());
            if (Pastely.getInstance().isDevMode()) e.printStackTrace();
            return null;
        }
    }

    public static void setCachedContent(Paste paste) {
        if (!Pastely.getInstance().isRedisEnabled()) return;
        try {
            Pastely.getInstance().getRedisClient()
                    .setex("paste:" + paste.getId(), 60 * 30, paste.getContent(false));
        } catch (Exception e) {
            System.out.println("Failed to set cached content for paste " + paste.getId());
            if (Pastely.getInstance().isDevMode()) e.printStackTrace();
        }
    }

    public static void deleteCachedContent(Paste paste) {
        if (!Pastely.getInstance().isRedisEnabled()) return;
        try {
            Pastely.getInstance().getRedisClient().del("paste:" + paste.getId());
        } catch (Exception e) {
            System.out.println("Failed to delete cached content for paste " + paste.getId());
            e.printStackTrace();
        }
    }
}
