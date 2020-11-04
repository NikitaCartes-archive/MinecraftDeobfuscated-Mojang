package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EmptyMapItem extends ComplexItem {
	public EmptyMapItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = MapItem.create(level, player.getBlockX(), player.getBlockZ(), (byte)0, true, false);
		ItemStack itemStack2 = player.getItemInHand(interactionHand);
		if (!player.getAbilities().instabuild) {
			itemStack2.shrink(1);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		player.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
		if (itemStack2.isEmpty()) {
			return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
		} else {
			if (!player.getInventory().add(itemStack.copy())) {
				player.drop(itemStack, false);
			}

			return InteractionResultHolder.sidedSuccess(itemStack2, level.isClientSide());
		}
	}
}
