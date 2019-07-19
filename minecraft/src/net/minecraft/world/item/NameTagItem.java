package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class NameTagItem extends Item {
	public NameTagItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean interactEnemy(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		if (itemStack.hasCustomHoverName() && !(livingEntity instanceof Player)) {
			if (livingEntity.isAlive()) {
				livingEntity.setCustomName(itemStack.getHoverName());
				if (livingEntity instanceof Mob) {
					((Mob)livingEntity).setPersistenceRequired();
				}

				itemStack.shrink(1);
			}

			return true;
		} else {
			return false;
		}
	}
}
