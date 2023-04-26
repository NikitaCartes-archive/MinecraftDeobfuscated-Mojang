package net.minecraft.world.item;

import java.util.stream.Stream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemUtils {
	public static InteractionResultHolder<ItemStack> startUsingInstantly(Level level, Player player, InteractionHand interactionHand) {
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.consume(player.getItemInHand(interactionHand));
	}

	public static ItemStack createFilledResult(ItemStack itemStack, Player player, ItemStack itemStack2, boolean bl) {
		boolean bl2 = player.getAbilities().instabuild;
		if (bl && bl2) {
			if (!player.getInventory().contains(itemStack2)) {
				player.getInventory().add(itemStack2);
			}

			return itemStack;
		} else {
			if (!bl2) {
				itemStack.shrink(1);
			}

			if (itemStack.isEmpty()) {
				return itemStack2;
			} else {
				if (!player.getInventory().add(itemStack2)) {
					player.drop(itemStack2, false);
				}

				return itemStack;
			}
		}
	}

	public static ItemStack createFilledResult(ItemStack itemStack, Player player, ItemStack itemStack2) {
		return createFilledResult(itemStack, player, itemStack2, true);
	}

	public static void onContainerDestroyed(ItemEntity itemEntity, Stream<ItemStack> stream) {
		Level level = itemEntity.level();
		if (!level.isClientSide) {
			stream.forEach(itemStack -> level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), itemStack)));
		}
	}
}
