package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemUtils {
	public static InteractionResultHolder<ItemStack> useDrink(Level level, Player player, InteractionHand interactionHand) {
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.consume(player.getItemInHand(interactionHand));
	}
}
