package net.minecraft.world.level.lighting;

import net.minecraft.world.level.chunk.DataLayer;

public class FlatDataLayer extends DataLayer {
	public FlatDataLayer() {
		super(128);
	}

	public FlatDataLayer(DataLayer dataLayer, int i) {
		super(128);
		System.arraycopy(dataLayer.getData(), i * 128, this.data, 0, 128);
	}

	@Override
	protected int getIndex(int i, int j, int k) {
		return k << 4 | i;
	}

	@Override
	public byte[] getData() {
		byte[] bs = new byte[2048];

		for (int i = 0; i < 16; i++) {
			System.arraycopy(this.data, 0, bs, i * 128, 128);
		}

		return bs;
	}
}
