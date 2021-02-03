package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem extends Item {
	private final EntityType<? extends HangingEntity> type;

	public HangingEntityItem(EntityType<? extends HangingEntity> entityType, Item.Properties properties) {
		super(properties);
		this.type = entityType;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		BlockPos blockPos = useOnContext.getClickedPos();
		Direction direction = useOnContext.getClickedFace();
		BlockPos blockPos2 = blockPos.relative(direction);
		Player player = useOnContext.getPlayer();
		ItemStack itemStack = useOnContext.getItemInHand();
		if (player != null && !this.mayPlace(player, direction, itemStack, blockPos2)) {
			return InteractionResult.FAIL;
		} else {
			Level level = useOnContext.getLevel();
			HangingEntity hangingEntity;
			if (this.type == EntityType.PAINTING) {
				hangingEntity = new Painting(level, blockPos2, direction);
			} else if (this.type == EntityType.ITEM_FRAME) {
				hangingEntity = new ItemFrame(level, blockPos2, direction);
			} else {
				if (this.type != EntityType.GLOW_ITEM_FRAME) {
					return InteractionResult.sidedSuccess(level.isClientSide);
				}

				hangingEntity = new ItemFrame(EntityType.GLOW_ITEM_FRAME, level, blockPos2, direction);
			}

			CompoundTag compoundTag = itemStack.getTag();
			if (compoundTag != null) {
				EntityType.updateCustomEntityTag(level, player, hangingEntity, compoundTag);
			}

			if (hangingEntity.survives()) {
				if (!level.isClientSide) {
					hangingEntity.playPlacementSound();
					level.gameEvent(player, GameEvent.ENTITY_PLACE, blockPos);
					level.addFreshEntity(hangingEntity);
				}

				itemStack.shrink(1);
				return InteractionResult.sidedSuccess(level.isClientSide);
			} else {
				return InteractionResult.CONSUME;
			}
		}
	}

	protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
		return !direction.getAxis().isVertical() && player.mayUseItemAt(blockPos, direction, itemStack);
	}
}
