package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CarrotOnAStickItem extends Item {
	public CarrotOnAStickItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (level.isClientSide) {
			return InteractionResultHolder.pass(itemStack);
		} else {
			if (player.isPassenger() && player.getVehicle() instanceof Pig) {
				Pig pig = (Pig)player.getVehicle();
				if (itemStack.getMaxDamage() - itemStack.getDamageValue() >= 7 && pig.boost()) {
					itemStack.hurtAndBreak(7, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
					if (itemStack.isEmpty()) {
						ItemStack itemStack2 = new ItemStack(Items.FISHING_ROD);
						itemStack2.setTag(itemStack.getTag());
						return InteractionResultHolder.success(itemStack2);
					}

					return InteractionResultHolder.success(itemStack);
				}
			}

			player.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResultHolder.pass(itemStack);
		}
	}
}
