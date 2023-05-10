package net.minecraft.world.level.block;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final ResourceLocation SHERDS_DYNAMIC_DROP_ID = new ResourceLocation("sherds");
	private static final VoxelShape BOUNDING_BOX = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final BooleanProperty CRACKED = BlockStateProperties.CRACKED;
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	protected DecoratedPotBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(HORIZONTAL_FACING, Direction.NORTH)
				.setValue(WATERLOGGED, Boolean.valueOf(false))
				.setValue(CRACKED, Boolean.valueOf(false))
		);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		return this.defaultBlockState()
			.setValue(HORIZONTAL_FACING, blockPlaceContext.getHorizontalDirection())
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER))
			.setValue(CRACKED, Boolean.valueOf(false));
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return BOUNDING_BOX;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, WATERLOGGED, CRACKED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new DecoratedPotBlockEntity(blockPos, blockState);
	}

	@Override
	public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
			builder.withDynamicDrop(
				SHERDS_DYNAMIC_DROP_ID, consumer -> decoratedPotBlockEntity.getDecorations().sorted().map(Item::getDefaultInstance).forEach(consumer)
			);
		}

		return super.getDrops(blockState, builder);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		ItemStack itemStack = player.getMainHandItem();
		BlockState blockState2 = blockState;
		if (itemStack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(itemStack)) {
			blockState2 = blockState.setValue(CRACKED, Boolean.valueOf(true));
			level.setBlock(blockPos, blockState2, 4);
		}

		super.playerWillDestroy(level, blockPos, blockState2, player);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public SoundType getSoundType(BlockState blockState) {
		return blockState.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);
		DecoratedPotBlockEntity.Decorations decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(itemStack));
		if (!decorations.equals(DecoratedPotBlockEntity.Decorations.EMPTY)) {
			list.add(CommonComponents.EMPTY);
			Stream.of(decorations.front(), decorations.left(), decorations.right(), decorations.back())
				.forEach(item -> list.add(new ItemStack(item, 1).getHoverName().plainCopy().withStyle(ChatFormatting.GRAY)));
		}
	}
}
