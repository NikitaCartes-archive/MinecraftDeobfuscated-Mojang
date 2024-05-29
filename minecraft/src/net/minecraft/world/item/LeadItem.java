package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class LeadItem extends Item {
	public LeadItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.is(BlockTags.FENCES)) {
			Player player = useOnContext.getPlayer();
			if (!level.isClientSide && player != null) {
				bindPlayerMobs(player, level, blockPos);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos blockPos) {
		LeashFenceKnotEntity leashFenceKnotEntity = null;
		double d = 7.0;
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		AABB aABB = new AABB((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0);
		List<Entity> list = level.getEntitiesOfClass(Entity.class, aABB, entityx -> {
			if (entityx instanceof Leashable leashable && leashable.getLeashHolder() == player) {
				return true;
			}

			return false;
		});

		for (Entity entity : list) {
			if (leashFenceKnotEntity == null) {
				leashFenceKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(level, blockPos);
				leashFenceKnotEntity.playPlacementSound();
			}

			((Leashable)entity).setLeashedTo(leashFenceKnotEntity, true);
		}

		if (!list.isEmpty()) {
			level.gameEvent(GameEvent.BLOCK_ATTACH, blockPos, GameEvent.Context.of(player));
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}
}
