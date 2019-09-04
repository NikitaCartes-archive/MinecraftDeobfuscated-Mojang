package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;

public class ChunkBiomeContainer implements BiomeManager.NoiseBiomeSource {
	private static final int WIDTH_BITS = (int)Math.round(Math.log(16.0) / Math.log(2.0)) - 2;
	private static final int HEIGHT_BITS = (int)Math.round(Math.log(256.0) / Math.log(2.0)) - 2;
	public static final int BIOMES_SIZE = 1 << WIDTH_BITS + WIDTH_BITS + HEIGHT_BITS;
	public static final int HORIZONTAL_MASK = (1 << WIDTH_BITS) - 1;
	public static final int VERTICAL_MASK = (1 << HEIGHT_BITS) - 1;
	private final Biome[] biomes;

	public ChunkBiomeContainer(Biome[] biomes) {
		this.biomes = biomes;
	}

	private ChunkBiomeContainer() {
		this(new Biome[BIOMES_SIZE]);
	}

	public ChunkBiomeContainer(FriendlyByteBuf friendlyByteBuf) {
		this();

		for (int i = 0; i < this.biomes.length; i++) {
			this.biomes[i] = Registry.BIOME.byId(friendlyByteBuf.readInt());
		}
	}

	public ChunkBiomeContainer(ChunkPos chunkPos, BiomeSource biomeSource) {
		this();
		int i = chunkPos.getMinBlockX() >> 2;
		int j = chunkPos.getMinBlockZ() >> 2;

		for (int k = 0; k < this.biomes.length; k++) {
			int l = k & HORIZONTAL_MASK;
			int m = k >> WIDTH_BITS + WIDTH_BITS & VERTICAL_MASK;
			int n = k >> WIDTH_BITS & HORIZONTAL_MASK;
			this.biomes[k] = biomeSource.getNoiseBiome(i + l, m, j + n);
		}
	}

	public ChunkBiomeContainer(ChunkPos chunkPos, BiomeSource biomeSource, @Nullable int[] is) {
		this();
		int i = chunkPos.getMinBlockX() >> 2;
		int j = chunkPos.getMinBlockZ() >> 2;
		if (is != null) {
			for (int k = 0; k < is.length; k++) {
				this.biomes[k] = Registry.BIOME.byId(is[k]);
				if (this.biomes[k] == null) {
					int l = k & HORIZONTAL_MASK;
					int m = k >> WIDTH_BITS + WIDTH_BITS & VERTICAL_MASK;
					int n = k >> WIDTH_BITS & HORIZONTAL_MASK;
					this.biomes[k] = biomeSource.getNoiseBiome(i + l, m, j + n);
				}
			}
		} else {
			for (int kx = 0; kx < this.biomes.length; kx++) {
				int l = kx & HORIZONTAL_MASK;
				int m = kx >> WIDTH_BITS + WIDTH_BITS & VERTICAL_MASK;
				int n = kx >> WIDTH_BITS & HORIZONTAL_MASK;
				this.biomes[kx] = biomeSource.getNoiseBiome(i + l, m, j + n);
			}
		}
	}

	public int[] writeBiomes() {
		int[] is = new int[this.biomes.length];

		for (int i = 0; i < this.biomes.length; i++) {
			is[i] = Registry.BIOME.getId(this.biomes[i]);
		}

		return is;
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		for (Biome biome : this.biomes) {
			friendlyByteBuf.writeInt(Registry.BIOME.getId(biome));
		}
	}

	public ChunkBiomeContainer copy() {
		return new ChunkBiomeContainer((Biome[])this.biomes.clone());
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		int l = i & HORIZONTAL_MASK;
		int m = Mth.clamp(j, 0, VERTICAL_MASK);
		int n = k & HORIZONTAL_MASK;
		return this.biomes[m << WIDTH_BITS + WIDTH_BITS | n << WIDTH_BITS | l];
	}
}
