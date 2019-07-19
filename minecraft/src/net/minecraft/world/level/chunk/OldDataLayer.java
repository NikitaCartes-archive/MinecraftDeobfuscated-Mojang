package net.minecraft.world.level.chunk;

public class OldDataLayer {
	public final byte[] data;
	private final int depthBits;
	private final int depthBitsPlusFour;

	public OldDataLayer(byte[] bs, int i) {
		this.data = bs;
		this.depthBits = i;
		this.depthBitsPlusFour = i + 4;
	}

	public int get(int i, int j, int k) {
		int l = i << this.depthBitsPlusFour | k << this.depthBits | j;
		int m = l >> 1;
		int n = l & 1;
		return n == 0 ? this.data[m] & 15 : this.data[m] >> 4 & 15;
	}
}
