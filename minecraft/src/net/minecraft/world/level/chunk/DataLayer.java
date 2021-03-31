package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.VisibleForDebug;

public class DataLayer {
	public static final int SIZE = 2048;
	public static final int LAYER_SIZE = 128;
	private static final int NIBBLE_SIZE = 4;
	@Nullable
	protected byte[] data;

	public DataLayer() {
	}

	public DataLayer(byte[] bs) {
		this.data = bs;
		if (bs.length != 2048) {
			throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + bs.length));
		}
	}

	protected DataLayer(int i) {
		this.data = new byte[i];
	}

	public int get(int i, int j, int k) {
		return this.get(this.getIndex(i, j, k));
	}

	public void set(int i, int j, int k, int l) {
		this.set(this.getIndex(i, j, k), l);
	}

	protected int getIndex(int i, int j, int k) {
		return j << 8 | k << 4 | i;
	}

	private int get(int i) {
		if (this.data == null) {
			return 0;
		} else {
			int j = this.getPosition(i);
			return this.isFirst(i) ? this.data[j] & 15 : this.data[j] >> 4 & 15;
		}
	}

	private void set(int i, int j) {
		if (this.data == null) {
			this.data = new byte[2048];
		}

		int k = this.getPosition(i);
		if (this.isFirst(i)) {
			this.data[k] = (byte)(this.data[k] & 240 | j & 15);
		} else {
			this.data[k] = (byte)(this.data[k] & 15 | (j & 15) << 4);
		}
	}

	private boolean isFirst(int i) {
		return (i & 1) == 0;
	}

	private int getPosition(int i) {
		return i >> 1;
	}

	public byte[] getData() {
		if (this.data == null) {
			this.data = new byte[2048];
		}

		return this.data;
	}

	public DataLayer copy() {
		return this.data == null ? new DataLayer() : new DataLayer((byte[])this.data.clone());
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < 4096; i++) {
			stringBuilder.append(Integer.toHexString(this.get(i)));
			if ((i & 15) == 15) {
				stringBuilder.append("\n");
			}

			if ((i & 0xFF) == 255) {
				stringBuilder.append("\n");
			}
		}

		return stringBuilder.toString();
	}

	@VisibleForDebug
	public String layerToString(int i) {
		StringBuilder stringBuilder = new StringBuilder();

		for (int j = 0; j < 256; j++) {
			stringBuilder.append(Integer.toHexString(this.get(j)));
			if ((j & 15) == 15) {
				stringBuilder.append("\n");
			}
		}

		return stringBuilder.toString();
	}

	public boolean isEmpty() {
		return this.data == null;
	}
}
