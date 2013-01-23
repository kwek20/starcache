package com.division.starcache.core;

import com.massivecraft.factions.event.LandClaimEvent;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class FactionsListener implements Listener{
	private Map<String, Integer> factionMap = new HashMap<String, Integer>();
	private final StarCache sC;
	
	public FactionsListener(StarCache instance){
		sC = instance;
	}
	
	
    @EventHandler(priority = EventPriority.LOW)
    public void onLandClaim(LandClaimEvent evt) {
        CacheEvent cacheEvent = sC.getCacheEvent();
        if (cacheEvent == null || !cacheEvent.isActive()) {
            return;
        }
        Chunk eventChunk = cacheEvent.getEventChunk();
        if (evt.getLocation().getX() == eventChunk.getX() && evt.getLocation().getZ() == eventChunk.getZ()) {
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
