package com.division.starcache.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Evan
 */
public class StarCache extends JavaPlugin {

    private SCConfig config;
    private CacheEvent cacheEvent = null;
    private final CacheTask cacheTask = new CacheTask();
    public static String chatFormat = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "StarCache" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "%s";

    @Override
    public void onEnable() {
        config = new SCConfig(this);
        config.load();
        this.getServer().getScheduler().runTaskTimer(this, cacheTask, 15120L, 15120L);
        SCListener listen = new SCListener(this);
        this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                if (cacheEvent == null || cacheEvent.isActive() == false) {
                    return;
                }
                Bukkit.getServer().broadcastMessage(String.format(chatFormat, getAnnouncerMessage()));
                if (cacheEvent.isUnlockStage()) {
                    Bukkit.getServer().broadcastMessage(String.format(chatFormat, "The Cache unlocks in " + (300000 - (System.currentTimeMillis() - cacheEvent.getUnlockStageStart())) / 1000 + " seconds."));
                }
            }
        }, 1800L, 1800L);
    }

    @Override
    public void onDisable() {
        if (cacheEvent.isActive()) {
            cacheEvent.abortEvent();
            this.getServer().broadcastMessage(String.format(chatFormat, "The StarCache event has been cancelled due to shutdown."));
            this.getServer().getScheduler().cancelTasks(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("starcache")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("start")) {
                    if (!sender.hasPermission("starcache.start")) {
                        sender.sendMessage(String.format(chatFormat, "You do not have enough permissions."));
                        return true;
                    }
                    if (cacheEvent == null || !cacheEvent.isActive()) {
                        this.initializeCacheEvent();
                        sender.sendMessage(String.format(chatFormat, "Event has been started."));
                    } else {
                        sender.sendMessage(String.format(chatFormat, "There is already a StarCache Event ongoing."));
                    }
                }
                if (args[0].equalsIgnoreCase("end")) {
                    if (!sender.hasPermission("starcache.end")) {
                        sender.sendMessage(String.format(chatFormat, "You do not have enough permissions."));
                        return true;
                    }
                    if (cacheEvent != null && cacheEvent.isActive()) {
                        cacheEvent.abortEvent();
                        this.getServer().broadcastMessage(String.format(chatFormat, sender.getName() + " has cancelled the StarCache Event."));
                    } else {
                        sender.sendMessage(String.format(chatFormat, "There is no current StarCache Event ongoing."));
                    }
                }
                if (args[0].equalsIgnoreCase("info")) {
                    if (cacheEvent != null && cacheEvent.isActive()) {
                        sender.sendMessage(String.format(chatFormat, getAnnouncerMessage()));
                        if (config.getAnnouncerMethod().equalsIgnoreCase("CHUNK")) {
                            sender.sendMessage(ChatColor.YELLOW + "HINT: Multiply the coordinates by 16 and you get the approximate x and z.");
                        }
                        if (cacheEvent.isUnlockStage()) {
                            sender.sendMessage(String.format(chatFormat, "The Cache unlocks in " + (300000 - (System.currentTimeMillis() - cacheEvent.getUnlockStageStart())) / 1000 + " seconds."));
                        }
                    } else {
                        sender.sendMessage(String.format(chatFormat, "The next StarCache will occur in about" + (((config.getLastEvent() + (config.getEventCooldown() * 1000)) - System.currentTimeMillis()) / 1000) + " seconds."));
                    }

                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid number of arguements.");
                sender.sendMessage(ChatColor.RED + cmd.getUsage());
            }
            return true;
        }
        return false;
    }

    public class CacheTask implements Runnable {

        @Override
        public void run() {
            if (System.currentTimeMillis() - config.getLastEvent() < config.getEventCooldown() * 1000 || cacheEvent != null && cacheEvent.isActive()) {
            } else {
                initializeCacheEvent();
            }
        }
    }

    public void initializeCacheEvent() {
        CacheEvent event = new CacheEvent(config);
        this.cacheEvent = event;
        event.startEvent();
    }

    public CacheEvent getCacheEvent() {
        return cacheEvent;
    }

    public SCConfig getSConfig() {
        return config;
    }

    public String getAnnouncerMessage() {
        if (config.getAnnouncerMethod().equalsIgnoreCase("EXACT")) {
            com.massivecraft.factions.FLocation loc = cacheEvent.getEventChunk();
            return "A StarCache is still spawned in chunk: " + loc.getX() + ", " + loc.getZ() + ". Bring armor and defend the objective.";
        } else {
            org.bukkit.Location loc = cacheEvent.getStarCache().getLocation();
            return "A StarCache is still spawned at location: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ". Bring armor and defend the objective.";
        }
    }
}