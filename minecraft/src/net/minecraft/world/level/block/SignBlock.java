package net.minecraft.world.level.block;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final float AABB_OFFSET = 4.0F;
	protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
	private final WoodType type;

	protected SignBlock(BlockBehaviour.Properties properties, WoodType woodType) {
		super(properties);
		this.type = woodType;
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
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public boolean isPossibleToRespawnInThis() {
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SignBlockEntity(blockPos, blockState);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Item item = itemStack.getItem();
		SignApplicator signApplicator2 = itemStack.getItem() instanceof SignApplicator signApplicator ? signApplicator : null;
		boolean bl = signApplicator2 != null && player.getAbilities().mayBuild;
		if (level.isClientSide) {
			return bl ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
		} else if (level.getBlockEntity(blockPos) instanceof SignBlockEntity signBlockEntity) {
			boolean bl2 = signBlockEntity.isFacingFrontText(player);
			SignText signText = signBlockEntity.getText(bl2);
			if (signBlockEntity.isWaxed()) {
				boolean bl3 = signText.executeClickCommandsIfPresent((ServerPlayer)player, (ServerLevel)level, blockPos);
				if (!bl3) {
					level.playSound(null, signBlockEntity.getBlockPos(), SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
				}

				return InteractionResult.SUCCESS;
			} else if (bl
				&& !this.otherPlayerIsEditingSign(player, signBlockEntity)
				&& signApplicator2.canApplyToSign(signText, player)
				&& signApplicator2.tryApplyToSign(level, signBlockEntity, bl2, player)) {
				if (!player.isCreative()) {
					itemStack.shrink(1);
				}

				if (player instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack);
				}

				level.gameEvent(GameEvent.BLOCK_CHANGE, signBlockEntity.getBlockPos(), GameEvent.Context.of(player, signBlockEntity.getBlockState()));
				player.awardStat(Stats.ITEM_USED.get(item));
				return InteractionResult.SUCCESS;
			} else if (!this.otherPlayerIsEditingSign(player, signBlockEntity)) {
				this.openTextEdit(player, signBlockEntity, bl2);
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.PASS;
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	public abstract float getYRotationDegrees(BlockState blockState);

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	public WoodType type() {
		return this.type;
	}

	public static WoodType getWoodType(Block block) {
		WoodType woodType;
		if (block instanceof SignBlock) {
			woodType = ((SignBlock)block).type();
		} else {
			woodType = WoodType.OAK;
		}

		return woodType;
	}

	public void openTextEdit(Player player, SignBlockEntity signBlockEntity, boolean bl) {
		signBlockEntity.setAllowedPlayerEditor(player.getUUID());
		player.openTextEdit(signBlockEntity, bl);
	}

	private boolean otherPlayerIsEditingSign(Player player, SignBlockEntity signBlockEntity) {
		UUID uUID = signBlockEntity.getPlayerWhoMayEdit();
		return uUID != null && !uUID.equals(player.getUUID());
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, BlockEntityType.SIGN, SignBlockEntity::tick);
	}
}
