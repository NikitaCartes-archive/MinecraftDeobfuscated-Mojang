package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final float AABB_OFFSET = 4.0F;
	protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
	private final WoodType type;

	protected SignBlock(WoodType woodType, BlockBehaviour.Properties properties) {
		super(properties);
		this.type = woodType;
	}

	@Override
	protected abstract MapCodec<? extends SignBlock> codec();

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public boolean isPossibleToRespawnInThis(BlockState blockState) {
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SignBlockEntity(blockPos, blockState);
	}

	@Override
	protected ItemInteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		BlockEntity signApplicator2 = level.getBlockEntity(blockPos);
		if (signApplicator2 instanceof SignBlockEntity signBlockEntity) {
			Item bl2 = itemStack.getItem();
			SignApplicator signApplicator2x = bl2 instanceof SignApplicator signApplicator ? signApplicator : null;
			boolean bl = signApplicator2x != null && player.mayBuild();
			if (!level.isClientSide) {
				if (bl && !signBlockEntity.isWaxed() && !this.otherPlayerIsEditingSign(player, signBlockEntity)) {
					boolean bl2x = signBlockEntity.isFacingFrontText(player);
					if (signApplicator2x.canApplyToSign(signBlockEntity.getText(bl2x), player) && signApplicator2x.tryApplyToSign(level, signBlockEntity, bl2x, player)) {
						signBlockEntity.executeClickCommandsIfPresent(player, level, blockPos, bl2x);
						player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
						level.gameEvent(GameEvent.BLOCK_CHANGE, signBlockEntity.getBlockPos(), GameEvent.Context.of(player, signBlockEntity.getBlockState()));
						if (!player.isCreative()) {
							itemStack.shrink(1);
						}

						return ItemInteractionResult.SUCCESS;
					} else {
						return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
					}
				} else {
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				}
			} else {
				return !bl && !signBlockEntity.isWaxed() ? ItemInteractionResult.CONSUME : ItemInteractionResult.SUCCESS;
			}
		} else {
			return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		BlockEntity bl = level.getBlockEntity(blockPos);
		if (bl instanceof SignBlockEntity signBlockEntity) {
			if (level.isClientSide) {
				Util.pauseInIde(new IllegalStateException("Expected to only call this on server"));
			}

			boolean blx = signBlockEntity.isFacingFrontText(player);
			boolean bl2 = signBlockEntity.executeClickCommandsIfPresent(player, level, blockPos, blx);
			if (signBlockEntity.isWaxed()) {
				level.playSound(null, signBlockEntity.getBlockPos(), signBlockEntity.getSignInteractionFailedSoundEvent(), SoundSource.BLOCKS);
				return InteractionResult.SUCCESS;
			} else if (bl2) {
				return InteractionResult.SUCCESS;
			} else if (!this.otherPlayerIsEditingSign(player, signBlockEntity) && player.mayBuild() && this.hasEditableText(player, signBlockEntity, blx)) {
				this.openTextEdit(player, signBlockEntity, blx);
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.PASS;
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	private boolean hasEditableText(Player player, SignBlockEntity signBlockEntity, boolean bl) {
		SignText signText = signBlockEntity.getText(bl);
		return Arrays.stream(signText.getMessages(player.isTextFilteringEnabled()))
			.allMatch(component -> component.equals(CommonComponents.EMPTY) || component.getContents() instanceof PlainTextContents);
	}

	public abstract float getYRotationDegrees(BlockState blockState);

	public Vec3 getSignHitboxCenterPosition(BlockState blockState) {
		return new Vec3(0.5, 0.5, 0.5);
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
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
