package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

public interface CrossbowAttackMob {
	void setChargingCrossbow(boolean bl);

	void shootProjectile(LivingEntity livingEntity, ItemStack itemStack, Projectile projectile, float f);

	@Nullable
	LivingEntity getTarget();
}
