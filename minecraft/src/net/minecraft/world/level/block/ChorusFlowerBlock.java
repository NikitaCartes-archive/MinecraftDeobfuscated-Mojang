package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ChorusFlowerBlock extends Block {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
	private final ChorusPlantBlock plant;

	protected ChorusFlowerBlock(ChorusPlantBlock chorusPlantBlock, BlockBehaviour.Properties properties) {
		super(properties);
		this.plant = chorusPlantBlock;
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return (Integer)blockState.getValue(AGE) < 5;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		BlockPos blockPos2 = blockPos.above();
		if (serverLevel.isEmptyBlock(blockPos2) && blockPos2.getY() < 256) {
			int i = (Integer)blockState.getValue(AGE);
			if (i < 5) {
				boolean bl = false;
				boolean bl2 = false;
				BlockState blockState2 = serverLevel.getBlockState(blockPos.below());
				Block block = blockState2.getBlock();
				if (block == Blocks.END_STONE) {
					bl = true;
				} else if (block == this.plant) {
					int j = 1;

					for (int k = 0; k < 4; k++) {
						Block block2 = serverLevel.getBlockState(blockPos.below(j + 1)).getBlock();
						if (block2 != this.plant) {
							if (block2 == Blocks.END_STONE) {
								bl2 = true;
							}
							break;
						}

						j++;
					}

					if (j < 2 || j <= random.nextInt(bl2 ? 5 : 4)) {
						bl = true;
					}
				} else if (blockState2.isAir()) {
					bl = true;
				}

				if (bl && allNeighborsEmpty(serverLevel, blockPos2, null) && serverLevel.isEmptyBlock(blockPos.above(2))) {
					serverLevel.setBlock(blockPos, this.plant.getStateForPlacement(serverLevel, blockPos), 2);
					this.placeGrownFlower(serverLevel, blockPos2, i);
				} else if (i < 4) {
					int j = random.nextInt(4);
					if (bl2) {
						j++;
					}

					boolean bl3 = false;

					for (int l = 0; l < j; l++) {
						Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
						BlockPos blockPos3 = blockPos.relative(direction);
						if (serverLevel.isEmptyBlock(blockPos3)
							&& serverLevel.isEmptyBlock(blockPos3.below())
							&& allNeighborsEmpty(serverLevel, blockPos3, direction.getOpposite())) {
							this.placeGrownFlower(serverLevel, blockPos3, i + 1);
							bl3 = true;
						}
					}

					if (bl3) {
						serverLevel.setBlock(blockPos, this.plant.getStateForPlacement(serverLevel, blockPos), 2);
					} else {
						this.placeDeadFlower(serverLevel, blockPos);
					}
				} else {
					this.placeDeadFlower(serverLevel, blockPos);
				}
			}
		}
	}

	private void placeGrownFlower(Level level, BlockPos blockPos, int i) {
		level.setBlock(blockPos, this.defaultBlockState().setValue(AGE, Integer.valueOf(i)), 2);
		level.levelEvent(1033, blockPos, 0);
	}

	private void placeDeadFlower(Level level, BlockPos blockPos) {
		level.setBlock(blockPos, this.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
		level.levelEvent(1034, blockPos, 0);
	}

	private static boolean allNeighborsEmpty(LevelReader levelReader, BlockPos blockPos, @Nullable Direction direction) {
		for (Direction direction2 : Direction.Plane.HORIZONTAL) {
			if (direction2 != direction && !levelReader.isEmptyBlock(blockPos.relative(direction2))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction != Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean isUnstable() {
		return true;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.below());
		Block block = blockState2.getBlock();
		if (block != this.plant && block != Blocks.END_STONE) {
			if (!blockState2.isAir()) {
				return false;
			} else {
				boolean bl = false;

				for (Direction direction : Direction.Plane.HORIZONTAL) {
					BlockState blockState3 = levelReader.getBlockState(blockPos.relative(direction));
					if (blockState3.getBlock() == this.plant) {
						if (bl) {
							return false;
						}

						bl = true;
					} else if (!blockState3.isAir()) {
						return false;
					}
				}

				return bl;
			}
		} else {
			return true;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	public static void generatePlant(LevelAccessor levelAccessor, BlockPos blockPos, Random random, int i) {
		levelAccessor.setBlock(blockPos, ((ChorusPlantBlock)Blocks.CHORUS_PLANT).getStateForPlacement(levelAccessor, blockPos), 2);
		growTreeRecursive(levelAccessor, blockPos, random, blockPos, i, 0);
	}

	private static void growTreeRecursive(LevelAccessor levelAccessor, BlockPos blockPos, Random random, BlockPos blockPos2, int i, int j) {
		ChorusPlantBlock chorusPlantBlock = (ChorusPlantBlock)Blocks.CHORUS_PLANT;
		int k = random.nextInt(4) + 1;
		if (j == 0) {
			k++;
		}

		for (int l = 0; l < k; l++) {
			BlockPos blockPos3 = blockPos.above(l + 1);
			if (!allNeighborsEmpty(levelAccessor, blockPos3, null)) {
				return;
			}

			levelAccessor.setBlock(blockPos3, chorusPlantBlock.getStateForPlacement(levelAccessor, blockPos3), 2);
			levelAccessor.setBlock(blockPos3.below(), chorusPlantBlock.getStateForPlacement(levelAccessor, blockPos3.below()), 2);
		}

		boolean bl = false;
		if (j < 4) {
			int m = random.nextInt(4);
			if (j == 0) {
				m++;
			}

			for (int n = 0; n < m; n++) {
				Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
				BlockPos blockPos4 = blockPos.above(k).relative(direction);
				if (Math.abs(blockPos4.getX() - blockPos2.getX()) < i
					&& Math.abs(blockPos4.getZ() - blockPos2.getZ()) < i
					&& levelAccessor.isEmptyBlock(blockPos4)
					&& levelAccessor.isEmptyBlock(blockPos4.below())
					&& allNeighborsEmpty(levelAccessor, blockPos4, direction.getOpposite())) {
					bl = true;
					levelAccessor.setBlock(blockPos4, chorusPlantBlock.getStateForPlacement(levelAccessor, blockPos4), 2);
					levelAccessor.setBlock(
						blockPos4.relative(direction.getOpposite()), chorusPlantBlock.getStateForPlacement(levelAccessor, blockPos4.relative(direction.getOpposite())), 2
					);
					growTreeRecursive(levelAccessor, blockPos4, random, blockPos2, i, j + 1);
				}
			}
		}

		if (!bl) {
			levelAccessor.setBlock(blockPos.above(k), Blocks.CHORUS_FLOWER.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
		}
	}

	@Override
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		if (projectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES)) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			level.destroyBlock(blockPos, true, projectile);
		}
	}
}
