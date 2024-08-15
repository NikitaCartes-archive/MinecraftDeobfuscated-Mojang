package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EmptyMapItem extends ComplexItem {
	public EmptyMapItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			itemStack.consume(1, player);
			player.awardStat(Stats.ITEM_USED.get(this));
			player.level().playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);
			ItemStack itemStack2 = MapItem.create(level, player.getBlockX(), player.getBlockZ(), (byte)0, true, false);
			if (itemStack.isEmpty()) {
				return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack2);
			} else {
				if (!player.getInventory().add(itemStack2.copy())) {
					player.drop(itemStack2, false);
				}

				return InteractionResult.SUCCESS;
			}
		}
	}
}
