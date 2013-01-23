package com.division.starcache.core;

import java.util.List;

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
        Chunk eventChunk = cacheEvent.getEventChunk();
        Chunk chunk = evt.getBlock().getChunk();
        if (chunk == eventChunk) {
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
        if(cacheEvent == null){
            return;
        }
        if (!cacheEvent.isActive()) {
            return;
        }
        if (evtBlock.equals(cacheEvent.getStarCache())) {
            if (!cacheEvent.isUnlockStage()) {
                cacheEvent.startUnlockStage();
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
                        sC.getServer().getScheduler().runTaskLater(sC, new Runnable() {
                            @Override
                            public void run() {
                                cacheEvent.clearArea();
                            }
                        }, 80L);
                        sC.getServer().getScheduler().runTaskLater(sC, new Runnable() {
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
        Chunk eventChunk = cacheEvent.getEventChunk();
        for (Block b : evt.getBlocks()) {
            Chunk chunk = b.getChunk();
            if (chunk == eventChunk) {
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
        Chunk eventChunk = cacheEvent.getEventChunk();
        Chunk chunkTo = evt.getToBlock().getChunk();
        if (chunkTo == eventChunk) {
            evt.setCancelled(true);
        }
    }
}
