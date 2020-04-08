package net.minecraft.world.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.player.Player;

public class SaddleItem extends Item {
	public SaddleItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean interactEnemy(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		if (livingEntity instanceof Saddleable && livingEntity.isAlive()) {
			Saddleable saddleable = (Saddleable)livingEntity;
			if (!saddleable.isSaddled() && saddleable.isSaddleable()) {
				saddleable.equipSaddle(SoundSource.NEUTRAL);
				itemStack.shrink(1);
				return true;
			}
		}

		return false;
	}
}
