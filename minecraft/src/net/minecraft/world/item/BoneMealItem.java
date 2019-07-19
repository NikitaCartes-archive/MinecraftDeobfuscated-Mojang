package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BoneMealItem extends Item {
	public BoneMealItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
		if (growCrop(useOnContext.getItemInHand(), level, blockPos)) {
			if (!level.isClientSide) {
				level.levelEvent(2005, blockPos, 0);
			}

			return InteractionResult.SUCCESS;
		} else {
			BlockState blockState = level.getBlockState(blockPos);
			boolean bl = blockState.isFaceSturdy(level, blockPos, useOnContext.getClickedFace());
			if (bl && growWaterPlant(useOnContext.getItemInHand(), level, blockPos2, useOnContext.getClickedFace())) {
				if (!level.isClientSide) {
					level.levelEvent(2005, blockPos2, 0);
				}

				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.PASS;
			}
		}
	}

	public static boolean growCrop(ItemStack itemStack, Level level, BlockPos blockPos) {
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.getBlock() instanceof BonemealableBlock) {
			BonemealableBlock bonemealableBlock = (BonemealableBlock)blockState.getBlock();
			if (bonemealableBlock.isValidBonemealTarget(level, blockPos, blockState, level.isClientSide)) {
				if (!level.isClientSide) {
					if (bonemealableBlock.isBonemealSuccess(level, level.random, blockPos, blockState)) {
						bonemealableBlock.performBonemeal(level, level.random, blockPos, blockState);
					}

					itemStack.shrink(1);
				}

				return true;
			}
		}

		return false;
	}

	public static boolean growWaterPlant(ItemStack itemStack, Level level, BlockPos blockPos, @Nullable Direction direction) {
		if (level.getBlockState(blockPos).getBlock() == Blocks.WATER && level.getFluidState(blockPos).getAmount() == 8) {
			if (!level.isClientSide) {
				label79:
				for (int i = 0; i < 128; i++) {
					BlockPos blockPos2 = blockPos;
					Biome biome = level.getBiome(blockPos);
					BlockState blockState = Blocks.SEAGRASS.defaultBlockState();

					for (int j = 0; j < i / 16; j++) {
						blockPos2 = blockPos2.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
						biome = level.getBiome(blockPos2);
						if (level.getBlockState(blockPos2).isCollisionShapeFullBlock(level, blockPos2)) {
							continue label79;
						}
					}

					if (biome == Biomes.WARM_OCEAN || biome == Biomes.DEEP_WARM_OCEAN) {
						if (i == 0 && direction != null && direction.getAxis().isHorizontal()) {
							blockState = BlockTags.WALL_CORALS.getRandomElement(level.random).defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, direction);
						} else if (random.nextInt(4) == 0) {
							blockState = BlockTags.UNDERWATER_BONEMEALS.getRandomElement(random).defaultBlockState();
						}
					}

					if (blockState.getBlock().is(BlockTags.WALL_CORALS)) {
						for (int jx = 0; !blockState.canSurvive(level, blockPos2) && jx < 4; jx++) {
							blockState = blockState.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
						}
					}

					if (blockState.canSurvive(level, blockPos2)) {
						BlockState blockState2 = level.getBlockState(blockPos2);
						if (blockState2.getBlock() == Blocks.WATER && level.getFluidState(blockPos2).getAmount() == 8) {
							level.setBlock(blockPos2, blockState, 3);
						} else if (blockState2.getBlock() == Blocks.SEAGRASS && random.nextInt(10) == 0) {
							((BonemealableBlock)Blocks.SEAGRASS).performBonemeal(level, random, blockPos2, blockState2);
						}
					}
				}

				itemStack.shrink(1);
			}

			return true;
		} else {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public static void addGrowthParticles(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		if (i == 0) {
			i = 15;
		}

		BlockState blockState = levelAccessor.getBlockState(blockPos);
		if (!blockState.isAir()) {
			for (int j = 0; j < i; j++) {
				double d = random.nextGaussian() * 0.02;
				double e = random.nextGaussian() * 0.02;
				double f = random.nextGaussian() * 0.02;
				levelAccessor.addParticle(
					ParticleTypes.HAPPY_VILLAGER,
					(double)((float)blockPos.getX() + random.nextFloat()),
					(double)blockPos.getY() + (double)random.nextFloat() * blockState.getShape(levelAccessor, blockPos).max(Direction.Axis.Y),
					(double)((float)blockPos.getZ() + random.nextFloat()),
					d,
					e,
					f
				);
			}
		}
	}
}
