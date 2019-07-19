package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
			return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
		} else {
			if (player.isPassenger() && player.getVehicle() instanceof Pig) {
				Pig pig = (Pig)player.getVehicle();
				if (itemStack.getMaxDamage() - itemStack.getDamageValue() >= 7 && pig.boost()) {
					itemStack.hurtAndBreak(7, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
					if (itemStack.isEmpty()) {
						ItemStack itemStack2 = new ItemStack(Items.FISHING_ROD);
						itemStack2.setTag(itemStack.getTag());
						return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack2);
					}

					return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
				}
			}

			player.awardStat(Stats.ITEM_USED.get(this));
			return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
		}
	}
}
