package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class StrongRootsBlock extends PipeBlock implements SimpleWaterloggedBlock {
	public static final SimpleWeightedRandomList<Direction> GROWTH_DIRECTION = SimpleWeightedRandomList.<Direction>builder()
		.add(Direction.DOWN, 10)
		.add(Direction.NORTH)
		.add(Direction.SOUTH)
		.add(Direction.EAST)
		.add(Direction.WEST)
		.build();
	public static final MapCodec<StrongRootsBlock> CODEC = simpleCodec(StrongRootsBlock::new);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final Supplier<ItemStack> SILK_TOUCH_DROP_TOOL = Suppliers.memoize(() -> {
		ItemStack itemStack = new ItemStack(Items.NETHERITE_PICKAXE);
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		mutable.set(Enchantments.SILK_TOUCH, 1);
		itemStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
		return itemStack;
	});
	private static final Supplier<ItemStack> FORTUNE_DROP_TOOL = Suppliers.memoize(() -> {
		ItemStack itemStack = new ItemStack(Items.NETHERITE_PICKAXE);
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		mutable.set(Enchantments.FORTUNE, 3);
		itemStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
		return itemStack;
	});
	private static final Supplier<ItemStack> PLAIN_DROP_TOOL = Suppliers.memoize(() -> new ItemStack(Items.NETHERITE_PICKAXE));
	public static final SimpleWeightedRandomList<Supplier<ItemStack>> TOOLS = SimpleWeightedRandomList.<Supplier<ItemStack>>builder()
		.add(PLAIN_DROP_TOOL, 3)
		.add(SILK_TOUCH_DROP_TOOL)
		.add(FORTUNE_DROP_TOOL)
		.build();
	public static final StrongRootsBlock.TraceEntry UP_TRACE = new StrongRootsBlock.TraceEntry(Direction.UP, UP);
	private static final List<StrongRootsBlock.TraceEntry> HORIZONTAL_TRACES = List.of(
		new StrongRootsBlock.TraceEntry(Direction.NORTH, NORTH),
		new StrongRootsBlock.TraceEntry(Direction.SOUTH, SOUTH),
		new StrongRootsBlock.TraceEntry(Direction.EAST, EAST),
		new StrongRootsBlock.TraceEntry(Direction.WEST, WEST)
	);

	@Override
	public MapCodec<StrongRootsBlock> codec() {
		return CODEC;
	}

	protected StrongRootsBlock(BlockBehaviour.Properties properties) {
		super(0.3125F, properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
				.setValue(UP, Boolean.valueOf(false))
				.setValue(DOWN, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return getStateWithConnections(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), this.defaultBlockState());
	}

	public static BlockState getStateWithConnections(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		BlockState blockState2 = blockGetter.getBlockState(blockPos.below());
		BlockState blockState3 = blockGetter.getBlockState(blockPos.above());
		BlockState blockState4 = blockGetter.getBlockState(blockPos.north());
		BlockState blockState5 = blockGetter.getBlockState(blockPos.east());
		BlockState blockState6 = blockGetter.getBlockState(blockPos.south());
		BlockState blockState7 = blockGetter.getBlockState(blockPos.west());
		Block block = blockState.getBlock();
		return blockState.setValue(WATERLOGGED, Boolean.valueOf(blockGetter.getFluidState(blockPos).getType() == Fluids.WATER))
			.trySetValue(DOWN, Boolean.valueOf(blockState2.is(block) || blockState2.is(Blocks.POWERFUL_POTATO)))
			.trySetValue(UP, Boolean.valueOf(blockState3.is(block) || blockState3.is(Blocks.POWERFUL_POTATO)))
			.trySetValue(NORTH, Boolean.valueOf(blockState4.is(block) || blockState4.is(Blocks.POWERFUL_POTATO)))
			.trySetValue(EAST, Boolean.valueOf(blockState5.is(block) || blockState5.is(Blocks.POWERFUL_POTATO)))
			.trySetValue(SOUTH, Boolean.valueOf(blockState6.is(block) || blockState6.is(Blocks.POWERFUL_POTATO)))
			.trySetValue(WEST, Boolean.valueOf(blockState7.is(block) || blockState7.is(Blocks.POWERFUL_POTATO)));
	}

	private static Optional<BlockPos> traceToTater(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
		int i = 0;
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
		List<StrongRootsBlock.TraceEntry> list = new ArrayList(5);
		list.add(UP_TRACE);
		list.addAll(HORIZONTAL_TRACES);
		List<StrongRootsBlock.TraceEntry> list2 = list.subList(1, 5);
		Util.shuffle(list2, randomSource);

		while (i < 512) {
			for (StrongRootsBlock.TraceEntry traceEntry : list) {
				boolean bl = (Boolean)blockState.getValue(traceEntry.property);
				if (bl) {
					mutableBlockPos2.setWithOffset(mutableBlockPos, traceEntry.direction);
					if (serverLevel.isLoaded(mutableBlockPos2)) {
						BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos2);
						if (blockState2.is(Blocks.POWERFUL_POTATO)) {
							return Optional.of(mutableBlockPos2);
						}

						if (blockState2.is(Blocks.STRONG_ROOTS)) {
							mutableBlockPos.set(mutableBlockPos2);
							blockState = blockState2;
							i++;
							Util.shuffle(list2, randomSource);
							break;
						}
					}
				}
			}
			break;
		}

		return Optional.empty();
	}

	@Nullable
	private static StrongRootsBlock.FoundPos checkReplacementPos(ServerLevel serverLevel, BlockPos blockPos) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		if (!canReplace(blockState)) {
			return null;
		} else {
			BlockState blockState2 = Blocks.STRONG_ROOTS.defaultBlockState();
			boolean bl = false;

			for (Direction direction : Direction.values()) {
				BlockState blockState3 = serverLevel.getBlockState(blockPos.relative(direction));
				boolean bl2 = blockState3.is(Blocks.STRONG_ROOTS) || blockState3.is(Blocks.POWERFUL_POTATO);
				if (bl2) {
					if (bl) {
						return null;
					}

					bl = true;
					blockState2 = blockState2.trySetValue((Property)PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(true));
				}
			}

			return bl ? new StrongRootsBlock.FoundPos(blockState2, blockState) : null;
		}
	}

	@Nullable
	public static List<ItemStack> tryPlace(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		StrongRootsBlock.FoundPos foundPos = checkReplacementPos(serverLevel, blockPos);
		return foundPos != null ? foundPos.apply(serverLevel, blockPos, randomSource) : null;
	}

	public static boolean canReplace(BlockState blockState) {
		if (blockState.is(Blocks.POWERFUL_POTATO)) {
			return false;
		} else {
			return blockState.is(BlockTags.FEATURES_CANNOT_REPLACE) ? false : !blockState.is(Blocks.STRONG_ROOTS);
		}
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		boolean bl = blockState2.is(this) || blockState2.is(Blocks.POWERFUL_POTATO);
		return blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(bl));
	}

	@Override
	protected boolean isRandomlyTicking(BlockState blockState) {
		int i = 0;

		for (Property<Boolean> property : PROPERTY_BY_DIRECTION.values()) {
			if ((Boolean)blockState.getValue(property)) {
				i++;
			}

			if (i > 3) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		GROWTH_DIRECTION.getRandomValue(randomSource)
			.ifPresent(
				direction -> {
					BlockPos blockPos2 = blockPos.relative(direction);
					StrongRootsBlock.FoundPos foundPos = checkReplacementPos(serverLevel, blockPos2);
					if (foundPos != null) {
						traceToTater(serverLevel, blockPos, blockState, randomSource)
							.ifPresent(blockPos2x -> foundPos.apply(serverLevel, blockPos2, randomSource).forEach(itemStack -> popResource(serverLevel, blockPos2x, itemStack)));
					}
				}
			);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	public static record FoundPos(BlockState newState, BlockState oldState) {
		public List<ItemStack> apply(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
			boolean bl = this.oldState.getFluidState().getType() == Fluids.WATER;
			List<ItemStack> list = Block.getDrops(
				this.oldState, serverLevel, blockPos, null, null, (ItemStack)StrongRootsBlock.TOOLS.getRandomValue(randomSource).map(Supplier::get).orElse(ItemStack.EMPTY)
			);
			serverLevel.setBlock(blockPos, this.newState.setValue(StrongRootsBlock.WATERLOGGED, Boolean.valueOf(bl)), 2);
			return list;
		}
	}

	static record TraceEntry(Direction direction, Property<Boolean> property) {
	}
}
