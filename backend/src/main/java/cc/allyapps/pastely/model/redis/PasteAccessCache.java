package cc.allyapps.pastely.model.redis;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.model.database.Paste;
import redis.clients.jedis.Jedis;

public class PasteAccessCache {
    public static long getAccessCount(Paste paste) {
        if (!Pastely.getInstance().isRedisEnabled()) return 0;
        try {
            return Long.parseLong(Pastely.getInstance().getRedisClient().get("paste:" + paste.getId() + ":accessCount"));
        } catch (Exception e) {
            if (Pastely.getInstance().isDevMode()) e.printStackTrace();
            return 0;
        }
    }

    public static void increaseAccessCount(Paste paste) {
        if (!Pastely.getInstance().isRedisEnabled()) return;
        Jedis redis = Pastely.getInstance().getRedisClient();
        try {
            String key = "paste:" + paste.getId() + ":accessCount";

            if (redis.get(key) != null) {
                redis.incr(key);
            } else {
                redis.setex(key, 60 * 30, "1");
            }
        } catch (Exception e) {
            System.out.println("Failed to set access count for paste " + paste.getId());
            if (Pastely.getInstance().isDevMode()) e.printStackTrace();
        }
    }
}
