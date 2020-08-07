package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

public class LeadItem extends Item {
	public LeadItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		Block block = level.getBlockState(blockPos).getBlock();
		if (block.is(BlockTags.FENCES)) {
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
		boolean bl = false;
		double d = 7.0;
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();

		for (Mob mob : level.getEntitiesOfClass(
			Mob.class, new AABB((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0)
		)) {
			if (mob.getLeashHolder() == player) {
				if (leashFenceKnotEntity == null) {
					leashFenceKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(level, blockPos);
				}

				mob.setLeashedTo(leashFenceKnotEntity, true);
				bl = true;
			}
		}

		return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}
}
