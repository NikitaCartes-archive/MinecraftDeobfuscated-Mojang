package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ComposterBlock extends Block implements WorldlyContainerHolder {
	public static final MapCodec<ComposterBlock> CODEC = simpleCodec(ComposterBlock::new);
	public static final int READY = 8;
	public static final int MIN_LEVEL = 0;
	public static final int MAX_LEVEL = 7;
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
	public static final Object2FloatMap<ItemLike> COMPOSTABLES = new Object2FloatOpenHashMap<>();
	private static final int AABB_SIDE_THICKNESS = 2;
	private static final VoxelShape OUTER_SHAPE = Shapes.block();
	private static final VoxelShape[] SHAPES = Util.make(new VoxelShape[9], voxelShapes -> {
		for (int i = 0; i < 8; i++) {
			voxelShapes[i] = Shapes.join(OUTER_SHAPE, Block.box(2.0, (double)Math.max(2, 1 + i * 2), 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);
		}

		voxelShapes[8] = voxelShapes[7];
	});

	@Override
	public MapCodec<ComposterBlock> codec() {
		return CODEC;
	}

	public static void bootStrap() {
		COMPOSTABLES.defaultReturnValue(-1.0F);
		float f = 0.3F;
		float g = 0.5F;
		float h = 0.65F;
		float i = 0.85F;
		float j = 1.0F;
		add(0.3F, Items.JUNGLE_LEAVES);
		add(0.3F, Items.OAK_LEAVES);
		add(0.3F, Items.SPRUCE_LEAVES);
		add(0.3F, Items.DARK_OAK_LEAVES);
		add(0.3F, Items.ACACIA_LEAVES);
		add(0.3F, Items.CHERRY_LEAVES);
		add(0.3F, Items.BIRCH_LEAVES);
		add(0.3F, Items.AZALEA_LEAVES);
		add(0.3F, Items.MANGROVE_LEAVES);
		add(0.3F, Items.OAK_SAPLING);
		add(0.3F, Items.SPRUCE_SAPLING);
		add(0.3F, Items.BIRCH_SAPLING);
		add(0.3F, Items.JUNGLE_SAPLING);
		add(0.3F, Items.ACACIA_SAPLING);
		add(0.3F, Items.CHERRY_SAPLING);
		add(0.3F, Items.DARK_OAK_SAPLING);
		add(0.3F, Items.MANGROVE_PROPAGULE);
		add(0.3F, Items.BEETROOT_SEEDS);
		add(0.3F, Items.DRIED_KELP);
		add(0.3F, Items.GRASS);
		add(0.3F, Items.KELP);
		add(0.3F, Items.MELON_SEEDS);
		add(0.3F, Items.PUMPKIN_SEEDS);
		add(0.3F, Items.SEAGRASS);
		add(0.3F, Items.SWEET_BERRIES);
		add(0.3F, Items.GLOW_BERRIES);
		add(0.3F, Items.WHEAT_SEEDS);
		add(0.3F, Items.MOSS_CARPET);
		add(0.3F, Items.PINK_PETALS);
		add(0.3F, Items.SMALL_DRIPLEAF);
		add(0.3F, Items.HANGING_ROOTS);
		add(0.3F, Items.MANGROVE_ROOTS);
		add(0.3F, Items.TORCHFLOWER_SEEDS);
		add(0.3F, Items.PITCHER_POD);
		add(0.5F, Items.DRIED_KELP_BLOCK);
		add(0.5F, Items.TALL_GRASS);
		add(0.5F, Items.FLOWERING_AZALEA_LEAVES);
		add(0.5F, Items.CACTUS);
		add(0.5F, Items.SUGAR_CANE);
		add(0.5F, Items.VINE);
		add(0.5F, Items.NETHER_SPROUTS);
		add(0.5F, Items.WEEPING_VINES);
		add(0.5F, Items.TWISTING_VINES);
		add(0.5F, Items.MELON_SLICE);
		add(0.5F, Items.GLOW_LICHEN);
		add(0.65F, Items.SEA_PICKLE);
		add(0.65F, Items.LILY_PAD);
		add(0.65F, Items.PUMPKIN);
		add(0.65F, Items.CARVED_PUMPKIN);
		add(0.65F, Items.MELON);
		add(0.65F, Items.APPLE);
		add(0.65F, Items.BEETROOT);
		add(0.65F, Items.CARROT);
		add(0.65F, Items.COCOA_BEANS);
		add(0.65F, Items.POTATO);
		add(0.65F, Items.WHEAT);
		add(0.65F, Items.BROWN_MUSHROOM);
		add(0.65F, Items.RED_MUSHROOM);
		add(0.65F, Items.MUSHROOM_STEM);
		add(0.65F, Items.CRIMSON_FUNGUS);
		add(0.65F, Items.WARPED_FUNGUS);
		add(0.65F, Items.NETHER_WART);
		add(0.65F, Items.CRIMSON_ROOTS);
		add(0.65F, Items.WARPED_ROOTS);
		add(0.65F, Items.SHROOMLIGHT);
		add(0.65F, Items.DANDELION);
		add(0.65F, Items.POPPY);
		add(0.65F, Items.BLUE_ORCHID);
		add(0.65F, Items.ALLIUM);
		add(0.65F, Items.AZURE_BLUET);
		add(0.65F, Items.RED_TULIP);
		add(0.65F, Items.ORANGE_TULIP);
		add(0.65F, Items.WHITE_TULIP);
		add(0.65F, Items.PINK_TULIP);
		add(0.65F, Items.OXEYE_DAISY);
		add(0.65F, Items.CORNFLOWER);
		add(0.65F, Items.LILY_OF_THE_VALLEY);
		add(0.65F, Items.WITHER_ROSE);
		add(0.65F, Items.FERN);
		add(0.65F, Items.SUNFLOWER);
		add(0.65F, Items.LILAC);
		add(0.65F, Items.ROSE_BUSH);
		add(0.65F, Items.PEONY);
		add(0.65F, Items.LARGE_FERN);
		add(0.65F, Items.SPORE_BLOSSOM);
		add(0.65F, Items.AZALEA);
		add(0.65F, Items.MOSS_BLOCK);
		add(0.65F, Items.BIG_DRIPLEAF);
		add(0.85F, Items.HAY_BLOCK);
		add(0.85F, Items.BROWN_MUSHROOM_BLOCK);
		add(0.85F, Items.RED_MUSHROOM_BLOCK);
		add(0.85F, Items.NETHER_WART_BLOCK);
		add(0.85F, Items.WARPED_WART_BLOCK);
		add(0.85F, Items.FLOWERING_AZALEA);
		add(0.85F, Items.BREAD);
		add(0.85F, Items.BAKED_POTATO);
		add(0.85F, Items.COOKIE);
		add(0.85F, Items.TORCHFLOWER);
		add(0.85F, Items.PITCHER_PLANT);
		add(1.0F, Items.CAKE);
		add(1.0F, Items.PUMPKIN_PIE);
	}

	private static void add(float f, ItemLike itemLike) {
		COMPOSTABLES.put(itemLike.asItem(), f);
	}

	public ComposterBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
	}

	public static void handleFill(Level level, BlockPos blockPos, boolean bl) {
		BlockState blockState = level.getBlockState(blockPos);
		level.playLocalSound(blockPos, bl ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
		double d = blockState.getShape(level, blockPos).max(Direction.Axis.Y, 0.5, 0.5) + 0.03125;
		double e = 0.13125F;
		double f = 0.7375F;
		RandomSource randomSource = level.getRandom();

		for (int i = 0; i < 10; i++) {
			double g = randomSource.nextGaussian() * 0.02;
			double h = randomSource.nextGaussian() * 0.02;
			double j = randomSource.nextGaussian() * 0.02;
			level.addParticle(
				ParticleTypes.COMPOSTER,
				(double)blockPos.getX() + 0.13125F + 0.7375F * (double)randomSource.nextFloat(),
				(double)blockPos.getY() + d + (double)randomSource.nextFloat() * (1.0 - d),
				(double)blockPos.getZ() + 0.13125F + 0.7375F * (double)randomSource.nextFloat(),
				g,
				h,
				j
			);
		}
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPES[blockState.getValue(LEVEL)];
	}

	@Override
	public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return OUTER_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPES[0];
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if ((Integer)blockState.getValue(LEVEL) == 7) {
			level.scheduleTick(blockPos, blockState.getBlock(), 20);
		}
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		int i = (Integer)blockState.getValue(LEVEL);
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (i < 8 && COMPOSTABLES.containsKey(itemStack.getItem())) {
			if (i < 7 && !level.isClientSide) {
				BlockState blockState2 = addItem(player, blockState, level, blockPos, itemStack);
				level.levelEvent(1500, blockPos, blockState != blockState2 ? 1 : 0);
				player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
				if (!player.getAbilities().instabuild) {
					itemStack.shrink(1);
				}
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		} else if (i == 8) {
			extractProduce(player, blockState, level, blockPos);
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	public static BlockState insertItem(Entity entity, BlockState blockState, ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
		int i = (Integer)blockState.getValue(LEVEL);
		if (i < 7 && COMPOSTABLES.containsKey(itemStack.getItem())) {
			BlockState blockState2 = addItem(entity, blockState, serverLevel, blockPos, itemStack);
			itemStack.shrink(1);
			return blockState2;
		} else {
			return blockState;
		}
	}

	public static BlockState extractProduce(Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
		if (!level.isClientSide) {
			Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockPos, 0.5, 1.01, 0.5).offsetRandom(level.random, 0.7F);
			ItemEntity itemEntity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), new ItemStack(Items.BONE_MEAL));
			itemEntity.setDefaultPickUpDelay();
			level.addFreshEntity(itemEntity);
		}

		BlockState blockState2 = empty(entity, blockState, level, blockPos);
		level.playSound(null, blockPos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		return blockState2;
	}

	static BlockState empty(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockState blockState2 = blockState.setValue(LEVEL, Integer.valueOf(0));
		levelAccessor.setBlock(blockPos, blockState2, 3);
		levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
		return blockState2;
	}

	static BlockState addItem(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, ItemStack itemStack) {
		int i = (Integer)blockState.getValue(LEVEL);
		float f = COMPOSTABLES.getFloat(itemStack.getItem());
		if ((i != 0 || !(f > 0.0F)) && !(levelAccessor.getRandom().nextDouble() < (double)f)) {
			return blockState;
		} else {
			int j = i + 1;
			BlockState blockState2 = blockState.setValue(LEVEL, Integer.valueOf(j));
			levelAccessor.setBlock(blockPos, blockState2, 3);
			levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
			if (j == 7) {
				levelAccessor.scheduleTick(blockPos, blockState.getBlock(), 20);
			}

			return blockState2;
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if ((Integer)blockState.getValue(LEVEL) == 7) {
			serverLevel.setBlock(blockPos, blockState.cycle(LEVEL), 3);
			serverLevel.playSound(null, blockPos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return (Integer)blockState.getValue(LEVEL);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public WorldlyContainer getContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		int i = (Integer)blockState.getValue(LEVEL);
		if (i == 8) {
			return new ComposterBlock.OutputContainer(blockState, levelAccessor, blockPos, new ItemStack(Items.BONE_MEAL));
		} else {
			return (WorldlyContainer)(i < 7 ? new ComposterBlock.InputContainer(blockState, levelAccessor, blockPos) : new ComposterBlock.EmptyContainer());
		}
	}

	static class EmptyContainer extends SimpleContainer implements WorldlyContainer {
		public EmptyContainer() {
			super(0);
		}

		@Override
		public int[] getSlotsForFace(Direction direction) {
			return new int[0];
		}

		@Override
		public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
			return false;
		}

		@Override
		public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
			return false;
		}
	}

	static class InputContainer extends SimpleContainer implements WorldlyContainer {
		private final BlockState state;
		private final LevelAccessor level;
		private final BlockPos pos;
		private boolean changed;

		public InputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
			super(1);
			this.state = blockState;
			this.level = levelAccessor;
			this.pos = blockPos;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public int[] getSlotsForFace(Direction direction) {
			return direction == Direction.UP ? new int[]{0} : new int[0];
		}

		@Override
		public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
			return !this.changed && direction == Direction.UP && ComposterBlock.COMPOSTABLES.containsKey(itemStack.getItem());
		}

		@Override
		public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
			return false;
		}

		@Override
		public void setChanged() {
			ItemStack itemStack = this.getItem(0);
			if (!itemStack.isEmpty()) {
				this.changed = true;
				BlockState blockState = ComposterBlock.addItem(null, this.state, this.level, this.pos, itemStack);
				this.level.levelEvent(1500, this.pos, blockState != this.state ? 1 : 0);
				this.removeItemNoUpdate(0);
			}
		}
	}

	static class OutputContainer extends SimpleContainer implements WorldlyContainer {
		private final BlockState state;
		private final LevelAccessor level;
		private final BlockPos pos;
		private boolean changed;

		public OutputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, ItemStack itemStack) {
			super(itemStack);
			this.state = blockState;
			this.level = levelAccessor;
			this.pos = blockPos;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public int[] getSlotsForFace(Direction direction) {
			return direction == Direction.DOWN ? new int[]{0} : new int[0];
		}

		@Override
		public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
			return false;
		}

		@Override
		public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
			return !this.changed && direction == Direction.DOWN && itemStack.is(Items.BONE_MEAL);
		}

		@Override
		public void setChanged() {
			ComposterBlock.empty(null, this.state, this.level, this.pos);
			this.changed = true;
		}
	}
}
