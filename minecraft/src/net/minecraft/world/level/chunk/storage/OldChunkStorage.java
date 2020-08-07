package net.minecraft.world.level.chunk.storage;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.OldDataLayer;

public class OldChunkStorage {
	public static OldChunkStorage.OldLevelChunk load(CompoundTag compoundTag) {
		int i = compoundTag.getInt("xPos");
		int j = compoundTag.getInt("zPos");
		OldChunkStorage.OldLevelChunk oldLevelChunk = new OldChunkStorage.OldLevelChunk(i, j);
		oldLevelChunk.blocks = compoundTag.getByteArray("Blocks");
		oldLevelChunk.data = new OldDataLayer(compoundTag.getByteArray("Data"), 7);
		oldLevelChunk.skyLight = new OldDataLayer(compoundTag.getByteArray("SkyLight"), 7);
		oldLevelChunk.blockLight = new OldDataLayer(compoundTag.getByteArray("BlockLight"), 7);
		oldLevelChunk.heightmap = compoundTag.getByteArray("HeightMap");
		oldLevelChunk.terrainPopulated = compoundTag.getBoolean("TerrainPopulated");
		oldLevelChunk.entities = compoundTag.getList("Entities", 10);
		oldLevelChunk.blockEntities = compoundTag.getList("TileEntities", 10);
		oldLevelChunk.blockTicks = compoundTag.getList("TileTicks", 10);

		try {
			oldLevelChunk.lastUpdated = compoundTag.getLong("LastUpdate");
		} catch (ClassCastException var5) {
			oldLevelChunk.lastUpdated = (long)compoundTag.getInt("LastUpdate");
		}

		return oldLevelChunk;
	}

	public static void convertToAnvilFormat(
		RegistryAccess.RegistryHolder registryHolder, OldChunkStorage.OldLevelChunk oldLevelChunk, CompoundTag compoundTag, BiomeSource biomeSource
	) {
		compoundTag.putInt("xPos", oldLevelChunk.x);
		compoundTag.putInt("zPos", oldLevelChunk.z);
		compoundTag.putLong("LastUpdate", oldLevelChunk.lastUpdated);
		int[] is = new int[oldLevelChunk.heightmap.length];

		for (int i = 0; i < oldLevelChunk.heightmap.length; i++) {
			is[i] = oldLevelChunk.heightmap[i];
		}

		compoundTag.putIntArray("HeightMap", is);
		compoundTag.putBoolean("TerrainPopulated", oldLevelChunk.terrainPopulated);
		ListTag listTag = new ListTag();

		for (int j = 0; j < 8; j++) {
			boolean bl = true;

			for (int k = 0; k < 16 && bl; k++) {
				for (int l = 0; l < 16 && bl; l++) {
					for (int m = 0; m < 16; m++) {
						int n = k << 11 | m << 7 | l + (j << 4);
						int o = oldLevelChunk.blocks[n];
						if (o != 0) {
							bl = false;
							break;
						}
					}
				}
			}

			if (!bl) {
				byte[] bs = new byte[4096];
				DataLayer dataLayer = new DataLayer();
				DataLayer dataLayer2 = new DataLayer();
				DataLayer dataLayer3 = new DataLayer();

				for (int o = 0; o < 16; o++) {
					for (int p = 0; p < 16; p++) {
						for (int q = 0; q < 16; q++) {
							int r = o << 11 | q << 7 | p + (j << 4);
							int s = oldLevelChunk.blocks[r];
							bs[p << 8 | q << 4 | o] = (byte)(s & 0xFF);
							dataLayer.set(o, p, q, oldLevelChunk.data.get(o, p + (j << 4), q));
							dataLayer2.set(o, p, q, oldLevelChunk.skyLight.get(o, p + (j << 4), q));
							dataLayer3.set(o, p, q, oldLevelChunk.blockLight.get(o, p + (j << 4), q));
						}
					}
				}

				CompoundTag compoundTag2 = new CompoundTag();
				compoundTag2.putByte("Y", (byte)(j & 0xFF));
				compoundTag2.putByteArray("Blocks", bs);
				compoundTag2.putByteArray("Data", dataLayer.getData());
				compoundTag2.putByteArray("SkyLight", dataLayer2.getData());
				compoundTag2.putByteArray("BlockLight", dataLayer3.getData());
				listTag.add(compoundTag2);
			}
		}

		compoundTag.put("Sections", listTag);
		compoundTag.putIntArray(
			"Biomes",
			new ChunkBiomeContainer(registryHolder.registryOrThrow(Registry.BIOME_REGISTRY), new ChunkPos(oldLevelChunk.x, oldLevelChunk.z), biomeSource).writeBiomes()
		);
		compoundTag.put("Entities", oldLevelChunk.entities);
		compoundTag.put("TileEntities", oldLevelChunk.blockEntities);
		if (oldLevelChunk.blockTicks != null) {
			compoundTag.put("TileTicks", oldLevelChunk.blockTicks);
		}

		compoundTag.putBoolean("convertedFromAlphaFormat", true);
	}

	public static class OldLevelChunk {
		public long lastUpdated;
		public boolean terrainPopulated;
		public byte[] heightmap;
		public OldDataLayer blockLight;
		public OldDataLayer skyLight;
		public OldDataLayer data;
		public byte[] blocks;
		public ListTag entities;
		public ListTag blockEntities;
		public ListTag blockTicks;
		public final int x;
		public final int z;

		public OldLevelChunk(int i, int j) {
			this.x = i;
			this.z = j;
		}
	}
}
