package com.mjkrempl.cartloader.Minecart;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class CustomMinecart {
	public static final String nbtKey = "chunkLoading";
	private static final String namePrefix = "Chunk Loading Minecart";
	private static final String identifierPrefix = "chunk_loading";
	private static final String identifierSuffix = "minecart";
	
	private CustomMinecart() {}
	
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
	
	public static String getIdentifier(MinecartType type) {
		if (type == MinecartType.NORMAL) return identifierPrefix + "_" + identifierSuffix;
		return identifierPrefix + "_" + type.identifier + "_" + identifierSuffix;
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
		lore.add(ChatColor.DARK_GREEN.toString() + ChatColor.ITALIC + "Chunk Loading");
		meta.setLore(lore);
		//meta.setRarity(ItemRarity.UNCOMMON);
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
	
	public static CraftingRecipe getRecipe(MinecartType type, Material ingredient, Plugin plugin) {
		String identifier = getIdentifier(type);
		NamespacedKey key = new NamespacedKey(plugin, identifier);
		ItemStack item = getItem(type);
		
		ShapelessRecipe recipe = new ShapelessRecipe(key, item);
		recipe.addIngredient(type.material);
		recipe.addIngredient(ingredient);
		
		return recipe;
	}
}
