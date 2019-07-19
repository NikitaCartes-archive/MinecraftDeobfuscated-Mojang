package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EmptyMapItem extends ComplexItem {
	public EmptyMapItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = MapItem.create(level, Mth.floor(player.x), Mth.floor(player.z), (byte)0, true, false);
		ItemStack itemStack2 = player.getItemInHand(interactionHand);
		if (!player.abilities.instabuild) {
			itemStack2.shrink(1);
		}

		if (itemStack2.isEmpty()) {
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
		} else {
			if (!player.inventory.add(itemStack.copy())) {
				player.drop(itemStack, false);
			}

			player.awardStat(Stats.ITEM_USED.get(this));
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack2);
		}
	}
}
