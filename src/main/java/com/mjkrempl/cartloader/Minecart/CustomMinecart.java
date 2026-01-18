package com.mjkrempl.cartloader.Minecart;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class CustomMinecart {
	private static final String nbtKey = "chunkLoading";
	private static final String namePrefix = "Chunk Loading Minecart";
	private static final String identifierPrefix = "chunk_loading";
	private static final String identifierSuffix = "minecart";
	
	private static NamespacedKey persistentDataKey = NamespacedKey.minecraft(nbtKey.toLowerCase());
	private static boolean legacySupport = false;
	
	private CustomMinecart() {}
	
	public static void setNamespace(Plugin plugin) {
		persistentDataKey = new NamespacedKey(plugin, nbtKey.toLowerCase());
	}
	public static void setLegacySupport(boolean value) {
		legacySupport = value;
	}
	
	
	// - Strings
	
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
	
	
	// - Item
	
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
	
	public static boolean isItem(ItemStack item) {
		if (item == null) return false;
		return NBT.get(item, nbt -> {
			return nbt.getBoolean(nbtKey);
		});
	}
	
	
	// - Entity
	
	public static void setEntityData(Entity entity) {
		// Set value in "custom data" compound (in case this is called in 1.21.5+ which shouldn't happen)
		NBT.modify(entity, nbt -> {
			nbt.setBoolean(nbtKey, true);
			ReadWriteNBT data = nbt.getOrCreateCompound("data");
			data.setBoolean(nbtKey, true);
		});
		
		// Set value in persistent data container (for legacy versions 1.20.6–1.21.4)
		if (!legacySupport) return;
		entity.getPersistentDataContainer().set(persistentDataKey, PersistentDataType.BOOLEAN, true);
	}
	
	public static boolean isEntity(Entity entity) {
		if (!(entity instanceof Vehicle vehicle)) return false;
		
		// First check "custom data" compound (automatic with custom item for 1.21.5+)
		boolean nbtValue = NBT.get(vehicle, nbt -> {
			ReadableNBT data = nbt.getCompound("data");
			if (data == null) return false;
			return data.getBoolean(nbtKey);
		});
		if (nbtValue) return true;
		
		// Else check persistent data container (manual for legacy versions 1.20.6–1.21.4)
		if (!legacySupport) return false;
		Boolean persistentDataValue = entity.getPersistentDataContainer().get(persistentDataKey, PersistentDataType.BOOLEAN);
		return Boolean.TRUE.equals(persistentDataValue);
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
