package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity & ItemSteerable> extends Item {
	private final EntityType<T> canInteractWith;
	private final int consumeItemDamage;

	public FoodOnAStickItem(Item.Properties properties, EntityType<T> entityType, int i) {
		super(properties);
		this.canInteractWith = entityType;
		this.consumeItemDamage = i;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (level.isClientSide) {
			return InteractionResultHolder.pass(itemStack);
		} else {
			Entity entity = player.getVehicle();
			if (player.isPassenger() && entity instanceof ItemSteerable && entity.getType() == this.canInteractWith) {
				ItemSteerable itemSteerable = (ItemSteerable)entity;
				if (itemSteerable.boost()) {
					itemStack.hurtAndBreak(this.consumeItemDamage, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
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
