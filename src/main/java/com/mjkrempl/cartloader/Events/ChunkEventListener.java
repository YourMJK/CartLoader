package com.mjkrempl.cartloader.Events;

import com.mjkrempl.cartloader.ChunkManagement.GlobalChunkManager;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ChunkEventListener implements Listener {
	private final JavaPlugin plugin;
	private final GlobalChunkManager chunkManager;
	
	public ChunkEventListener(JavaPlugin plugin, GlobalChunkManager chunkManager) {
		this.plugin = plugin;
		this.chunkManager = chunkManager;
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		UUID worldUID = event.getWorld().getUID();
		chunkManager.onChunkLoaded(chunk.getX(), chunk.getZ(), worldUID);
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		UUID worldUID = event.getWorld().getUID();
		chunkManager.onChunkUnloaded(chunk.getX(), chunk.getZ(), worldUID);
	}
	
	@EventHandler
	public void onEntitiesLoad(EntitiesLoadEvent event) {
		Chunk chunk = event.getChunk();
		UUID worldUID = event.getWorld().getUID();
		chunkManager.onEntitiesLoaded(chunk.getX(), chunk.getZ(), worldUID);
	}
	
	@EventHandler
	public void onEntitiesUnload(EntitiesUnloadEvent event) {
		Chunk chunk = event.getChunk();
		UUID worldUID = event.getWorld().getUID();
		chunkManager.onEntitiesUnloaded(chunk.getX(), chunk.getZ(), worldUID);
	}
}
