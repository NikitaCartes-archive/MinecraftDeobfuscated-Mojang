package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FireBlock extends BaseFireBlock {
	public static final int MAX_AGE = 15;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
	public static final BooleanProperty NORTH = PipeBlock.NORTH;
	public static final BooleanProperty EAST = PipeBlock.EAST;
	public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
	public static final BooleanProperty WEST = PipeBlock.WEST;
	public static final BooleanProperty UP = PipeBlock.UP;
	private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = (Map<Direction, BooleanProperty>)PipeBlock.PROPERTY_BY_DIRECTION
		.entrySet()
		.stream()
		.filter(entry -> entry.getKey() != Direction.DOWN)
		.collect(Util.toMap());
	private static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
	private static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
	private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
	private final Map<BlockState, VoxelShape> shapesCache;
	private static final int FLAME_INSTANT = 60;
	private static final int FLAME_EASY = 30;
	private static final int FLAME_MEDIUM = 15;
	private static final int FLAME_HARD = 5;
	private static final int BURN_INSTANT = 100;
	private static final int BURN_EASY = 60;
	private static final int BURN_MEDIUM = 20;
	private static final int BURN_HARD = 5;
	private final Object2IntMap<Block> flameOdds = new Object2IntOpenHashMap<>();
	private final Object2IntMap<Block> burnOdds = new Object2IntOpenHashMap<>();

	public FireBlock(BlockBehaviour.Properties properties) {
		super(properties, 1.0F);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(AGE, Integer.valueOf(0))
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
				.setValue(UP, Boolean.valueOf(false))
		);
		this.shapesCache = ImmutableMap.copyOf(
			(Map<? extends BlockState, ? extends VoxelShape>)this.stateDefinition
				.getPossibleStates()
				.stream()
				.filter(blockState -> (Integer)blockState.getValue(AGE) == 0)
				.collect(Collectors.toMap(Function.identity(), FireBlock::calculateShape))
		);
	}

	private static VoxelShape calculateShape(BlockState blockState) {
		VoxelShape voxelShape = Shapes.empty();
		if ((Boolean)blockState.getValue(UP)) {
			voxelShape = UP_AABB;
		}

		if ((Boolean)blockState.getValue(NORTH)) {
			voxelShape = Shapes.or(voxelShape, NORTH_AABB);
		}

		if ((Boolean)blockState.getValue(SOUTH)) {
			voxelShape = Shapes.or(voxelShape, SOUTH_AABB);
		}

		if ((Boolean)blockState.getValue(EAST)) {
			voxelShape = Shapes.or(voxelShape, EAST_AABB);
		}

		if ((Boolean)blockState.getValue(WEST)) {
			voxelShape = Shapes.or(voxelShape, WEST_AABB);
		}

		return voxelShape.isEmpty() ? DOWN_AABB : voxelShape;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return this.canSurvive(blockState, levelAccessor, blockPos)
			? this.getStateWithAge(levelAccessor, blockPos, (Integer)blockState.getValue(AGE))
			: Blocks.AIR.defaultBlockState();
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.shapesCache.get(blockState.setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.getStateForPlacement(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
	}

	protected BlockState getStateForPlacement(BlockGetter blockGetter, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		if (!this.canBurn(blockState) && !blockState.isFaceSturdy(blockGetter, blockPos2, Direction.UP)) {
			BlockState blockState2 = this.defaultBlockState();

			for (Direction direction : Direction.values()) {
				BooleanProperty booleanProperty = (BooleanProperty)PROPERTY_BY_DIRECTION.get(direction);
				if (booleanProperty != null) {
					blockState2 = blockState2.setValue(booleanProperty, Boolean.valueOf(this.canBurn(blockGetter.getBlockState(blockPos.relative(direction)))));
				}
			}

			return blockState2;
		} else {
			return this.defaultBlockState();
		}
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		return levelReader.getBlockState(blockPos2).isFaceSturdy(levelReader, blockPos2, Direction.UP) || this.isValidFireLocation(levelReader, blockPos);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		serverLevel.getBlockTicks().scheduleTick(blockPos, this, getFireTickDelay(serverLevel.random));
		if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
			if (!blockState.canSurvive(serverLevel, blockPos)) {
				serverLevel.removeBlock(blockPos, false);
			}

			BlockState blockState2 = serverLevel.getBlockState(blockPos.below());
			boolean bl = blockState2.is(serverLevel.dimensionType().infiniburn());
			int i = (Integer)blockState.getValue(AGE);
			if (!bl && serverLevel.isRaining() && this.isNearRain(serverLevel, blockPos) && random.nextFloat() < 0.2F + (float)i * 0.03F) {
				serverLevel.removeBlock(blockPos, false);
			} else {
				int j = Math.min(15, i + random.nextInt(3) / 2);
				if (i != j) {
					blockState = blockState.setValue(AGE, Integer.valueOf(j));
					serverLevel.setBlock(blockPos, blockState, 4);
				}

				if (!bl) {
					if (!this.isValidFireLocation(serverLevel, blockPos)) {
						BlockPos blockPos2 = blockPos.below();
						if (!serverLevel.getBlockState(blockPos2).isFaceSturdy(serverLevel, blockPos2, Direction.UP) || i > 3) {
							serverLevel.removeBlock(blockPos, false);
						}

						return;
					}

					if (i == 15 && random.nextInt(4) == 0 && !this.canBurn(serverLevel.getBlockState(blockPos.below()))) {
						serverLevel.removeBlock(blockPos, false);
						return;
					}
				}

				boolean bl2 = serverLevel.isHumidAt(blockPos);
				int k = bl2 ? -50 : 0;
				this.checkBurnOut(serverLevel, blockPos.east(), 300 + k, random, i);
				this.checkBurnOut(serverLevel, blockPos.west(), 300 + k, random, i);
				this.checkBurnOut(serverLevel, blockPos.below(), 250 + k, random, i);
				this.checkBurnOut(serverLevel, blockPos.above(), 250 + k, random, i);
				this.checkBurnOut(serverLevel, blockPos.north(), 300 + k, random, i);
				this.checkBurnOut(serverLevel, blockPos.south(), 300 + k, random, i);
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int l = -1; l <= 1; l++) {
					for (int m = -1; m <= 1; m++) {
						for (int n = -1; n <= 4; n++) {
							if (l != 0 || n != 0 || m != 0) {
								int o = 100;
								if (n > 1) {
									o += (n - 1) * 100;
								}

								mutableBlockPos.setWithOffset(blockPos, l, n, m);
								int p = this.getFireOdds(serverLevel, mutableBlockPos);
								if (p > 0) {
									int q = (p + 40 + serverLevel.getDifficulty().getId() * 7) / (i + 30);
									if (bl2) {
										q /= 2;
									}

									if (q > 0 && random.nextInt(o) <= q && (!serverLevel.isRaining() || !this.isNearRain(serverLevel, mutableBlockPos))) {
										int r = Math.min(15, i + random.nextInt(5) / 4);
										serverLevel.setBlock(mutableBlockPos, this.getStateWithAge(serverLevel, mutableBlockPos, r), 3);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected boolean isNearRain(Level level, BlockPos blockPos) {
		return level.isRainingAt(blockPos)
			|| level.isRainingAt(blockPos.west())
			|| level.isRainingAt(blockPos.east())
			|| level.isRainingAt(blockPos.north())
			|| level.isRainingAt(blockPos.south());
	}

	private int getBurnOdd(BlockState blockState) {
		return blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)
			? 0
			: this.burnOdds.getInt(blockState.getBlock());
	}

	private int getFlameOdds(BlockState blockState) {
		return blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)
			? 0
			: this.flameOdds.getInt(blockState.getBlock());
	}

	private void checkBurnOut(Level level, BlockPos blockPos, int i, Random random, int j) {
		int k = this.getBurnOdd(level.getBlockState(blockPos));
		if (random.nextInt(i) < k) {
			BlockState blockState = level.getBlockState(blockPos);
			if (random.nextInt(j + 10) < 5 && !level.isRainingAt(blockPos)) {
				int l = Math.min(j + random.nextInt(5) / 4, 15);
				level.setBlock(blockPos, this.getStateWithAge(level, blockPos, l), 3);
			} else {
				level.removeBlock(blockPos, false);
			}

			Block block = blockState.getBlock();
			if (block instanceof TntBlock) {
				TntBlock.explode(level, blockPos);
			}
		}
	}

	private BlockState getStateWithAge(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		BlockState blockState = getState(levelAccessor, blockPos);
		return blockState.is(Blocks.FIRE) ? blockState.setValue(AGE, Integer.valueOf(i)) : blockState;
	}

	private boolean isValidFireLocation(BlockGetter blockGetter, BlockPos blockPos) {
		for (Direction direction : Direction.values()) {
			if (this.canBurn(blockGetter.getBlockState(blockPos.relative(direction)))) {
				return true;
			}
		}

		return false;
	}

	private int getFireOdds(LevelReader levelReader, BlockPos blockPos) {
		if (!levelReader.isEmptyBlock(blockPos)) {
			return 0;
		} else {
			int i = 0;

			for (Direction direction : Direction.values()) {
				BlockState blockState = levelReader.getBlockState(blockPos.relative(direction));
				i = Math.max(this.getFlameOdds(blockState), i);
			}

			return i;
		}
	}

	@Override
	protected boolean canBurn(BlockState blockState) {
		return this.getFlameOdds(blockState) > 0;
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		super.onPlace(blockState, level, blockPos, blockState2, bl);
		level.getBlockTicks().scheduleTick(blockPos, this, getFireTickDelay(level.random));
	}

	private static int getFireTickDelay(Random random) {
		return 30 + random.nextInt(10);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
	}

	private void setFlammable(Block block, int i, int j) {
		this.flameOdds.put(block, i);
		this.burnOdds.put(block, j);
	}

	public static void bootStrap() {
		FireBlock fireBlock = (FireBlock)Blocks.FIRE;
		fireBlock.setFlammable(Blocks.OAK_PLANKS, 5, 20);
		fireBlock.setFlammable(Blocks.SPRUCE_PLANKS, 5, 20);
		fireBlock.setFlammable(Blocks.BIRCH_PLANKS, 5, 20);
		fireBlock.setFlammable(Blocks.JUNGLE_PLANKS, 5, 20);
		fireBlock.setFlammable(Blocks.ACACIA_PLANKS, 5, 20);
		fireBlock.setFlammable(Blocks.DARK_OAK_PLANKS, 5, 20);
		fireBlock.setFlammable(Blocks.OAK_SLAB, 5, 20);
		fireBlock.setFlammable(Blocks.SPRUCE_SLAB, 5, 20);
		fireBlock.setFlammable(Blocks.BIRCH_SLAB, 5, 20);
		fireBlock.setFlammable(Blocks.JUNGLE_SLAB, 5, 20);
		fireBlock.setFlammable(Blocks.ACACIA_SLAB, 5, 20);
		fireBlock.setFlammable(Blocks.DARK_OAK_SLAB, 5, 20);
		fireBlock.setFlammable(Blocks.OAK_FENCE_GATE, 5, 20);
		fireBlock.setFlammable(Blocks.SPRUCE_FENCE_GATE, 5, 20);
		fireBlock.setFlammable(Blocks.BIRCH_FENCE_GATE, 5, 20);
		fireBlock.setFlammable(Blocks.JUNGLE_FENCE_GATE, 5, 20);
		fireBlock.setFlammable(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
		fireBlock.setFlammable(Blocks.ACACIA_FENCE_GATE, 5, 20);
		fireBlock.setFlammable(Blocks.OAK_FENCE, 5, 20);
		fireBlock.setFlammable(Blocks.SPRUCE_FENCE, 5, 20);
		fireBlock.setFlammable(Blocks.BIRCH_FENCE, 5, 20);
		fireBlock.setFlammable(Blocks.JUNGLE_FENCE, 5, 20);
		fireBlock.setFlammable(Blocks.DARK_OAK_FENCE, 5, 20);
		fireBlock.setFlammable(Blocks.ACACIA_FENCE, 5, 20);
		fireBlock.setFlammable(Blocks.OAK_STAIRS, 5, 20);
		fireBlock.setFlammable(Blocks.BIRCH_STAIRS, 5, 20);
		fireBlock.setFlammable(Blocks.SPRUCE_STAIRS, 5, 20);
		fireBlock.setFlammable(Blocks.JUNGLE_STAIRS, 5, 20);
		fireBlock.setFlammable(Blocks.ACACIA_STAIRS, 5, 20);
		fireBlock.setFlammable(Blocks.DARK_OAK_STAIRS, 5, 20);
		fireBlock.setFlammable(Blocks.OAK_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.SPRUCE_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.BIRCH_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.JUNGLE_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.ACACIA_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.DARK_OAK_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_OAK_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_SPRUCE_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_BIRCH_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_JUNGLE_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_ACACIA_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_DARK_OAK_LOG, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_OAK_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_SPRUCE_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_BIRCH_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_JUNGLE_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_ACACIA_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.STRIPPED_DARK_OAK_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.OAK_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.SPRUCE_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.BIRCH_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.JUNGLE_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.ACACIA_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.DARK_OAK_WOOD, 5, 5);
		fireBlock.setFlammable(Blocks.OAK_LEAVES, 30, 60);
		fireBlock.setFlammable(Blocks.SPRUCE_LEAVES, 30, 60);
		fireBlock.setFlammable(Blocks.BIRCH_LEAVES, 30, 60);
		fireBlock.setFlammable(Blocks.JUNGLE_LEAVES, 30, 60);
		fireBlock.setFlammable(Blocks.ACACIA_LEAVES, 30, 60);
		fireBlock.setFlammable(Blocks.DARK_OAK_LEAVES, 30, 60);
		fireBlock.setFlammable(Blocks.BOOKSHELF, 30, 20);
		fireBlock.setFlammable(Blocks.TNT, 15, 100);
		fireBlock.setFlammable(Blocks.GRASS, 60, 100);
		fireBlock.setFlammable(Blocks.FERN, 60, 100);
		fireBlock.setFlammable(Blocks.DEAD_BUSH, 60, 100);
		fireBlock.setFlammable(Blocks.SUNFLOWER, 60, 100);
		fireBlock.setFlammable(Blocks.LILAC, 60, 100);
		fireBlock.setFlammable(Blocks.ROSE_BUSH, 60, 100);
		fireBlock.setFlammable(Blocks.PEONY, 60, 100);
		fireBlock.setFlammable(Blocks.TALL_GRASS, 60, 100);
		fireBlock.setFlammable(Blocks.LARGE_FERN, 60, 100);
		fireBlock.setFlammable(Blocks.DANDELION, 60, 100);
		fireBlock.setFlammable(Blocks.POPPY, 60, 100);
		fireBlock.setFlammable(Blocks.BLUE_ORCHID, 60, 100);
		fireBlock.setFlammable(Blocks.ALLIUM, 60, 100);
		fireBlock.setFlammable(Blocks.AZURE_BLUET, 60, 100);
		fireBlock.setFlammable(Blocks.RED_TULIP, 60, 100);
		fireBlock.setFlammable(Blocks.ORANGE_TULIP, 60, 100);
		fireBlock.setFlammable(Blocks.WHITE_TULIP, 60, 100);
		fireBlock.setFlammable(Blocks.PINK_TULIP, 60, 100);
		fireBlock.setFlammable(Blocks.OXEYE_DAISY, 60, 100);
		fireBlock.setFlammable(Blocks.CORNFLOWER, 60, 100);
		fireBlock.setFlammable(Blocks.LILY_OF_THE_VALLEY, 60, 100);
		fireBlock.setFlammable(Blocks.WITHER_ROSE, 60, 100);
		fireBlock.setFlammable(Blocks.WHITE_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.ORANGE_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.MAGENTA_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.LIGHT_BLUE_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.YELLOW_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.LIME_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.PINK_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.GRAY_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.LIGHT_GRAY_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.CYAN_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.PURPLE_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.BLUE_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.BROWN_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.GREEN_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.RED_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.BLACK_WOOL, 30, 60);
		fireBlock.setFlammable(Blocks.VINE, 15, 100);
		fireBlock.setFlammable(Blocks.COAL_BLOCK, 5, 5);
		fireBlock.setFlammable(Blocks.HAY_BLOCK, 60, 20);
		fireBlock.setFlammable(Blocks.TARGET, 15, 20);
		fireBlock.setFlammable(Blocks.WHITE_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.ORANGE_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.MAGENTA_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.LIGHT_BLUE_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.YELLOW_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.LIME_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.PINK_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.GRAY_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.LIGHT_GRAY_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.CYAN_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.PURPLE_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.BLUE_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.BROWN_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.GREEN_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.RED_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.BLACK_CARPET, 60, 20);
		fireBlock.setFlammable(Blocks.DRIED_KELP_BLOCK, 30, 60);
		fireBlock.setFlammable(Blocks.BAMBOO, 60, 60);
		fireBlock.setFlammable(Blocks.SCAFFOLDING, 60, 60);
		fireBlock.setFlammable(Blocks.LECTERN, 30, 20);
		fireBlock.setFlammable(Blocks.COMPOSTER, 5, 20);
		fireBlock.setFlammable(Blocks.SWEET_BERRY_BUSH, 60, 100);
		fireBlock.setFlammable(Blocks.BEEHIVE, 5, 20);
		fireBlock.setFlammable(Blocks.BEE_NEST, 30, 20);
		fireBlock.setFlammable(Blocks.AZALEA_LEAVES, 30, 60);
		fireBlock.setFlammable(Blocks.FLOWERING_AZALEA_LEAVES, 30, 60);
		fireBlock.setFlammable(Blocks.CAVE_VINES, 15, 60);
		fireBlock.setFlammable(Blocks.CAVE_VINES_PLANT, 15, 60);
		fireBlock.setFlammable(Blocks.SPORE_BLOSSOM, 60, 100);
		fireBlock.setFlammable(Blocks.AZALEA, 30, 60);
		fireBlock.setFlammable(Blocks.FLOWERING_AZALEA, 30, 60);
		fireBlock.setFlammable(Blocks.BIG_DRIPLEAF, 60, 100);
		fireBlock.setFlammable(Blocks.BIG_DRIPLEAF_STEM, 60, 100);
		fireBlock.setFlammable(Blocks.SMALL_DRIPLEAF, 60, 100);
		fireBlock.setFlammable(Blocks.HANGING_ROOTS, 30, 60);
		fireBlock.setFlammable(Blocks.GLOW_LICHEN, 15, 100);
	}
}
