package com.mjkrempl.cartchunkloader.ChunkManagement;

public final class ChunkCoord {
	private final long pairValue;
	
	public ChunkCoord(int x, int z) {
		this.pairValue = pair(x, z);
	}
	public ChunkCoord(long pairValue) {
		this.pairValue = pairValue;
	}
	
	public static long pair(int x, int z) {
		// Shift z into upper 32 bits, and x into lower 32 bits of a long
		return ((long)z << 32) | ((long)x & 0xffff_ffffL);
	}
	
	public static int getX(long pairValue) {
		// Mask lower 32 bits
		return (int)pairValue;
	}
	public static int getZ(long pairValue) {
		// Shift upper 32 bits
		return (int)(pairValue >> 32);
	}
	
	public int getX() {
		return getX(pairValue);
	}
	public int getZ() {
		return getZ(pairValue);
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(pairValue);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ChunkCoord) {
			return pairValue == ((ChunkCoord)obj).pairValue;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[x=" + getX() + ",z=" + getZ() + "]";
	}
}
