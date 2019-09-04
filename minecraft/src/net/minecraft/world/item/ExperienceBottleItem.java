package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.level.Level;

public class ExperienceBottleItem extends Item {
	public ExperienceBottleItem(Item.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		level.playSound(null, player.x, player.y, player.z, SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
		if (!level.isClientSide) {
			ThrownExperienceBottle thrownExperienceBottle = new ThrownExperienceBottle(level, player);
			thrownExperienceBottle.setItem(itemStack);
			thrownExperienceBottle.shootFromRotation(player, player.xRot, player.yRot, -20.0F, 0.7F, 1.0F);
			level.addFreshEntity(thrownExperienceBottle);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.abilities.instabuild) {
			itemStack.shrink(1);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
	}
}
