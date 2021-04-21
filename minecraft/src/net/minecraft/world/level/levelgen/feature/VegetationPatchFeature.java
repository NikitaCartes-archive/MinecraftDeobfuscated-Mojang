package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class VegetationPatchFeature extends Feature<VegetationPatchConfiguration> {
	public VegetationPatchFeature(Codec<VegetationPatchConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		VegetationPatchConfiguration vegetationPatchConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		Predicate<BlockState> predicate = getReplaceableTag(vegetationPatchConfiguration);
		int i = vegetationPatchConfiguration.xzRadius.sample(random) + 1;
		int j = vegetationPatchConfiguration.xzRadius.sample(random) + 1;
		Set<BlockPos> set = this.placeGroundPatch(worldGenLevel, vegetationPatchConfiguration, random, blockPos, predicate, i, j);
		this.distributeVegetation(featurePlaceContext, worldGenLevel, vegetationPatchConfiguration, random, set, i, j);
		return !set.isEmpty();
	}

	protected Set<BlockPos> placeGroundPatch(
		WorldGenLevel worldGenLevel,
		VegetationPatchConfiguration vegetationPatchConfiguration,
		Random random,
		BlockPos blockPos,
		Predicate<BlockState> predicate,
		int i,
		int j
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable();
		Direction direction = vegetationPatchConfiguration.surface.getDirection();
		Direction direction2 = direction.getOpposite();
		Set<BlockPos> set = new HashSet();

		for (int k = -i; k <= i; k++) {
			boolean bl = k == -i || k == i;

			for (int l = -j; l <= j; l++) {
				boolean bl2 = l == -j || l == j;
				boolean bl3 = bl || bl2;
				boolean bl4 = bl && bl2;
				boolean bl5 = bl3 && !bl4;
				if (!bl4
					&& (!bl5 || vegetationPatchConfiguration.extraEdgeColumnChance != 0.0F && !(random.nextFloat() > vegetationPatchConfiguration.extraEdgeColumnChance))) {
					mutableBlockPos.setWithOffset(blockPos, k, 0, l);

					for (int m = 0;
						worldGenLevel.isStateAtPosition(mutableBlockPos, BlockBehaviour.BlockStateBase::isAir) && m < vegetationPatchConfiguration.verticalRange;
						m++
					) {
						mutableBlockPos.move(direction);
					}

					for (int var25 = 0;
						worldGenLevel.isStateAtPosition(mutableBlockPos, blockStatex -> !blockStatex.isAir()) && var25 < vegetationPatchConfiguration.verticalRange;
						var25++
					) {
						mutableBlockPos.move(direction2);
					}

					mutableBlockPos2.setWithOffset(mutableBlockPos, vegetationPatchConfiguration.surface.getDirection());
					BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos2);
					if (worldGenLevel.isEmptyBlock(mutableBlockPos)
						&& blockState.isFaceSturdy(worldGenLevel, mutableBlockPos2, vegetationPatchConfiguration.surface.getDirection().getOpposite())) {
						int n = vegetationPatchConfiguration.depth.sample(random)
							+ (vegetationPatchConfiguration.extraBottomBlockChance > 0.0F && random.nextFloat() < vegetationPatchConfiguration.extraBottomBlockChance ? 1 : 0);
						BlockPos blockPos2 = mutableBlockPos2.immutable();
						boolean bl6 = this.placeGround(worldGenLevel, vegetationPatchConfiguration, predicate, random, mutableBlockPos2, n);
						if (bl6) {
							set.add(blockPos2);
						}
					}
				}
			}
		}

		return set;
	}

	protected void distributeVegetation(
		FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext,
		WorldGenLevel worldGenLevel,
		VegetationPatchConfiguration vegetationPatchConfiguration,
		Random random,
		Set<BlockPos> set,
		int i,
		int j
	) {
		for (BlockPos blockPos : set) {
			if (vegetationPatchConfiguration.vegetationChance > 0.0F && random.nextFloat() < vegetationPatchConfiguration.vegetationChance) {
				this.placeVegetation(worldGenLevel, vegetationPatchConfiguration, featurePlaceContext.chunkGenerator(), random, blockPos);
			}
		}
	}

	protected boolean placeVegetation(
		WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos
	) {
		return ((ConfiguredFeature)vegetationPatchConfiguration.vegetationFeature.get())
			.place(worldGenLevel, chunkGenerator, random, blockPos.relative(vegetationPatchConfiguration.surface.getDirection().getOpposite()));
	}

	protected boolean placeGround(
		WorldGenLevel worldGenLevel,
		VegetationPatchConfiguration vegetationPatchConfiguration,
		Predicate<BlockState> predicate,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		int i
	) {
		for (int j = 0; j < i; j++) {
			BlockState blockState = vegetationPatchConfiguration.groundState.getState(random, mutableBlockPos);
			BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
			if (!blockState.is(blockState2.getBlock())) {
				if (!predicate.test(blockState2)) {
					return j != 0;
				}

				worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
				mutableBlockPos.move(vegetationPatchConfiguration.surface.getDirection());
			}
		}

		return true;
	}

	private static Predicate<BlockState> getReplaceableTag(VegetationPatchConfiguration vegetationPatchConfiguration) {
		Tag<Block> tag = BlockTags.getAllTags().getTag(vegetationPatchConfiguration.replaceable);
		return tag == null ? blockState -> true : blockState -> blockState.is(tag);
	}
}
