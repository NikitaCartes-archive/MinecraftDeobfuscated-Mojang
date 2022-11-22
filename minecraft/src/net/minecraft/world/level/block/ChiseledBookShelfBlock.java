package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChiseledBookShelfBlock extends BaseEntityBlock {
	private static final int MAX_BOOKS_IN_STORAGE = 6;
	public static final int BOOKS_PER_ROW = 3;
	public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(
		BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED,
		BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED,
		BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED,
		BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED,
		BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED,
		BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED
	);

	public ChiseledBookShelfBlock(BlockBehaviour.Properties properties) {
		super(properties);
		BlockState blockState = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

		for (BooleanProperty booleanProperty : SLOT_OCCUPIED_PROPERTIES) {
			blockState = blockState.setValue(booleanProperty, Boolean.valueOf(false));
		}

		this.registerDefaultState(blockState);
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
			Optional<Vec2> optional = getRelativeHitCoordinatesForBlockFace(blockHitResult, blockState.getValue(HorizontalDirectionalBlock.FACING));
			if (optional.isEmpty()) {
				return InteractionResult.PASS;
			} else {
				int i = getHitSlot((Vec2)optional.get());
				if ((Boolean)blockState.getValue((Property)SLOT_OCCUPIED_PROPERTIES.get(i))) {
					removeBook(level, blockPos, player, chiseledBookShelfBlockEntity, i);
					return InteractionResult.sidedSuccess(level.isClientSide);
				} else {
					ItemStack itemStack = player.getItemInHand(interactionHand);
					if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
						addBook(level, blockPos, player, chiseledBookShelfBlockEntity, itemStack, i);
						return InteractionResult.sidedSuccess(level.isClientSide);
					} else {
						return InteractionResult.CONSUME;
					}
				}
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult blockHitResult, Direction direction) {
		Direction direction2 = blockHitResult.getDirection();
		if (direction != direction2) {
			return Optional.empty();
		} else {
			BlockPos blockPos = blockHitResult.getBlockPos().relative(direction2);
			Vec3 vec3 = blockHitResult.getLocation().subtract((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
			double d = vec3.x();
			double e = vec3.y();
			double f = vec3.z();

			return switch (direction2) {
				case NORTH -> Optional.of(new Vec2((float)(1.0 - d), (float)e));
				case SOUTH -> Optional.of(new Vec2((float)d, (float)e));
				case WEST -> Optional.of(new Vec2((float)f, (float)e));
				case EAST -> Optional.of(new Vec2((float)(1.0 - f), (float)e));
				case DOWN, UP -> Optional.empty();
			};
		}
	}

	private static int getHitSlot(Vec2 vec2) {
		int i = vec2.y >= 0.5F ? 0 : 1;
		int j = getSection(vec2.x);
		return j + i * 3;
	}

	private static int getSection(float f) {
		float g = 0.0625F;
		float h = 0.375F;
		if (f < 0.375F) {
			return 0;
		} else {
			float i = 0.6875F;
			return f < 0.6875F ? 1 : 2;
		}
	}

	private static void addBook(
		Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, ItemStack itemStack, int i
	) {
		if (!level.isClientSide) {
			player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
			SoundEvent soundEvent = itemStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
			chiseledBookShelfBlockEntity.setItem(i, itemStack.split(1));
			level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (player.isCreative()) {
				itemStack.grow(1);
			}

			level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
		}
	}

	private static void removeBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, int i) {
		if (!level.isClientSide) {
			ItemStack itemStack = chiseledBookShelfBlockEntity.removeItem(i, 1);
			SoundEvent soundEvent = itemStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
			level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (!player.getInventory().add(itemStack)) {
				player.drop(itemStack, false);
			}

			level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new ChiseledBookShelfBlockEntity(blockPos, blockState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING);
		SLOT_OCCUPIED_PROPERTIES.forEach(property -> builder.add(property));
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if (level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity && !chiseledBookShelfBlockEntity.isEmpty()) {
				for (int i = 0; i < 6; i++) {
					ItemStack itemStack = chiseledBookShelfBlockEntity.getItem(i);
					if (!itemStack.isEmpty()) {
						Containers.dropItemStack(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack);
					}
				}

				chiseledBookShelfBlockEntity.clearContent();
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
		if (level.isClientSide()) {
			return 0;
		} else {
			return level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity
				? chiseledBookShelfBlockEntity.getLastInteractedSlot() + 1
				: 0;
		}
	}
}
