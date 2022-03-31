package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;

public class SculkPatchFeature extends Feature<SculkPatchConfiguration> {
	public SculkPatchFeature(Codec<SculkPatchConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SculkPatchConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		if (!this.canSpreadFrom(worldGenLevel, blockPos)) {
			return false;
		} else {
			SculkPatchConfiguration sculkPatchConfiguration = featurePlaceContext.config();
			Random random = featurePlaceContext.random();
			SculkSpreader sculkSpreader = SculkSpreader.createWorldGenSpreader();
			int i = sculkPatchConfiguration.spreadRounds() + sculkPatchConfiguration.growthRounds();

			for (int j = 0; j < i; j++) {
				for (int k = 0; k < sculkPatchConfiguration.chargeCount(); k++) {
					sculkSpreader.addCursors(blockPos, sculkPatchConfiguration.amountPerCharge());
				}

				boolean bl = j < sculkPatchConfiguration.spreadRounds();

				for (int l = 0; l < sculkPatchConfiguration.spreadAttempts(); l++) {
					sculkSpreader.updateCursors(worldGenLevel, blockPos, random, bl);
				}

				sculkSpreader.clear();
			}

			BlockPos blockPos2 = blockPos.below();
			if (random.nextFloat() <= sculkPatchConfiguration.catalystChance()
				&& worldGenLevel.getBlockState(blockPos2).isCollisionShapeFullBlock(worldGenLevel, blockPos2)) {
				worldGenLevel.setBlock(blockPos, Blocks.SCULK_CATALYST.defaultBlockState(), 3);
			}

			int k = sculkPatchConfiguration.extraRareGrowths().sample(random);

			for (int l = 0; l < k; l++) {
				BlockPos blockPos3 = blockPos.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
				if (worldGenLevel.getBlockState(blockPos3).isAir()
					&& worldGenLevel.getBlockState(blockPos3.below()).isFaceSturdy(worldGenLevel, blockPos3.below(), Direction.UP)) {
					worldGenLevel.setBlock(blockPos3, Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, Boolean.valueOf(true)), 3);
				}
			}

			return true;
		}
	}

	private boolean canSpreadFrom(LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		if (blockState.getBlock() instanceof SculkBehaviour) {
			return true;
		} else {
			return !blockState.isAir() && (!blockState.is(Blocks.WATER) || !blockState.getFluidState().isSource())
				? false
				: Direction.stream()
					.map(blockPos::relative)
					.anyMatch(blockPosx -> levelAccessor.getBlockState(blockPosx).isCollisionShapeFullBlock(levelAccessor, blockPosx));
		}
	}
}
