package com.mjkrempl.cartloader.Minecart;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;

public enum MinecartType {
	NORMAL("normal", Material.MINECART, EntityType.MINECART),
	CHEST("chest", Material.CHEST_MINECART, EntityType.CHEST_MINECART),
	FURNACE("furnace", Material.FURNACE_MINECART, EntityType.FURNACE_MINECART),
	TNT("tnt", Material.TNT_MINECART, EntityType.TNT_MINECART),
	HOPPER("hopper", Material.HOPPER_MINECART, EntityType.HOPPER_MINECART),
	COMMAND_BLOCK("command_block", Material.COMMAND_BLOCK_MINECART, EntityType.COMMAND_BLOCK_MINECART);
	//SPAWNER
	
	public static final MinecartType[] all = new MinecartType[] {
		NORMAL,
		CHEST,
		FURNACE,
		TNT,
		HOPPER,
		COMMAND_BLOCK
	};
	
	
	public final String identifier;
	public final Material material;
	public final EntityType entityType;
	
	MinecartType(String identifier, Material material, EntityType entityType) {
		this.identifier = identifier;
		this.material = material;
		this.entityType = entityType;
	}
	
	public static @Nullable MinecartType fromIdentifier(String identifier) {
		return switch (identifier) {
			case "normal" -> NORMAL;
			case "chest" -> CHEST;
			case "furnace" -> FURNACE;
			case "tnt" -> TNT;
			case "hopper" -> HOPPER;
			case "command_block" -> COMMAND_BLOCK;
			default -> null;
		};
	}
	
	public static @Nullable MinecartType fromMaterial(Material material) {
		return switch (material) {
			case MINECART -> NORMAL;
			case CHEST_MINECART -> CHEST;
			case FURNACE_MINECART -> FURNACE;
			case TNT_MINECART -> TNT;
			case HOPPER_MINECART -> HOPPER;
			case COMMAND_BLOCK_MINECART -> COMMAND_BLOCK;
			default -> null;
		};
	}
	
	
	@Override
	public String toString() {
		return identifier;
	}
}
