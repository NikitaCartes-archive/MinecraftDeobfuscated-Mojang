package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.UniformFloat;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
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
		Random random = featurePlaceContext.random();
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
					int j = Mth.clamp(i, largeDripstoneConfiguration.columnRadius.getBaseValue(), largeDripstoneConfiguration.columnRadius.getMaxValue());
					int k = Mth.randomBetweenInclusive(random, largeDripstoneConfiguration.columnRadius.getBaseValue(), j);
					LargeDripstoneFeature.LargeDripstone largeDripstone = makeDripstone(
						blockPos.atY(range.ceiling() - 1), false, random, k, largeDripstoneConfiguration.stalactiteBluntness, largeDripstoneConfiguration.heightScale
					);
					LargeDripstoneFeature.LargeDripstone largeDripstone2 = makeDripstone(
						blockPos.atY(range.floor() + 1), true, random, k, largeDripstoneConfiguration.stalagmiteBluntness, largeDripstoneConfiguration.heightScale
					);
					LargeDripstoneFeature.WindOffsetter windOffsetter;
					if (largeDripstone.isSuitableForWind(largeDripstoneConfiguration) && largeDripstone2.isSuitableForWind(largeDripstoneConfiguration)) {
						windOffsetter = new LargeDripstoneFeature.WindOffsetter(blockPos.getY(), random, largeDripstoneConfiguration.windSpeed);
					} else {
						windOffsetter = LargeDripstoneFeature.WindOffsetter.noWind();
					}

					boolean bl = largeDripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldGenLevel, windOffsetter);
					boolean bl2 = largeDripstone2.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldGenLevel, windOffsetter);
					if (bl && largeDripstone.getMinY() > 0) {
						largeDripstone.placeBlocks(worldGenLevel, random, windOffsetter);
					}

					if (bl2 && largeDripstone2.getMaxY() < 55) {
						largeDripstone2.placeBlocks(worldGenLevel, random, windOffsetter);
					}

					return true;
				}
			} else {
				return false;
			}
		}
	}

	private static LargeDripstoneFeature.LargeDripstone makeDripstone(
		BlockPos blockPos, boolean bl, Random random, int i, UniformFloat uniformFloat, UniformFloat uniformFloat2
	) {
		return new LargeDripstoneFeature.LargeDripstone(blockPos, bl, i, (double)uniformFloat.sample(random), (double)uniformFloat2.sample(random));
	}

	static final class LargeDripstone {
		private BlockPos root;
		private final boolean pointingUp;
		private int radius;
		private final double bluntness;
		private final double scale;

		private LargeDripstone(BlockPos blockPos, boolean bl, int i, double d, double e) {
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

		private boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel worldGenLevel, LargeDripstoneFeature.WindOffsetter windOffsetter) {
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

		private void placeBlocks(WorldGenLevel worldGenLevel, Random random, LargeDripstoneFeature.WindOffsetter windOffsetter) {
			for (int i = -this.radius; i <= this.radius; i++) {
				for (int j = -this.radius; j <= this.radius; j++) {
					float f = Mth.sqrt((float)(i * i + j * j));
					if (!(f > (float)this.radius)) {
						int k = this.getHeightAtRadius(f);
						if (k > 0) {
							if ((double)random.nextFloat() < 0.2) {
								k = (int)((float)k * Mth.randomBetween(random, 0.8F, 1.0F));
							}

							BlockPos.MutableBlockPos mutableBlockPos = this.root.offset(i, 0, j).mutable();
							boolean bl = false;

							for (int l = 0; l < k; l++) {
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

		private boolean isSuitableForWind(LargeDripstoneConfiguration largeDripstoneConfiguration) {
			return this.radius >= largeDripstoneConfiguration.minRadiusForWind && this.bluntness >= (double)largeDripstoneConfiguration.minBluntnessForWind;
		}
	}

	static final class WindOffsetter {
		private final int originY;
		@Nullable
		private final Vec3 windSpeed;

		private WindOffsetter(int i, Random random, UniformFloat uniformFloat) {
			this.originY = i;
			float f = uniformFloat.sample(random);
			float g = Mth.randomBetween(random, 0.0F, (float) Math.PI);
			this.windSpeed = new Vec3((double)(Mth.cos(g) * f), 0.0, (double)(Mth.sin(g) * f));
		}

		private WindOffsetter() {
			this.originY = 0;
			this.windSpeed = null;
		}

		private static LargeDripstoneFeature.WindOffsetter noWind() {
			return new LargeDripstoneFeature.WindOffsetter();
		}

		private BlockPos offset(BlockPos blockPos) {
			if (this.windSpeed == null) {
				return blockPos;
			} else {
				int i = this.originY - blockPos.getY();
				Vec3 vec3 = this.windSpeed.scale((double)i);
				return blockPos.offset(vec3.x, 0.0, vec3.z);
			}
		}
	}
}
