package com.division.starcache.core;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Factions;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author Evan
 */
public class CacheEvent {

    private Random random = new Random();
    private boolean eventStarted = false;
    private boolean unlockStage = false;
    private long unlockStageStart;
    private Player winner = null;
    private FLocation eventChunk;
    private Block starCache = null;
    private SCConfig config = null;

    public CacheEvent(SCConfig config) {
        this.config = config;
    }

    private Block getCacheLocation() {
        int radius = config.getWorldRadius();
        double maxX = radius;
        double maxZ = radius;
        double minX = -radius;
        double minZ = -radius;
        int x = (int) (random.nextInt((int) (maxX - minX)) + minX);
        int z = (int) (random.nextInt((int) (maxZ - minZ)) + minZ);
        return Bukkit.getServer().getWorld("world").getHighestBlockAt(x, z);
    }

    public void startEvent() {
        Block cacheBlock = getCacheLocation();
        if (cacheBlock.getLocation().getBlockY() >= 230) {
            cacheBlock = getCacheLocation();
        }
        FLocation factionChunk = new FLocation(cacheBlock);
        while (Board.getFactionAt(factionChunk) != Factions.i.getNone()) {
            cacheBlock = getCacheLocation();
            factionChunk = new FLocation(cacheBlock);
        }
        cacheBlock.setType(Material.CHEST);
        Chest chest = (Chest) cacheBlock.getState();
        List<Cache> cacheList = config.getCacheList();
        if (cacheList.size() >= 2) {
            cacheList.get(random.nextInt(cacheList.size())).insertCacheIntoChest(chest);
        } else if (cacheList.size() == 1) {
                cacheList.get(0).insertCacheIntoChest(chest);
        } else {
            Bukkit.getServer().broadcastMessage(String.format(StarCache.chatFormat, "Error Code: CL11. Please contact an admin."));
            cacheBlock.setType(Material.AIR);
            return;
        }
        this.eventChunk = factionChunk;
        this.starCache = cacheBlock;
        Bukkit.getServer().broadcastMessage(String.format(StarCache.chatFormat, "A StarCache has been placed in chunk: " + eventChunk.getX() + ", " + eventChunk.getZ() + ". Bring armor and defend the objective."));
        this.eventStarted = true;
        this.unlockStageStart = System.currentTimeMillis();
    }

    public void clearArea() {
        Player[] ents = Bukkit.getServer().getOnlinePlayers();
        starCache.getWorld().createExplosion(starCache.getLocation(), 0.0F);
        for (Player player : ents) {
            if (player.equals(winner)) {
                continue;
            }
            final Location playerLoc = player.getLocation();
            final Location blockLoc = starCache.getLocation();
            final double dist = blockLoc.distance(playerLoc);
            if (dist <= 16) {
                double scale = 0.3125D / (dist / 16);
                Vector pulseVector = playerLoc.toVector().subtract(blockLoc.toVector()).normalize();
                pulseVector = new Vector(3, 1.67, 3).multiply(pulseVector).setY(1.67);
                player.setVelocity(pulseVector.multiply(scale));
                player.sendMessage(String.format(StarCache.chatFormat, "The StarCache emits a pulse of energy knocking away all but the winner."));

            }
        }
    }

    public void abortEvent() {
        Chest contents = (Chest) starCache.getState();
        contents.getBlockInventory().clear();
        starCache.setType(Material.AIR);
        eventStarted = false;
    }

    public void cleanup() {
        Chest cache = (Chest) starCache.getState();
        cache.getBlockInventory().clear();
        starCache.setType(Material.AIR);
        this.eventStarted = false;
        this.winner = null;
    }

    public boolean isActive() {
        return eventStarted;
    }

    public boolean isUnlockStage() {
        return unlockStage;
    }

    public FLocation getEventChunk() {
        return eventChunk;
    }

    public Player getEventWinner() {
        return winner;
    }

    public Block getStarCache() {
        return starCache;
    }

    public long getUnlockStageStart() {
        return unlockStageStart;
    }

    public void setEventWinner(Player winner) {
        this.winner = winner;
        this.unlockStage = false;
        winner.sendMessage(String.format(StarCache.chatFormat, "Congratulations you have won and are immune to the StarCache's forces."));
    }
}
