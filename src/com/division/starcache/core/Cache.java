package com.division.starcache.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

/**
 * 
 * @author Evan
 */
public class Cache {

	private Random r = new Random();
	private List<ItemStack> items = new LinkedList<ItemStack>();

	public Cache(List<ItemStack> entries) {
		items.addAll(entries);
	}

	public void insertCacheIntoChest(Chest c) {
		for (ItemStack i : items){
			int random = r.nextInt(27);
			while (c.getBlockInventory().getItem(random) != null && !c.getBlockInventory().getItem(random).getType().equals(Material.AIR)){
				random = r.nextInt(27);
			}
			
			c.getBlockInventory().setItem(random, i);
		}
	}
}
