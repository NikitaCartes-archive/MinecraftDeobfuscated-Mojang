package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DecoratedPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<DecoratedPotBlock> CODEC = simpleCodec(DecoratedPotBlock::new);
	public static final ResourceLocation SHERDS_DYNAMIC_DROP_ID = new ResourceLocation("sherds");
	private static final VoxelShape BOUNDING_BOX = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty CRACKED = BlockStateProperties.CRACKED;
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	@Override
	public MapCodec<DecoratedPotBlock> codec() {
		return CODEC;
	}

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
	protected BlockState updateShape(
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
	protected ItemInteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (level.getBlockEntity(blockPos) instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
			if (level.isClientSide) {
				return ItemInteractionResult.CONSUME;
			} else {
				ItemStack itemStack2 = decoratedPotBlockEntity.getTheItem();
				if (!itemStack.isEmpty()
					&& (itemStack2.isEmpty() || ItemStack.isSameItemSameTags(itemStack2, itemStack) && itemStack2.getCount() < itemStack2.getMaxStackSize())) {
					decoratedPotBlockEntity.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
					player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
					ItemStack itemStack3 = player.isCreative() ? itemStack.copyWithCount(1) : itemStack.split(1);
					float f;
					if (decoratedPotBlockEntity.isEmpty()) {
						decoratedPotBlockEntity.setTheItem(itemStack3);
						f = (float)itemStack3.getCount() / (float)itemStack3.getMaxStackSize();
					} else {
						itemStack2.grow(1);
						f = (float)itemStack2.getCount() / (float)itemStack2.getMaxStackSize();
					}

					level.playSound(null, blockPos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * f);
					if (level instanceof ServerLevel serverLevel) {
						serverLevel.sendParticles(
							ParticleTypes.DUST_PLUME, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0
						);
					}

					decoratedPotBlockEntity.setChanged();
					level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
					return ItemInteractionResult.SUCCESS;
				} else {
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				}
			}
		} else {
			return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.getBlockEntity(blockPos) instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
			level.playSound(null, blockPos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
			decoratedPotBlockEntity.wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		if (level.isClientSide) {
			level.getBlockEntity(blockPos, BlockEntityType.DECORATED_POT).ifPresent(decoratedPotBlockEntity -> decoratedPotBlockEntity.setFromItem(itemStack));
		}
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
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
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		Containers.dropContentsOnDestroy(blockState, blockState2, level, blockPos);
		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
			builder.withDynamicDrop(
				SHERDS_DYNAMIC_DROP_ID, consumer -> decoratedPotBlockEntity.getDecorations().sorted().map(Item::getDefaultInstance).forEach(consumer)
			);
		}

		return super.getDrops(blockState, builder);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		ItemStack itemStack = player.getMainHandItem();
		BlockState blockState2 = blockState;
		if (itemStack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(itemStack)) {
			blockState2 = blockState.setValue(CRACKED, Boolean.valueOf(true));
			level.setBlock(blockPos, blockState2, 4);
		}

		return super.playerWillDestroy(level, blockPos, blockState2, player);
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	protected SoundType getSoundType(BlockState blockState) {
		return blockState.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
	}

	@Override
	public void appendHoverText(
		ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag, @Nullable RegistryAccess registryAccess
	) {
		super.appendHoverText(itemStack, blockGetter, list, tooltipFlag, registryAccess);
		DecoratedPotBlockEntity.Decorations decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(itemStack));
		if (!decorations.equals(DecoratedPotBlockEntity.Decorations.EMPTY)) {
			list.add(CommonComponents.EMPTY);
			Stream.of(decorations.front(), decorations.left(), decorations.right(), decorations.back())
				.forEach(item -> list.add(new ItemStack(item, 1).getHoverName().plainCopy().withStyle(ChatFormatting.GRAY)));
		}
	}

	@Override
	protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		BlockPos blockPos = blockHitResult.getBlockPos();
		if (!level.isClientSide && projectile.mayInteract(level, blockPos) && projectile.mayBreak(level)) {
			level.setBlock(blockPos, blockState.setValue(CRACKED, Boolean.valueOf(true)), 4);
			level.destroyBlock(blockPos, true, projectile);
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return levelReader.getBlockEntity(blockPos) instanceof DecoratedPotBlockEntity decoratedPotBlockEntity
			? decoratedPotBlockEntity.getPotAsItem()
			: super.getCloneItemStack(levelReader, blockPos, blockState);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
	}
}
