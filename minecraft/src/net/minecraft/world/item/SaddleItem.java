package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;

public class SaddleItem extends Item {
	public SaddleItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean interactEnemy(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		if (livingEntity instanceof Pig) {
			Pig pig = (Pig)livingEntity;
			if (pig.isAlive() && !pig.hasSaddle() && !pig.isBaby()) {
				pig.setSaddle(true);
				pig.level.playSound(player, pig.getX(), pig.getY(), pig.getZ(), SoundEvents.PIG_SADDLE, SoundSource.NEUTRAL, 0.5F, 1.0F);
				itemStack.shrink(1);
			}

			return true;
		} else {
			return false;
		}
	}
}
