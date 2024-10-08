package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class NameTagItem extends Item {
	public NameTagItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		Component component = itemStack.get(DataComponents.CUSTOM_NAME);
		if (component != null && livingEntity.getType().canSerialize()) {
			if (!player.level().isClientSide && livingEntity.isAlive()) {
				livingEntity.setCustomName(component);
				if (livingEntity instanceof Mob mob) {
					mob.setPersistenceRequired();
				}

				itemStack.shrink(1);
			}

			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}
}
