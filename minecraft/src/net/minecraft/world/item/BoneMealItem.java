package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class BoneMealItem extends Item {
	public static final int GRASS_SPREAD_WIDTH = 3;
	public static final int GRASS_SPREAD_HEIGHT = 1;
	public static final int GRASS_COUNT_MULTIPLIER = 3;

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
				useOnContext.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
				level.levelEvent(1505, blockPos, 15);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			BlockState blockState = level.getBlockState(blockPos);
			boolean bl = blockState.isFaceSturdy(level, blockPos, useOnContext.getClickedFace());
			if (bl && growWaterPlant(useOnContext.getItemInHand(), level, blockPos2, useOnContext.getClickedFace())) {
				if (!level.isClientSide) {
					useOnContext.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
					level.levelEvent(1505, blockPos2, 15);
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			} else {
				return InteractionResult.PASS;
			}
		}
	}

	public static boolean growCrop(ItemStack itemStack, Level level, BlockPos blockPos) {
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.getBlock() instanceof BonemealableBlock bonemealableBlock && bonemealableBlock.isValidBonemealTarget(level, blockPos, blockState)) {
			if (level instanceof ServerLevel) {
				if (bonemealableBlock.isBonemealSuccess(level, level.random, blockPos, blockState)) {
					bonemealableBlock.performBonemeal((ServerLevel)level, level.random, blockPos, blockState);
				}

				itemStack.shrink(1);
			}

			return true;
		}

		return false;
	}

	public static boolean growWaterPlant(ItemStack itemStack, Level level, BlockPos blockPos, @Nullable Direction direction) {
		if (level.getBlockState(blockPos).is(Blocks.WATER) && level.getFluidState(blockPos).getAmount() == 8) {
			if (!(level instanceof ServerLevel)) {
				return true;
			} else {
				RandomSource randomSource = level.getRandom();

				label78:
				for (int i = 0; i < 128; i++) {
					BlockPos blockPos2 = blockPos;
					BlockState blockState = Blocks.SEAGRASS.defaultBlockState();

					for (int j = 0; j < i / 16; j++) {
						blockPos2 = blockPos2.offset(randomSource.nextInt(3) - 1, (randomSource.nextInt(3) - 1) * randomSource.nextInt(3) / 2, randomSource.nextInt(3) - 1);
						if (level.getBlockState(blockPos2).isCollisionShapeFullBlock(level, blockPos2)) {
							continue label78;
						}
					}

					Holder<Biome> holder = level.getBiome(blockPos2);
					if (holder.is(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL)) {
						if (i == 0 && direction != null && direction.getAxis().isHorizontal()) {
							blockState = (BlockState)BuiltInRegistries.BLOCK
								.getTag(BlockTags.WALL_CORALS)
								.flatMap(named -> named.getRandomElement(level.random))
								.map(holderx -> ((Block)holderx.value()).defaultBlockState())
								.orElse(blockState);
							if (blockState.hasProperty(BaseCoralWallFanBlock.FACING)) {
								blockState = blockState.setValue(BaseCoralWallFanBlock.FACING, direction);
							}
						} else if (randomSource.nextInt(4) == 0) {
							blockState = (BlockState)BuiltInRegistries.BLOCK
								.getTag(BlockTags.UNDERWATER_BONEMEALS)
								.flatMap(named -> named.getRandomElement(level.random))
								.map(holderx -> ((Block)holderx.value()).defaultBlockState())
								.orElse(blockState);
						}
					}

					if (blockState.is(BlockTags.WALL_CORALS, blockStateBase -> blockStateBase.hasProperty(BaseCoralWallFanBlock.FACING))) {
						for (int k = 0; !blockState.canSurvive(level, blockPos2) && k < 4; k++) {
							blockState = blockState.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(randomSource));
						}
					}

					if (blockState.canSurvive(level, blockPos2)) {
						BlockState blockState2 = level.getBlockState(blockPos2);
						if (blockState2.is(Blocks.WATER) && level.getFluidState(blockPos2).getAmount() == 8) {
							level.setBlock(blockPos2, blockState, 3);
						} else if (blockState2.is(Blocks.SEAGRASS) && randomSource.nextInt(10) == 0) {
							((BonemealableBlock)Blocks.SEAGRASS).performBonemeal((ServerLevel)level, randomSource, blockPos2, blockState2);
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

	public static void addGrowthParticles(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		if (levelAccessor.getBlockState(blockPos).getBlock() instanceof BonemealableBlock bonemealableBlock) {
			BlockPos blockPos2 = bonemealableBlock.getParticlePos(blockPos);
			switch (bonemealableBlock.getType()) {
				case NEIGHBOR_SPREADER:
					ParticleUtils.spawnParticles(levelAccessor, blockPos2, i * 3, 3.0, 1.0, false, ParticleTypes.HAPPY_VILLAGER);
					break;
				case GROWER:
					ParticleUtils.spawnParticleInBlock(levelAccessor, blockPos2, i, ParticleTypes.HAPPY_VILLAGER);
			}
		}
	}
}
