package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.AABB;

public class SpikeFeature extends Feature<SpikeConfiguration> {
	public static final int NUMBER_OF_SPIKES = 10;
	private static final int SPIKE_DISTANCE = 42;
	private static final LoadingCache<Long, List<SpikeFeature.EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder()
		.expireAfterWrite(5L, TimeUnit.MINUTES)
		.build(new SpikeFeature.SpikeCacheLoader());

	public SpikeFeature(Codec<SpikeConfiguration> codec) {
		super(codec);
	}

	public static List<SpikeFeature.EndSpike> getSpikesForLevel(WorldGenLevel worldGenLevel) {
		Random random = new Random(worldGenLevel.getSeed());
		long l = random.nextLong() & 65535L;
		return SPIKE_CACHE.getUnchecked(l);
	}

	@Override
	public boolean place(FeaturePlaceContext<SpikeConfiguration> featurePlaceContext) {
		SpikeConfiguration spikeConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		Random random = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		List<SpikeFeature.EndSpike> list = spikeConfiguration.getSpikes();
		if (list.isEmpty()) {
			list = getSpikesForLevel(worldGenLevel);
		}

		for (SpikeFeature.EndSpike endSpike : list) {
			if (endSpike.isCenterWithinChunk(blockPos)) {
				this.placeSpike(worldGenLevel, random, spikeConfiguration, endSpike);
			}
		}

		return true;
	}

	private void placeSpike(ServerLevelAccessor serverLevelAccessor, Random random, SpikeConfiguration spikeConfiguration, SpikeFeature.EndSpike endSpike) {
		int i = endSpike.getRadius();

		for (BlockPos blockPos : BlockPos.betweenClosed(
			new BlockPos(endSpike.getCenterX() - i, serverLevelAccessor.getMinBuildHeight(), endSpike.getCenterZ() - i),
			new BlockPos(endSpike.getCenterX() + i, endSpike.getHeight() + 10, endSpike.getCenterZ() + i)
		)) {
			if (blockPos.distSqr((double)endSpike.getCenterX(), (double)blockPos.getY(), (double)endSpike.getCenterZ(), false) <= (double)(i * i + 1)
				&& blockPos.getY() < endSpike.getHeight()) {
				this.setBlock(serverLevelAccessor, blockPos, Blocks.OBSIDIAN.defaultBlockState());
			} else if (blockPos.getY() > 65) {
				this.setBlock(serverLevelAccessor, blockPos, Blocks.AIR.defaultBlockState());
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
							this.setBlock(serverLevelAccessor, mutableBlockPos.set(endSpike.getCenterX() + m, endSpike.getHeight() + o, endSpike.getCenterZ() + n), blockState);
						}
					}
				}
			}
		}

		EndCrystal endCrystal = EntityType.END_CRYSTAL.create(serverLevelAccessor.getLevel());
		endCrystal.setBeamTarget(spikeConfiguration.getCrystalBeamTarget());
		endCrystal.setInvulnerable(spikeConfiguration.isCrystalInvulnerable());
		endCrystal.moveTo(
			(double)endSpike.getCenterX() + 0.5, (double)(endSpike.getHeight() + 1), (double)endSpike.getCenterZ() + 0.5, random.nextFloat() * 360.0F, 0.0F
		);
		serverLevelAccessor.addFreshEntity(endCrystal);
		this.setBlock(serverLevelAccessor, new BlockPos(endSpike.getCenterX(), endSpike.getHeight(), endSpike.getCenterZ()), Blocks.BEDROCK.defaultBlockState());
	}

	public static class EndSpike {
		public static final Codec<SpikeFeature.EndSpike> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("centerX").orElse(0).forGetter(endSpike -> endSpike.centerX),
						Codec.INT.fieldOf("centerZ").orElse(0).forGetter(endSpike -> endSpike.centerZ),
						Codec.INT.fieldOf("radius").orElse(0).forGetter(endSpike -> endSpike.radius),
						Codec.INT.fieldOf("height").orElse(0).forGetter(endSpike -> endSpike.height),
						Codec.BOOL.fieldOf("guarded").orElse(false).forGetter(endSpike -> endSpike.guarded)
					)
					.apply(instance, SpikeFeature.EndSpike::new)
		);
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
			this.topBoundingBox = new AABB((double)(i - k), (double)DimensionType.MIN_Y, (double)(j - k), (double)(i + k), (double)DimensionType.MAX_Y, (double)(j + k));
		}

		public boolean isCenterWithinChunk(BlockPos blockPos) {
			return SectionPos.blockToSectionCoord(blockPos.getX()) == SectionPos.blockToSectionCoord(this.centerX)
				&& SectionPos.blockToSectionCoord(blockPos.getZ()) == SectionPos.blockToSectionCoord(this.centerZ);
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
	}

	static class SpikeCacheLoader extends CacheLoader<Long, List<SpikeFeature.EndSpike>> {
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
