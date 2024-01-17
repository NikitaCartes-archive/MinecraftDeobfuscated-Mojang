package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChorusFlowerBlock extends Block {
	public static final MapCodec<ChorusFlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("plant").forGetter(chorusFlowerBlock -> chorusFlowerBlock.plant), propertiesCodec())
				.apply(instance, ChorusFlowerBlock::new)
	);
	public static final int DEAD_AGE = 5;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
	protected static final VoxelShape BLOCK_SUPPORT_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
	private final Block plant;

	@Override
	public MapCodec<ChorusFlowerBlock> codec() {
		return CODEC;
	}

	protected ChorusFlowerBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties);
		this.plant = block;
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}

	@Override
	protected boolean isRandomlyTicking(BlockState blockState) {
		return (Integer)blockState.getValue(AGE) < 5;
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return BLOCK_SUPPORT_SHAPE;
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		BlockPos blockPos2 = blockPos.above();
		if (serverLevel.isEmptyBlock(blockPos2) && blockPos2.getY() < serverLevel.getMaxBuildHeight()) {
			int i = (Integer)blockState.getValue(AGE);
			if (i < 5) {
				boolean bl = false;
				boolean bl2 = false;
				BlockState blockState2 = serverLevel.getBlockState(blockPos.below());
				if (blockState2.is(Blocks.END_STONE)) {
					bl = true;
				} else if (blockState2.is(this.plant)) {
					int j = 1;

					for (int k = 0; k < 4; k++) {
						BlockState blockState3 = serverLevel.getBlockState(blockPos.below(j + 1));
						if (!blockState3.is(this.plant)) {
							if (blockState3.is(Blocks.END_STONE)) {
								bl2 = true;
							}
							break;
						}

						j++;
					}

					if (j < 2 || j <= randomSource.nextInt(bl2 ? 5 : 4)) {
						bl = true;
					}
				} else if (blockState2.isAir()) {
					bl = true;
				}

				if (bl && allNeighborsEmpty(serverLevel, blockPos2, null) && serverLevel.isEmptyBlock(blockPos.above(2))) {
					serverLevel.setBlock(blockPos, ChorusPlantBlock.getStateWithConnections(serverLevel, blockPos, this.plant.defaultBlockState()), 2);
					this.placeGrownFlower(serverLevel, blockPos2, i);
				} else if (i < 4) {
					int j = randomSource.nextInt(4);
					if (bl2) {
						j++;
					}

					boolean bl3 = false;

					for (int l = 0; l < j; l++) {
						Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
						BlockPos blockPos3 = blockPos.relative(direction);
						if (serverLevel.isEmptyBlock(blockPos3)
							&& serverLevel.isEmptyBlock(blockPos3.below())
							&& allNeighborsEmpty(serverLevel, blockPos3, direction.getOpposite())) {
							this.placeGrownFlower(serverLevel, blockPos3, i + 1);
							bl3 = true;
						}
					}

					if (bl3) {
						serverLevel.setBlock(blockPos, ChorusPlantBlock.getStateWithConnections(serverLevel, blockPos, this.plant.defaultBlockState()), 2);
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
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction != Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.scheduleTick(blockPos, this, 1);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.below());
		if (!blockState2.is(this.plant) && !blockState2.is(Blocks.END_STONE)) {
			if (!blockState2.isAir()) {
				return false;
			} else {
				boolean bl = false;

				for (Direction direction : Direction.Plane.HORIZONTAL) {
					BlockState blockState3 = levelReader.getBlockState(blockPos.relative(direction));
					if (blockState3.is(this.plant)) {
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

	public static void generatePlant(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, int i) {
		levelAccessor.setBlock(blockPos, ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos, Blocks.CHORUS_PLANT.defaultBlockState()), 2);
		growTreeRecursive(levelAccessor, blockPos, randomSource, blockPos, i, 0);
	}

	private static void growTreeRecursive(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, BlockPos blockPos2, int i, int j) {
		Block block = Blocks.CHORUS_PLANT;
		int k = randomSource.nextInt(4) + 1;
		if (j == 0) {
			k++;
		}

		for (int l = 0; l < k; l++) {
			BlockPos blockPos3 = blockPos.above(l + 1);
			if (!allNeighborsEmpty(levelAccessor, blockPos3, null)) {
				return;
			}

			levelAccessor.setBlock(blockPos3, ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos3, block.defaultBlockState()), 2);
			levelAccessor.setBlock(blockPos3.below(), ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos3.below(), block.defaultBlockState()), 2);
		}

		boolean bl = false;
		if (j < 4) {
			int m = randomSource.nextInt(4);
			if (j == 0) {
				m++;
			}

			for (int n = 0; n < m; n++) {
				Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
				BlockPos blockPos4 = blockPos.above(k).relative(direction);
				if (Math.abs(blockPos4.getX() - blockPos2.getX()) < i
					&& Math.abs(blockPos4.getZ() - blockPos2.getZ()) < i
					&& levelAccessor.isEmptyBlock(blockPos4)
					&& levelAccessor.isEmptyBlock(blockPos4.below())
					&& allNeighborsEmpty(levelAccessor, blockPos4, direction.getOpposite())) {
					bl = true;
					levelAccessor.setBlock(blockPos4, ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos4, block.defaultBlockState()), 2);
					levelAccessor.setBlock(
						blockPos4.relative(direction.getOpposite()),
						ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos4.relative(direction.getOpposite()), block.defaultBlockState()),
						2
					);
					growTreeRecursive(levelAccessor, blockPos4, randomSource, blockPos2, i, j + 1);
				}
			}
		}

		if (!bl) {
			levelAccessor.setBlock(blockPos.above(k), Blocks.CHORUS_FLOWER.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
		}
	}

	@Override
	protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		BlockPos blockPos = blockHitResult.getBlockPos();
		if (!level.isClientSide && projectile.mayInteract(level, blockPos) && projectile.mayBreak(level)) {
			level.destroyBlock(blockPos, true, projectile);
		}
	}
}
