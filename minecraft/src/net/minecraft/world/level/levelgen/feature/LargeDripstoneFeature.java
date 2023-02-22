package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;

public class LargeDripstoneFeature extends Feature<LargeDripstoneConfiguration> {
	public LargeDripstoneFeature(Codec<LargeDripstoneConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		LargeDripstoneConfiguration largeDripstoneConfiguration = featurePlaceContext.config();
		RandomSource randomSource = featurePlaceContext.random();
		if (!DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos)) {
			return false;
		} else {
			Optional<Column> optional = Column.scan(
				worldGenLevel, blockPos, largeDripstoneConfiguration.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isDripstoneBaseOrLava
			);
			if (optional.isPresent() && optional.get() instanceof Column.Range) {
				Column.Range range = (Column.Range)optional.get();
				if (range.height() < 4) {
					return false;
				} else {
					int i = (int)((float)range.height() * largeDripstoneConfiguration.maxColumnRadiusToCaveHeightRatio);
					int j = Mth.clamp(i, largeDripstoneConfiguration.columnRadius.getMinValue(), largeDripstoneConfiguration.columnRadius.getMaxValue());
					int k = Mth.randomBetweenInclusive(randomSource, largeDripstoneConfiguration.columnRadius.getMinValue(), j);
					LargeDripstoneFeature.LargeDripstone largeDripstone = makeDripstone(
						blockPos.atY(range.ceiling() - 1), false, randomSource, k, largeDripstoneConfiguration.stalactiteBluntness, largeDripstoneConfiguration.heightScale
					);
					LargeDripstoneFeature.LargeDripstone largeDripstone2 = makeDripstone(
						blockPos.atY(range.floor() + 1), true, randomSource, k, largeDripstoneConfiguration.stalagmiteBluntness, largeDripstoneConfiguration.heightScale
					);
					LargeDripstoneFeature.WindOffsetter windOffsetter;
					if (largeDripstone.isSuitableForWind(largeDripstoneConfiguration) && largeDripstone2.isSuitableForWind(largeDripstoneConfiguration)) {
						windOffsetter = new LargeDripstoneFeature.WindOffsetter(blockPos.getY(), randomSource, largeDripstoneConfiguration.windSpeed);
					} else {
						windOffsetter = LargeDripstoneFeature.WindOffsetter.noWind();
					}

					boolean bl = largeDripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldGenLevel, windOffsetter);
					boolean bl2 = largeDripstone2.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldGenLevel, windOffsetter);
					if (bl) {
						largeDripstone.placeBlocks(worldGenLevel, randomSource, windOffsetter);
					}

					if (bl2) {
						largeDripstone2.placeBlocks(worldGenLevel, randomSource, windOffsetter);
					}

					return true;
				}
			} else {
				return false;
			}
		}
	}

	private static LargeDripstoneFeature.LargeDripstone makeDripstone(
		BlockPos blockPos, boolean bl, RandomSource randomSource, int i, FloatProvider floatProvider, FloatProvider floatProvider2
	) {
		return new LargeDripstoneFeature.LargeDripstone(blockPos, bl, i, (double)floatProvider.sample(randomSource), (double)floatProvider2.sample(randomSource));
	}

	private void placeDebugMarkers(WorldGenLevel worldGenLevel, BlockPos blockPos, Column.Range range, LargeDripstoneFeature.WindOffsetter windOffsetter) {
		worldGenLevel.setBlock(windOffsetter.offset(blockPos.atY(range.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
		worldGenLevel.setBlock(windOffsetter.offset(blockPos.atY(range.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

		for (BlockPos.MutableBlockPos mutableBlockPos = blockPos.atY(range.floor() + 2).mutable();
			mutableBlockPos.getY() < range.ceiling() - 1;
			mutableBlockPos.move(Direction.UP)
		) {
			BlockPos blockPos2 = windOffsetter.offset(mutableBlockPos);
			if (DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos2) || worldGenLevel.getBlockState(blockPos2).is(Blocks.DRIPSTONE_BLOCK)) {
				worldGenLevel.setBlock(blockPos2, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
			}
		}
	}

	static final class LargeDripstone {
		private BlockPos root;
		private final boolean pointingUp;
		private int radius;
		private final double bluntness;
		private final double scale;

		LargeDripstone(BlockPos blockPos, boolean bl, int i, double d, double e) {
			this.root = blockPos;
			this.pointingUp = bl;
			this.radius = i;
			this.bluntness = d;
			this.scale = e;
		}

		private int getHeight() {
			return this.getHeightAtRadius(0.0F);
		}

		private int getMinY() {
			return this.pointingUp ? this.root.getY() : this.root.getY() - this.getHeight();
		}

		private int getMaxY() {
			return !this.pointingUp ? this.root.getY() : this.root.getY() + this.getHeight();
		}

		boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel worldGenLevel, LargeDripstoneFeature.WindOffsetter windOffsetter) {
			while (this.radius > 1) {
				BlockPos.MutableBlockPos mutableBlockPos = this.root.mutable();
				int i = Math.min(10, this.getHeight());

				for (int j = 0; j < i; j++) {
					if (worldGenLevel.getBlockState(mutableBlockPos).is(Blocks.LAVA)) {
						return false;
					}

					if (DripstoneUtils.isCircleMostlyEmbeddedInStone(worldGenLevel, windOffsetter.offset(mutableBlockPos), this.radius)) {
						this.root = mutableBlockPos;
						return true;
					}

					mutableBlockPos.move(this.pointingUp ? Direction.DOWN : Direction.UP);
				}

				this.radius /= 2;
			}

			return false;
		}

		private int getHeightAtRadius(float f) {
			return (int)DripstoneUtils.getDripstoneHeight((double)f, (double)this.radius, this.scale, this.bluntness);
		}

		void placeBlocks(WorldGenLevel worldGenLevel, RandomSource randomSource, LargeDripstoneFeature.WindOffsetter windOffsetter) {
			for (int i = -this.radius; i <= this.radius; i++) {
				for (int j = -this.radius; j <= this.radius; j++) {
					float f = Mth.sqrt((float)(i * i + j * j));
					if (!(f > (float)this.radius)) {
						int k = this.getHeightAtRadius(f);
						if (k > 0) {
							if ((double)randomSource.nextFloat() < 0.2) {
								k = (int)((float)k * Mth.randomBetween(randomSource, 0.8F, 1.0F));
							}

							BlockPos.MutableBlockPos mutableBlockPos = this.root.offset(i, 0, j).mutable();
							boolean bl = false;
							int l = this.pointingUp ? worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mutableBlockPos.getX(), mutableBlockPos.getZ()) : Integer.MAX_VALUE;

							for (int m = 0; m < k && mutableBlockPos.getY() < l; m++) {
								BlockPos blockPos = windOffsetter.offset(mutableBlockPos);
								if (DripstoneUtils.isEmptyOrWaterOrLava(worldGenLevel, blockPos)) {
									bl = true;
									Block block = Blocks.DRIPSTONE_BLOCK;
									worldGenLevel.setBlock(blockPos, block.defaultBlockState(), 2);
								} else if (bl && worldGenLevel.getBlockState(blockPos).is(BlockTags.BASE_STONE_OVERWORLD)) {
									break;
								}

								mutableBlockPos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
							}
						}
					}
				}
			}
		}

		boolean isSuitableForWind(LargeDripstoneConfiguration largeDripstoneConfiguration) {
			return this.radius >= largeDripstoneConfiguration.minRadiusForWind && this.bluntness >= (double)largeDripstoneConfiguration.minBluntnessForWind;
		}
	}

	static final class WindOffsetter {
		private final int originY;
		@Nullable
		private final Vec3 windSpeed;

		WindOffsetter(int i, RandomSource randomSource, FloatProvider floatProvider) {
			this.originY = i;
			float f = floatProvider.sample(randomSource);
			float g = Mth.randomBetween(randomSource, 0.0F, (float) Math.PI);
			this.windSpeed = new Vec3((double)(Mth.cos(g) * f), 0.0, (double)(Mth.sin(g) * f));
		}

		private WindOffsetter() {
			this.originY = 0;
			this.windSpeed = null;
		}

		static LargeDripstoneFeature.WindOffsetter noWind() {
			return new LargeDripstoneFeature.WindOffsetter();
		}

		BlockPos offset(BlockPos blockPos) {
			if (this.windSpeed == null) {
				return blockPos;
			} else {
				int i = this.originY - blockPos.getY();
				Vec3 vec3 = this.windSpeed.scale((double)i);
				return blockPos.offset(Mth.floor(vec3.x), 0, Mth.floor(vec3.z));
			}
		}
	}
}
