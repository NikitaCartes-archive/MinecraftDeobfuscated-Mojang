package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.AABB;

public class SpikeFeature extends Feature<SpikeConfiguration> {
	private static final LoadingCache<Long, List<SpikeFeature.EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder()
		.expireAfterWrite(5L, TimeUnit.MINUTES)
		.build(new SpikeFeature.SpikeCacheLoader());

	public SpikeFeature(Function<Dynamic<?>, ? extends SpikeConfiguration> function) {
		super(function);
	}

	public static List<SpikeFeature.EndSpike> getSpikesForLevel(LevelAccessor levelAccessor) {
		Random random = new Random(levelAccessor.getSeed());
		long l = random.nextLong() & 65535L;
		return SPIKE_CACHE.getUnchecked(l);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		SpikeConfiguration spikeConfiguration
	) {
		List<SpikeFeature.EndSpike> list = spikeConfiguration.getSpikes();
		if (list.isEmpty()) {
			list = getSpikesForLevel(levelAccessor);
		}

		for (SpikeFeature.EndSpike endSpike : list) {
			if (endSpike.isCenterWithinChunk(blockPos)) {
				this.placeSpike(levelAccessor, random, spikeConfiguration, endSpike);
			}
		}

		return true;
	}

	private void placeSpike(LevelAccessor levelAccessor, Random random, SpikeConfiguration spikeConfiguration, SpikeFeature.EndSpike endSpike) {
		int i = endSpike.getRadius();

		for (BlockPos blockPos : BlockPos.betweenClosed(
			new BlockPos(endSpike.getCenterX() - i, 0, endSpike.getCenterZ() - i),
			new BlockPos(endSpike.getCenterX() + i, endSpike.getHeight() + 10, endSpike.getCenterZ() + i)
		)) {
			if (blockPos.distSqr((double)endSpike.getCenterX(), (double)blockPos.getY(), (double)endSpike.getCenterZ(), false) <= (double)(i * i + 1)
				&& blockPos.getY() < endSpike.getHeight()) {
				this.setBlock(levelAccessor, blockPos, Blocks.OBSIDIAN.defaultBlockState());
			} else if (blockPos.getY() > 65) {
				this.setBlock(levelAccessor, blockPos, Blocks.AIR.defaultBlockState());
			}
		}

		if (endSpike.isGuarded()) {
			int j = -2;
			int k = 2;
			int l = 3;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int m = -2; m <= 2; m++) {
				for (int n = -2; n <= 2; n++) {
					for (int o = 0; o <= 3; o++) {
						boolean bl = Mth.abs(m) == 2;
						boolean bl2 = Mth.abs(n) == 2;
						boolean bl3 = o == 3;
						if (bl || bl2 || bl3) {
							boolean bl4 = m == -2 || m == 2 || bl3;
							boolean bl5 = n == -2 || n == 2 || bl3;
							BlockState blockState = Blocks.IRON_BARS
								.defaultBlockState()
								.setValue(IronBarsBlock.NORTH, Boolean.valueOf(bl4 && n != -2))
								.setValue(IronBarsBlock.SOUTH, Boolean.valueOf(bl4 && n != 2))
								.setValue(IronBarsBlock.WEST, Boolean.valueOf(bl5 && m != -2))
								.setValue(IronBarsBlock.EAST, Boolean.valueOf(bl5 && m != 2));
							this.setBlock(levelAccessor, mutableBlockPos.set(endSpike.getCenterX() + m, endSpike.getHeight() + o, endSpike.getCenterZ() + n), blockState);
						}
					}
				}
			}
		}

		EndCrystal endCrystal = EntityType.END_CRYSTAL.create(levelAccessor.getLevel());
		endCrystal.setBeamTarget(spikeConfiguration.getCrystalBeamTarget());
		endCrystal.setInvulnerable(spikeConfiguration.isCrystalInvulnerable());
		endCrystal.moveTo(
			(double)((float)endSpike.getCenterX() + 0.5F),
			(double)(endSpike.getHeight() + 1),
			(double)((float)endSpike.getCenterZ() + 0.5F),
			random.nextFloat() * 360.0F,
			0.0F
		);
		levelAccessor.addFreshEntity(endCrystal);
		this.setBlock(levelAccessor, new BlockPos(endSpike.getCenterX(), endSpike.getHeight(), endSpike.getCenterZ()), Blocks.BEDROCK.defaultBlockState());
	}

	public static class EndSpike {
		private final int centerX;
		private final int centerZ;
		private final int radius;
		private final int height;
		private final boolean guarded;
		private final AABB topBoundingBox;

		public EndSpike(int i, int j, int k, int l, boolean bl) {
			this.centerX = i;
			this.centerZ = j;
			this.radius = k;
			this.height = l;
			this.guarded = bl;
			this.topBoundingBox = new AABB((double)(i - k), 0.0, (double)(j - k), (double)(i + k), 256.0, (double)(j + k));
		}

		public boolean isCenterWithinChunk(BlockPos blockPos) {
			return blockPos.getX() >> 4 == this.centerX >> 4 && blockPos.getZ() >> 4 == this.centerZ >> 4;
		}

		public int getCenterX() {
			return this.centerX;
		}

		public int getCenterZ() {
			return this.centerZ;
		}

		public int getRadius() {
			return this.radius;
		}

		public int getHeight() {
			return this.height;
		}

		public boolean isGuarded() {
			return this.guarded;
		}

		public AABB getTopBoundingBox() {
			return this.topBoundingBox;
		}

		public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
			Builder<T, T> builder = ImmutableMap.builder();
			builder.put(dynamicOps.createString("centerX"), dynamicOps.createInt(this.centerX));
			builder.put(dynamicOps.createString("centerZ"), dynamicOps.createInt(this.centerZ));
			builder.put(dynamicOps.createString("radius"), dynamicOps.createInt(this.radius));
			builder.put(dynamicOps.createString("height"), dynamicOps.createInt(this.height));
			builder.put(dynamicOps.createString("guarded"), dynamicOps.createBoolean(this.guarded));
			return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
		}

		public static <T> SpikeFeature.EndSpike deserialize(Dynamic<T> dynamic) {
			return new SpikeFeature.EndSpike(
				dynamic.get("centerX").asInt(0),
				dynamic.get("centerZ").asInt(0),
				dynamic.get("radius").asInt(0),
				dynamic.get("height").asInt(0),
				dynamic.get("guarded").asBoolean(false)
			);
		}
	}

	static class SpikeCacheLoader extends CacheLoader<Long, List<SpikeFeature.EndSpike>> {
		private SpikeCacheLoader() {
		}

		public List<SpikeFeature.EndSpike> load(Long long_) {
			List<Integer> list = (List<Integer>)IntStream.range(0, 10).boxed().collect(Collectors.toList());
			Collections.shuffle(list, new Random(long_));
			List<SpikeFeature.EndSpike> list2 = Lists.<SpikeFeature.EndSpike>newArrayList();

			for (int i = 0; i < 10; i++) {
				int j = Mth.floor(42.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 10) * (double)i)));
				int k = Mth.floor(42.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 10) * (double)i)));
				int l = (Integer)list.get(i);
				int m = 2 + l / 3;
				int n = 76 + l * 3;
				boolean bl = l == 1 || l == 2;
				list2.add(new SpikeFeature.EndSpike(j, k, m, n, bl));
			}

			return list2;
		}
	}
}
