package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
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

public class ChiseledBookShelfBlock extends BaseEntityBlock {
	public static final MapCodec<ChiseledBookShelfBlock> CODEC = simpleCodec(ChiseledBookShelfBlock::new);
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

	@Override
	public MapCodec<ChiseledBookShelfBlock> codec() {
		return CODEC;
	}

	public ChiseledBookShelfBlock(BlockBehaviour.Properties properties) {
		super(properties);
		BlockState blockState = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

		for (BooleanProperty booleanProperty : SLOT_OCCUPIED_PROPERTIES) {
			blockState = blockState.setValue(booleanProperty, Boolean.valueOf(false));
		}

		this.registerDefaultState(blockState);
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected InteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity) {
			if (!itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			} else {
				OptionalInt optionalInt = this.getHitSlot(blockHitResult, blockState);
				if (optionalInt.isEmpty()) {
					return InteractionResult.PASS;
				} else if ((Boolean)blockState.getValue((Property)SLOT_OCCUPIED_PROPERTIES.get(optionalInt.getAsInt()))) {
					return InteractionResult.TRY_WITH_EMPTY_HAND;
				} else {
					addBook(level, blockPos, player, chiseledBookShelfBlockEntity, itemStack, optionalInt.getAsInt());
					return InteractionResult.SUCCESS;
				}
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity) {
			OptionalInt optionalInt = this.getHitSlot(blockHitResult, blockState);
			if (optionalInt.isEmpty()) {
				return InteractionResult.PASS;
			} else if (!(Boolean)blockState.getValue((Property)SLOT_OCCUPIED_PROPERTIES.get(optionalInt.getAsInt()))) {
				return InteractionResult.CONSUME;
			} else {
				removeBook(level, blockPos, player, chiseledBookShelfBlockEntity, optionalInt.getAsInt());
				return InteractionResult.SUCCESS;
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	private OptionalInt getHitSlot(BlockHitResult blockHitResult, BlockState blockState) {
		return (OptionalInt)getRelativeHitCoordinatesForBlockFace(blockHitResult, blockState.getValue(HorizontalDirectionalBlock.FACING)).map(vec2 -> {
			int i = vec2.y >= 0.5F ? 0 : 1;
			int j = getSection(vec2.x);
			return OptionalInt.of(j + i * 3);
		}).orElseGet(OptionalInt::empty);
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
			chiseledBookShelfBlockEntity.setItem(i, itemStack.consumeAndReturn(1, player));
			level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
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
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			boolean bl2;
			label32: {
				if (level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity && !chiseledBookShelfBlockEntity.isEmpty()) {
					for (int i = 0; i < 6; i++) {
						ItemStack itemStack = chiseledBookShelfBlockEntity.getItem(i);
						if (!itemStack.isEmpty()) {
							Containers.dropItemStack(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack);
						}
					}

					chiseledBookShelfBlockEntity.clearContent();
					bl2 = true;
					break label32;
				}

				bl2 = false;
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
			if (bl2) {
				level.updateNeighbourForOutputSignal(blockPos, this);
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(blockState.getValue(HorizontalDirectionalBlock.FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(HorizontalDirectionalBlock.FACING)));
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		if (level.isClientSide()) {
			return 0;
		} else {
			return level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity
				? chiseledBookShelfBlockEntity.getLastInteractedSlot() + 1
				: 0;
		}
	}
}
