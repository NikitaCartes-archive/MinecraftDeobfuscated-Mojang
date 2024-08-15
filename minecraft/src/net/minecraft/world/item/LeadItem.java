package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
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
				return bindPlayerMobs(player, level, blockPos);
			}
		}

		return InteractionResult.PASS;
	}

	public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos blockPos) {
		LeashFenceKnotEntity leashFenceKnotEntity = null;
		List<Leashable> list = leashableInArea(level, blockPos, leashablex -> leashablex.getLeashHolder() == player);

		for (Leashable leashable : list) {
			if (leashFenceKnotEntity == null) {
				leashFenceKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(level, blockPos);
				leashFenceKnotEntity.playPlacementSound();
			}

			leashable.setLeashedTo(leashFenceKnotEntity, true);
		}

		if (!list.isEmpty()) {
			level.gameEvent(GameEvent.BLOCK_ATTACH, blockPos, GameEvent.Context.of(player));
			return InteractionResult.SUCCESS_SERVER;
		} else {
			return InteractionResult.PASS;
		}
	}

	public static List<Leashable> leashableInArea(Level level, BlockPos blockPos, Predicate<Leashable> predicate) {
		double d = 7.0;
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		AABB aABB = new AABB((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0);
		return level.getEntitiesOfClass(Entity.class, aABB, entity -> {
			if (entity instanceof Leashable leashable && predicate.test(leashable)) {
				return true;
			}

			return false;
		}).stream().map(Leashable.class::cast).toList();
	}
}
