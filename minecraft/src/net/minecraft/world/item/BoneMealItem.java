package net.minecraft.world.item;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
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

			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			BlockState blockState = level.getBlockState(blockPos);
			boolean bl = blockState.isFaceSturdy(level, blockPos, useOnContext.getClickedFace());
			if (bl && growWaterPlant(useOnContext.getItemInHand(), level, blockPos2, useOnContext.getClickedFace())) {
				if (!level.isClientSide) {
					level.levelEvent(2005, blockPos2, 0);
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
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
				if (level instanceof ServerLevel) {
					if (bonemealableBlock.isBonemealSuccess(level, level.random, blockPos, blockState)) {
						bonemealableBlock.performBonemeal((ServerLevel)level, level.random, blockPos, blockState);
					}

					itemStack.shrink(1);
				}

				return true;
			}
		}

		return false;
	}

	public static boolean growWaterPlant(ItemStack itemStack, Level level, BlockPos blockPos, @Nullable Direction direction) {
		if (level.getBlockState(blockPos).is(Blocks.WATER) && level.getFluidState(blockPos).getAmount() == 8) {
			if (!(level instanceof ServerLevel)) {
				return true;
			} else {
				Random random = level.getRandom();

				label80:
				for (int i = 0; i < 128; i++) {
					BlockPos blockPos2 = blockPos;
					BlockState blockState = Blocks.SEAGRASS.defaultBlockState();

					for (int j = 0; j < i / 16; j++) {
						blockPos2 = blockPos2.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
						if (level.getBlockState(blockPos2).isCollisionShapeFullBlock(level, blockPos2)) {
							continue label80;
						}
					}

					Optional<ResourceKey<Biome>> optional = level.getBiomeName(blockPos2);
					if (Objects.equals(optional, Optional.of(Biomes.WARM_OCEAN)) || Objects.equals(optional, Optional.of(Biomes.DEEP_WARM_OCEAN))) {
						if (i == 0 && direction != null && direction.getAxis().isHorizontal()) {
							blockState = BlockTags.WALL_CORALS.getRandomElement(level.random).defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, direction);
						} else if (random.nextInt(4) == 0) {
							blockState = BlockTags.UNDERWATER_BONEMEALS.getRandomElement(random).defaultBlockState();
						}
					}

					if (blockState.is(BlockTags.WALL_CORALS)) {
						for (int k = 0; !blockState.canSurvive(level, blockPos2) && k < 4; k++) {
							blockState = blockState.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
						}
					}

					if (blockState.canSurvive(level, blockPos2)) {
						BlockState blockState2 = level.getBlockState(blockPos2);
						if (blockState2.is(Blocks.WATER) && level.getFluidState(blockPos2).getAmount() == 8) {
							level.setBlock(blockPos2, blockState, 3);
						} else if (blockState2.is(Blocks.SEAGRASS) && random.nextInt(10) == 0) {
							((BonemealableBlock)Blocks.SEAGRASS).performBonemeal((ServerLevel)level, random, blockPos2, blockState2);
						}
					}
				}

				itemStack.shrink(1);
				return true;
			}
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
			double d = 0.5;
			double e;
			if (blockState.is(Blocks.WATER)) {
				i *= 3;
				e = 1.0;
				d = 3.0;
			} else if (blockState.isSolidRender(levelAccessor, blockPos)) {
				blockPos = blockPos.above();
				i *= 3;
				d = 3.0;
				e = 1.0;
			} else {
				e = blockState.getShape(levelAccessor, blockPos).max(Direction.Axis.Y);
			}

			levelAccessor.addParticle(
				ParticleTypes.HAPPY_VILLAGER, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0
			);
			Random random = levelAccessor.getRandom();

			for (int j = 0; j < i; j++) {
				double f = random.nextGaussian() * 0.02;
				double g = random.nextGaussian() * 0.02;
				double h = random.nextGaussian() * 0.02;
				double k = 0.5 - d;
				double l = (double)blockPos.getX() + k + random.nextDouble() * d * 2.0;
				double m = (double)blockPos.getY() + random.nextDouble() * e;
				double n = (double)blockPos.getZ() + k + random.nextDouble() * d * 2.0;
				if (!levelAccessor.getBlockState(new BlockPos(l, m, n).below()).isAir()) {
					levelAccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, l, m, n, f, g, h);
				}
			}
		}
	}
}
