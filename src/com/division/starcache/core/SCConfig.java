package com.division.starcache.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

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
    private String world;
    private boolean usingFactions;
    private List<Cache> cacheList = new ArrayList<Cache>();
    
    private StarCache plugin;

    public SCConfig(StarCache instance) {
    	plugin = instance;
    	if (!instance.getDataFolder().exists()) {
            instance.getDataFolder().mkdirs();
        }
        configFile = new File(instance.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
        	instance.saveDefaultConfig();
        }
        
        
    }

    @SuppressWarnings("unchecked")
	public void load() {
        try {
            System.out.println("[StarCache] Loading config..");
            checkConfig();
            config.load(configFile);
            
        } catch (Exception ex) {
            System.out.println("[StarCache] generating config...");
            plugin.saveDefaultConfig();
            load();
            return;
        }
        this.eventCooldown = config.getLong("general.eventCooldown");
        this.worldRadius = config.getInt("general.world.radius");
        this.lastEvent = config.getLong("general.lastEvent");
        this.announcerMethod = config.getString("general.announcer.method");
        this.world = config.getString("general.world.name");
        if (Bukkit.getServer().getWorld(world) == null){
        	System.out.println("[STARCACHE] the world option is invalid! resetting...");
        	this.world = Bukkit.getServer().getWorlds().get(0).getName();
        	plugin.getConfig().set("general.world.name", this.world);
        	
        	plugin.getConfig().set("test", new ItemStack[]{new ItemStack(Material.ACACIA_STAIRS), new ItemStack(Material.HARD_CLAY, 29), new ItemStack(Material.COOKED_FISH, 34, (short) 1)});
        	
        	plugin.saveConfig();
        }
        
        if (config.contains("general.useFactions")) {
            this.usingFactions = config.getBoolean("general.useFactions");
        } else {
            this.usingFactions = true;
        }
        
        if (config.contains("caches")) {
            Set<String> caches = config.getConfigurationSection("caches").getKeys(false);
            for (String cache : caches) {
                cacheList.add(new Cache((List<ItemStack>) config.get("caches." + cache)));
            }
        }
    }
    
    public void checkConfig(){
		YamlConfiguration config = YamlConfiguration.loadConfiguration(plugin.getResource("config.yml"));
		
		YamlConfiguration fconfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));
		for (String key : config.getKeys(true)){
			if (!fconfig.contains(key) && !key.equals("caches")){
				plugin.getConfig().set(key, config.get(key));
				System.out.println("[STARCACHE] Added the config value: " + key);
				plugin.saveConfig();
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
    
    public String getWorldName() {
        return world;
    }
}
