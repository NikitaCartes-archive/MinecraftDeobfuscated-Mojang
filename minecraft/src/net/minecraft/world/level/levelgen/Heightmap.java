package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Heightmap {
	private static final Logger LOGGER = LogManager.getLogger();
	static final Predicate<BlockState> NOT_AIR = blockState -> !blockState.isAir();
	static final Predicate<BlockState> NOT_SNOW = blockState -> !blockState.is(Blocks.SNOW_BLOCK) && !blockState.is(Blocks.SNOW);
	static final Predicate<BlockState> MATERIAL_MOTION_BLOCKING = blockState -> blockState.getMaterial().blocksMotion();
	private final BitStorage data;
	private final Predicate<BlockState> isOpaque;
	private final ChunkAccess chunk;

	public Heightmap(ChunkAccess chunkAccess, Heightmap.Types types) {
		this.isOpaque = types.isOpaque();
		this.chunk = chunkAccess;
		int i = Mth.ceillog2(chunkAccess.getHeight() + 1);
		this.data = new BitStorage(i, 256);
	}

	public static void primeHeightmaps(ChunkAccess chunkAccess, Set<Heightmap.Types> set) {
		int i = set.size();
		ObjectList<Heightmap> objectList = new ObjectArrayList<>(i);
		ObjectListIterator<Heightmap> objectListIterator = objectList.iterator();
		int j = chunkAccess.getHighestSectionPosition() + 16;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				for (Heightmap.Types types : set) {
					objectList.add(chunkAccess.getOrCreateHeightmapUnprimed(types));
				}

				for (int m = j - 1; m >= chunkAccess.getMinBuildHeight(); m--) {
					mutableBlockPos.set(k, m, l);
					BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
					if (!blockState.is(Blocks.AIR)) {
						while (objectListIterator.hasNext()) {
							Heightmap heightmap = (Heightmap)objectListIterator.next();
							if (heightmap.isOpaque.test(blockState)) {
								heightmap.setHeight(k, l, m + 1);
								objectListIterator.remove();
							}
						}

						if (objectList.isEmpty()) {
							break;
						}

						objectListIterator.back(i);
					}
				}
			}
		}
	}

	public boolean update(int i, int j, int k, BlockState blockState) {
		int l = this.getFirstAvailable(i, k);
		if (j <= l - 2) {
			return false;
		} else {
			if (this.isOpaque.test(blockState)) {
				if (j >= l) {
					this.setHeight(i, k, j + 1);
					return true;
				}
			} else if (l - 1 == j) {
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int m = j - 1; m >= this.chunk.getMinBuildHeight(); m--) {
					mutableBlockPos.set(i, m, k);
					if (this.isOpaque.test(this.chunk.getBlockState(mutableBlockPos))) {
						this.setHeight(i, k, m + 1);
						return true;
					}
				}

				this.setHeight(i, k, this.chunk.getMinBuildHeight());
				return true;
			}

			return false;
		}
	}

	public int getFirstAvailable(int i, int j) {
		return this.getFirstAvailable(getIndex(i, j));
	}

	public int getHighestTaken(int i, int j) {
		return this.getFirstAvailable(getIndex(i, j)) - 1;
	}

	private int getFirstAvailable(int i) {
		return this.data.get(i) + this.chunk.getMinBuildHeight();
	}

	private void setHeight(int i, int j, int k) {
		this.data.set(getIndex(i, j), k - this.chunk.getMinBuildHeight());
	}

	public void setRawData(ChunkAccess chunkAccess, Heightmap.Types types, long[] ls) {
		long[] ms = this.data.getRaw();
		if (ms.length == ls.length) {
			System.arraycopy(ls, 0, ms, 0, ls.length);
		} else {
			LOGGER.warn("Ignoring heightmap data for chunk " + chunkAccess.getPos() + ", size does not match; expected: " + ms.length + ", got: " + ls.length);
			primeHeightmaps(chunkAccess, EnumSet.of(types));
		}
	}

	public long[] getRawData() {
		return this.data.getRaw();
	}

	private static int getIndex(int i, int j) {
		return i + j * 16;
	}

	public static enum Types implements StringRepresentable {
		WORLD_SURFACE_WG("WORLD_SURFACE_WG", Heightmap.Usage.WORLDGEN, Heightmap.NOT_AIR),
		WORLD_SURFACE("WORLD_SURFACE", Heightmap.Usage.CLIENT, Heightmap.NOT_AIR),
		WORLD_SURFACE_IGNORE_SNOW(
			"WORLD_SURFACE_IGNORE_SNOW", Heightmap.Usage.LIVE_WORLD, blockState -> Heightmap.NOT_AIR.test(blockState) && Heightmap.NOT_SNOW.test(blockState)
		),
		OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Heightmap.Usage.WORLDGEN, Heightmap.MATERIAL_MOTION_BLOCKING),
		OCEAN_FLOOR("OCEAN_FLOOR", Heightmap.Usage.LIVE_WORLD, Heightmap.MATERIAL_MOTION_BLOCKING),
		MOTION_BLOCKING("MOTION_BLOCKING", Heightmap.Usage.CLIENT, blockState -> blockState.getMaterial().blocksMotion() || !blockState.getFluidState().isEmpty()),
		MOTION_BLOCKING_NO_LEAVES(
			"MOTION_BLOCKING_NO_LEAVES",
			Heightmap.Usage.LIVE_WORLD,
			blockState -> (blockState.getMaterial().blocksMotion() || !blockState.getFluidState().isEmpty()) && !(blockState.getBlock() instanceof LeavesBlock)
		);

		public static final Codec<Heightmap.Types> CODEC = StringRepresentable.fromEnum(Heightmap.Types::values, Heightmap.Types::getFromKey);
		private final String serializationKey;
		private final Heightmap.Usage usage;
		private final Predicate<BlockState> isOpaque;
		private static final Map<String, Heightmap.Types> REVERSE_LOOKUP = Util.make(Maps.<String, Heightmap.Types>newHashMap(), hashMap -> {
			for (Heightmap.Types types : values()) {
				hashMap.put(types.serializationKey, types);
			}
		});

		private Types(String string2, Heightmap.Usage usage, Predicate<BlockState> predicate) {
			this.serializationKey = string2;
			this.usage = usage;
			this.isOpaque = predicate;
		}

		public String getSerializationKey() {
			return this.serializationKey;
		}

		public boolean sendToClient() {
			return this.usage == Heightmap.Usage.CLIENT;
		}

		public boolean keepAfterWorldgen() {
			return this.usage != Heightmap.Usage.WORLDGEN;
		}

		@Nullable
		public static Heightmap.Types getFromKey(String string) {
			return (Heightmap.Types)REVERSE_LOOKUP.get(string);
		}

		public Predicate<BlockState> isOpaque() {
			return this.isOpaque;
		}

		@Override
		public String getSerializedName() {
			return this.serializationKey;
		}
	}

	public static enum Usage {
		WORLDGEN,
		LIVE_WORLD,
		CLIENT;
	}
}
