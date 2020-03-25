package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerableMount;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity & ItemSteerableMount> extends Item {
	private final EntityType<T> canInteractWith;

	public FoodOnAStickItem(Item.Properties properties, EntityType<T> entityType) {
		super(properties);
		this.canInteractWith = entityType;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (level.isClientSide) {
			return InteractionResultHolder.pass(itemStack);
		} else {
			Entity entity = player.getVehicle();
			if (player.isPassenger() && entity instanceof ItemSteerableMount && entity.getType() == this.canInteractWith) {
				ItemSteerableMount itemSteerableMount = (ItemSteerableMount)entity;
				if (itemSteerableMount.boost()) {
					itemStack.hurtAndBreak(7, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
					player.swing(interactionHand, true);
					if (itemStack.isEmpty()) {
						ItemStack itemStack2 = new ItemStack(Items.FISHING_ROD);
						itemStack2.setTag(itemStack.getTag());
						return InteractionResultHolder.consume(itemStack2);
					}

					return InteractionResultHolder.consume(itemStack);
				}
			}

			player.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResultHolder.pass(itemStack);
		}
	}
}
