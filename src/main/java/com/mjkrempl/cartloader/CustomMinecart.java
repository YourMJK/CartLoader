package com.mjkrempl.cartloader;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class CustomMinecart {
	public static final String nbtKey = "chunkLoading";
	public static final String namePrefix = "Chunk Loading Minecart";
	
	public static String getName(MinecartType type) {
		return switch (type) {
			case NORMAL -> namePrefix;
			case CHEST -> namePrefix + " with Chest";
			case FURNACE -> namePrefix + " with Furnace";
			case TNT -> namePrefix + " with TNT";
			case HOPPER -> namePrefix + " with Hopper";
			case COMMAND_BLOCK -> namePrefix + " with Command Block";
		};
	}
	
	public static ItemStack getItem(MinecartType type) {
		return getItem(type, 1);
	}
	public static ItemStack getItem(MinecartType type, int amount) {
		ItemStack item = new ItemStack(type.material, amount);
		
		NBT.modify(item, nbt -> {
			nbt.setBoolean(nbtKey, true);
		});
		
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		
		List<String> lore = new ArrayList<>();
		lore.add("Chunk Loading");
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static boolean isEntity(Entity entity) {
		if (!(entity instanceof Vehicle vehicle)) return false;
		
		return NBT.get(vehicle, nbt -> {
			ReadableNBT data = nbt.getCompound("data");
			if (data == null) return false;
			return data.getBoolean(nbtKey);
		});
	}
}
