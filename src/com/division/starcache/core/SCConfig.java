package com.division.starcache.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Evan
 */
public class SCConfig {

    private File configFile;
    private YamlConfiguration config = new YamlConfiguration();
    private long eventCooldown;
    private long lastEvent;
    private int worldRadius;
    private String announcerMethod;
    private boolean usingFactions;
    private List<Cache> cacheList = new ArrayList<Cache>();

    public SCConfig(StarCache instance) {
        if (!instance.getDataFolder().exists()) {
            instance.getDataFolder().mkdirs();
        }
        configFile = new File(instance.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                setup();
            } catch (IOException e) {
            }
        }
    }

    private void setup() {
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            InputStream in = this.getClass().getResourceAsStream("/config.yml");
            byte[] bytesRec = new byte[4096];
            int bytes;
            while ((bytes = in.read(bytesRec, 0, bytesRec.length)) != -1) {
                fos.write(bytesRec, 0, bytes);
            }
            in.close();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            System.out.println("[StarCache] Loading config..");
            config.load(configFile);
        } catch (Exception ex) {
            System.out.println("[StarCache] generating config...");
            setup();
            load();
            return;
        }
        this.eventCooldown = config.getLong("general.eventCooldown");
        this.worldRadius = config.getInt("general.world.radius");
        this.lastEvent = config.getLong("general.lastEvent");
        this.announcerMethod = config.getString("general.announcer.method");
        if (config.contains("general.useFactions")) {
            this.usingFactions = config.getBoolean("general.useFactions");
        } else {
            this.usingFactions = true;
        }
        if (config.contains("caches")) {
            Set<String> caches = config.getConfigurationSection("caches").getKeys(false);
            for (String cache : caches) {
                System.out.println(cache);
                List<String> items = config.getStringList("caches." + cache);
                cacheList.add(new Cache(items));
            }
        }
    }

    public long getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(long lastEvent) {
        this.lastEvent = lastEvent;
        config.set("general.lastEvent", lastEvent);
        try {
            config.save(configFile);
        } catch (IOException ex) {
        }
    }

    public int getWorldRadius() {
        return worldRadius;
    }

    public long getEventCooldown() {
        return eventCooldown;
    }

    public String getAnnouncerMethod() {
        return announcerMethod;
    }

    public List<Cache> getCacheList() {
        return cacheList;
    }

    public boolean isUsingFactions() {
        return usingFactions;
    }
}
