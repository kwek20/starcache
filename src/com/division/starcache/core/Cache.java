package com.division.starcache.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Evan
 */
public class Cache {

    private Map<Integer,Integer> itemMap = new HashMap<Integer,Integer>();
    
    public Cache(List<String> entries){
        for(int i = 0; i < entries.size(); ++i){
            String raw = entries.get(i);
            String[] vals = raw.split("%");
            itemMap.put(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
        }
    }
    
    public void insertCacheIntoChest(Chest chest){
        Inventory inventory = chest.getBlockInventory();
        Set<Integer> keys = itemMap.keySet();
        for(Integer key: keys){
            inventory.addItem(new ItemStack(key,itemMap.get(key)));
        }
    }
}
