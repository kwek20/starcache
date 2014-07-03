package com.division.starcache.core;

import com.massivecraft.factions.event.FactionsEventChunkChange;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class FactionsListener implements Listener{
	private final StarCache sC;
	
	public FactionsListener(StarCache instance){
		sC = instance;
	}
	
	
    @EventHandler(priority = EventPriority.LOW)
    public void onLandClaim(FactionsEventChunkChange e) {
        CacheEvent cacheEvent = sC.getCacheEvent();
        if (cacheEvent == null || !cacheEvent.isActive()) {
            return;
        }
        
        Chunk eventChunk = cacheEvent.getEventChunk();
        if (e.getChunk().asBukkitChunk().getX() == eventChunk.getX() && e.getChunk().asBukkitChunk().getZ() == eventChunk.getZ()) {
            e.setCancelled(true);
            e.getUSender().sendMessage(String.format(StarCache.chatFormat, "You cannot claim the StarCache chunk."));
        }
    }

}
