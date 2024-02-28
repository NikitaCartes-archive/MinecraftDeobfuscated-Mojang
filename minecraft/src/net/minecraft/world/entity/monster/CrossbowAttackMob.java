package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface CrossbowAttackMob extends RangedAttackMob {
	void setChargingCrossbow(boolean bl);

	@Nullable
	LivingEntity getTarget();

	void onCrossbowAttackPerformed();

	default void performCrossbowAttack(LivingEntity livingEntity, float f) {
		InteractionHand interactionHand = ProjectileUtil.getWeaponHoldingHand(livingEntity, Items.CROSSBOW);
		ItemStack itemStack = livingEntity.getItemInHand(interactionHand);
		if (itemStack.getItem() instanceof CrossbowItem crossbowItem) {
			crossbowItem.performShooting(
				livingEntity.level(), livingEntity, interactionHand, itemStack, f, (float)(14 - livingEntity.level().getDifficulty().getId() * 4), this.getTarget()
			);
		}

		this.onCrossbowAttackPerformed();
	}
}
