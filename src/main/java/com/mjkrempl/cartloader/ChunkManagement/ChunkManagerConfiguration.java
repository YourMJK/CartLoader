package com.mjkrempl.cartloader.ChunkManagement;

public final class ChunkManagerConfiguration {
	public final int regionRadius;
	public final int inactiveRegionLoadTime;
	public final int updateInterval;
	
	public ChunkManagerConfiguration(int regionRadius, int inactiveRegionLoadTime, int updateInterval) {
		this.regionRadius = regionRadius;
		this.inactiveRegionLoadTime = inactiveRegionLoadTime;
		this.updateInterval = updateInterval;
	}
}
