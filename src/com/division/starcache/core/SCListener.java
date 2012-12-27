package com.division.starcache.core;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.event.LandClaimEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Evan
 */
public class SCListener implements Listener {

    private StarCache sC;
    private Map<String, Integer> factionMap = new HashMap<String, Integer>();

    public SCListener(StarCache instance) {
        this.sC = instance;
        sC.getServer().getPluginManager().registerEvents(this, sC);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent evt) {
        CacheEvent cacheEvent = sC.getCacheEvent();
        if (cacheEvent == null || !cacheEvent.isActive()) {
            return;
        }
        Block evtBlock = evt.getBlock();
        if (evtBlock.equals(cacheEvent.getStarCache())) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(String.format(StarCache.chatFormat, "You cannot break the StarCache."));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent evt) {
        CacheEvent cacheEvent = sC.getCacheEvent();
        if (cacheEvent == null || !cacheEvent.isActive()) {
            return;
        }
        FLocation loc = cacheEvent.getEventChunk();
        Chunk chunk = evt.getBlock().getChunk();
        if (chunk.getX() == loc.getX() && chunk.getZ() == loc.getZ()) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(String.format(StarCache.chatFormat, "You cannot place blocks in the StarCache's chunk."));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent evt) {
        if (evt.getAction() != Action.RIGHT_CLICK_BLOCK || !evt.hasBlock() || evt.getPlayer().hasPermission("starcache.chestbypass")) {
            return;
        }
        Block evtBlock = evt.getClickedBlock();
        final CacheEvent cacheEvent = sC.getCacheEvent();
        if (!cacheEvent.isActive()) {
            return;
        }
        if (evtBlock.equals(cacheEvent.getStarCache())) {
            if (!cacheEvent.isUnlockStage()) {
                cacheEvent.startEvent();
                sC.getServer().broadcastMessage(String.format(StarCache.chatFormat, evt.getPlayer().getName() + " has initiated the StarCache event. Cache will unlock in 5 minutes."));
                evt.setCancelled(true);
            } else {
                if (System.currentTimeMillis() - cacheEvent.getUnlockStageStart() >= 300000) {
                    if (cacheEvent.getEventWinner() == null) {
                        cacheEvent.setEventWinner(evt.getPlayer());
                        sC.getServer().broadcastMessage(String.format(StarCache.chatFormat, evt.getPlayer().getName() + " has won the StarCache event!"));
                        List<Player> ents = evt.getPlayer().getWorld().getPlayers();
                        sC.getSConfig().setLastEvent(System.currentTimeMillis());
                        for (Player player : ents) {
                            final Location playerLoc = player.getLocation();
                            final Location blockLoc = cacheEvent.getStarCache().getLocation();
                            final double dist = blockLoc.distance(playerLoc);
                            if (dist <= 30) {
                                player.playSound(blockLoc, Sound.PORTAL_TRIGGER, 1.0F, 1.0F);
                                player.sendMessage(String.format(StarCache.chatFormat, "The StarCache begins to surge with energy."));
                            }
                        }
                        sC.getServer().getScheduler().scheduleSyncDelayedTask(sC, new Runnable() {
                            @Override
                            public void run() {
                                cacheEvent.clearArea();
                            }
                        }, 80L);
                        sC.getServer().getScheduler().scheduleSyncDelayedTask(sC, new Runnable() {
                            @Override
                            public void run() {
                                if (cacheEvent.isActive()) {
                                    cacheEvent.cleanup();
                                }
                            }
                        }, 1200L);
                    } else {
                        if (cacheEvent.getEventWinner() != evt.getPlayer()) {
                            evt.setCancelled(true);
                            evt.getPlayer().sendMessage(String.format(StarCache.chatFormat, "Someone has already won this event."));
                        }
                    }
                } else {
                    evt.setCancelled(true);
                    evt.getPlayer().sendMessage(String.format(StarCache.chatFormat, (300000 - (System.currentTimeMillis() - cacheEvent.getUnlockStageStart())) / 1000 + " seconds remaining."));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent evt) {
        CacheEvent cacheEvent = sC.getCacheEvent();
        if (cacheEvent == null || !cacheEvent.isActive()) {
            return;
        }
        for (Block b : evt.blockList()) {
            if (b.equals(cacheEvent.getStarCache())) {
                evt.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPistonPush(BlockPistonExtendEvent evt) {
        CacheEvent cacheEvent = sC.getCacheEvent();
        if (cacheEvent == null || !cacheEvent.isActive()) {
            return;
        }
        FLocation loc = cacheEvent.getEventChunk();
        for (Block b : evt.getBlocks()) {
            Chunk chunk = b.getChunk();
            if (chunk.getX() == loc.getX() && chunk.getZ() == loc.getZ()) {
                evt.setCancelled(true);
                return;
            }
            if (b.equals(cacheEvent.getStarCache())) {
                evt.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockFromTo(BlockFromToEvent evt) {
        CacheEvent cacheEvent = sC.getCacheEvent();
        if (cacheEvent == null || !cacheEvent.isActive()) {
            return;
        }
        FLocation loc = cacheEvent.getEventChunk();
        Chunk chunkTo = evt.getToBlock().getChunk();
        if (chunkTo.getX() == loc.getX() && chunkTo.getZ() == loc.getZ()) {
            evt.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLandClaim(LandClaimEvent evt) {
        CacheEvent cacheEvent = sC.getCacheEvent();
        if (cacheEvent == null || !cacheEvent.isActive()) {
            return;
        }
        if (evt.getLocation().equals(cacheEvent.getEventChunk())) {
            if(factionMap.containsKey(evt.getFaction().getTag())){
                factionMap.put(evt.getFaction().getTag(), factionMap.get(evt.getFaction().getTag()).intValue()+1);
                if(factionMap.get(evt.getFaction().getTag())>= 5){
                    evt.getFaction().sendMessage(ChatColor.GREEN+"*-*StarCache"+ChatColor.YELLOW+" unclaimed ALL of your faction's land.");
                    evt.getFaction().sendMessage(ChatColor.BLACK+"Just kidding! <3 Shake.");
                    factionMap.remove(evt.getFaction().getTag());
                }
            } else{
                factionMap.put(evt.getFaction().getTag(), 1);
            }
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(String.format(StarCache.chatFormat, "You cannot claim the StarCache chunk."));
        }
    }
}
