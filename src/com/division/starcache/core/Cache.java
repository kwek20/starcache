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

	private Map<String, Integer> itemMap = new HashMap<String, Integer>();

	public Cache(List<String> entries) {
		for (int i = 0; i < entries.size(); ++i) {
			String raw = entries.get(i);
			String[] vals = raw.split("%");
			itemMap.put(vals[0], Integer.parseInt(vals[1]));
		}
	}

	public void insertCacheIntoChest(Chest chest) {
		Inventory inventory = chest.getBlockInventory();
		Set<String> keys = itemMap.keySet();
		for (String key : keys) {
			String[] split = key.split("#");
			if (split.length == 1) {
				inventory.addItem(new ItemStack(Integer.parseInt(split[0]), itemMap.get(key)));
			} else {
				inventory.addItem(new ItemStack(Integer.parseInt(split[0]), itemMap.get(key), Short.parseShort(split[1])));
			}
		}
	}
}
