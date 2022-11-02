package net.minecraft.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ChiseledBookShelfBlock extends BaseEntityBlock {
	public static final IntegerProperty BOOKS_STORED = BlockStateProperties.BOOKS_STORED;
	public static final IntegerProperty LAST_INTERACTION_BOOK_SLOT = BlockStateProperties.LAST_INTERACTION_BOOK_SLOT;

	public ChiseledBookShelfBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(BOOKS_STORED, Integer.valueOf(0))
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
				.setValue(LAST_INTERACTION_BOOK_SLOT, Integer.valueOf(0))
		);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity) {
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			} else {
				ItemStack itemStack = player.getItemInHand(interactionHand);
				return itemStack.is(ItemTags.BOOKSHELF_BOOKS)
					? tryAddBook(level, blockPos, player, chiseledBookShelfBlockEntity, itemStack)
					: tryRemoveBook(level, blockPos, player, chiseledBookShelfBlockEntity);
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	private static InteractionResult tryRemoveBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity) {
		if (!chiseledBookShelfBlockEntity.isEmpty()) {
			ItemStack itemStack = chiseledBookShelfBlockEntity.removeBook();
			level.playSound(null, blockPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
			if (!player.getInventory().add(itemStack)) {
				player.drop(itemStack, false);
			}
		}

		return InteractionResult.CONSUME;
	}

	private static InteractionResult tryAddBook(
		Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, ItemStack itemStack
	) {
		if (chiseledBookShelfBlockEntity.addBook(itemStack.split(1))) {
			level.playSound(null, blockPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (player.isCreative()) {
				itemStack.grow(1);
			}

			level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
		} else {
			itemStack.grow(1);
		}

		return InteractionResult.CONSUME;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new ChiseledBookShelfBlockEntity(blockPos, blockState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOOKS_STORED).add(LAST_INTERACTION_BOOK_SLOT).add(HorizontalDirectionalBlock.FACING);
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if (level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity) {
				List<ItemStack> list = chiseledBookShelfBlockEntity.removeAllBooksWithoutBlockStateUpdate();
				list.forEach(itemStack -> Containers.dropItemStack(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack));
				level.updateNeighbourForOutputSignal(blockPos, this);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return (Integer)blockState.getValue(LAST_INTERACTION_BOOK_SLOT);
	}
}
