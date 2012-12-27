package com.division.starcache.core;

import java.io.File;
import java.io.IOException;
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
    private boolean changed = false;
    private List<Cache> cacheList = new ArrayList<Cache>();

    public SCConfig(StarCache instance) {
        configFile = new File(instance.getDataFolder(), "config.yml");
    }

    public void load() {
        try {
            System.out.println("[StarCache] Loading config..");
            config.load(configFile);
        } catch (Exception ex) {
            System.out.println("[StarCache] generating config...");
        }

        if (!config.contains("general.eventCooldown")) {
            config.set("general.eventCooldown", 43200);
            changed = true;
        } else {
            this.eventCooldown = config.getLong("general.eventCooldown");
        }
        if (!config.contains("general.world.radius")) {
            config.set("general.world.radius", 0);
            changed = true;
        } else {
            worldRadius = config.getInt("general.world.radius");
        }

        if (!config.contains("general.lastEvent")) {
            config.set("general.lastEvent", 0);
            changed = true;
        } else {
            this.lastEvent = config.getLong("general.lastEvent");
        }
        if (config.contains("caches")) {
            Set<String> caches = config.getConfigurationSection("caches").getKeys(false);
            for (String cache : caches) {
                System.out.println(cache);
                List<String> items = config.getStringList("caches." + cache);
                cacheList.add(new Cache(items));
            }
        }

        if (!config.contains("general.announcer.method")) {
            config.set("general.announcer.method", "EXACT");
            changed = true;
        } else {
            this.announcerMethod = config.getString("general.announcer.method");
        }

        if (changed) {
            try {
                config.save(configFile);
            } catch (IOException ex) {
            }
            changed = false;
            load();
        } else {
            System.out.println("[StarCache] Config loaded...");
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
    
    public String getAnnouncerMethod(){
        return announcerMethod;
    }

    public List<Cache> getCacheList() {
        return cacheList;
    }
}
