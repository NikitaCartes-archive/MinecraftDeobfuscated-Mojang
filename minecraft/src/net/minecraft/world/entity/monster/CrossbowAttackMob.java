package net.minecraft.world.entity.monster;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public interface CrossbowAttackMob extends RangedAttackMob {
	void setChargingCrossbow(boolean bl);

	void shootCrossbowProjectile(LivingEntity livingEntity, ItemStack itemStack, Projectile projectile, float f);

	@Nullable
	LivingEntity getTarget();

	void onCrossbowAttackPerformed();

	default void performCrossbowAttack(LivingEntity livingEntity, float f) {
		InteractionHand interactionHand = ProjectileUtil.getWeaponHoldingHand(livingEntity, Items.CROSSBOW);
		ItemStack itemStack = livingEntity.getItemInHand(interactionHand);
		if (livingEntity.isHolding(Items.CROSSBOW)) {
			CrossbowItem.performShooting(livingEntity.level, livingEntity, interactionHand, itemStack, f, (float)(14 - livingEntity.level.getDifficulty().getId() * 4));
		}

		this.onCrossbowAttackPerformed();
	}

	default void shootCrossbowProjectile(LivingEntity livingEntity, LivingEntity livingEntity2, Projectile projectile, float f, float g) {
		double d = livingEntity2.getX() - livingEntity.getX();
		double e = livingEntity2.getZ() - livingEntity.getZ();
		double h = Math.sqrt(d * d + e * e);
		double i = livingEntity2.getY(0.3333333333333333) - projectile.getY() + h * 0.2F;
		Vector3f vector3f = this.getProjectileShotVector(livingEntity, new Vec3(d, i, e), f);
		projectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), g, (float)(14 - livingEntity.level.getDifficulty().getId() * 4));
		livingEntity.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (livingEntity.getRandom().nextFloat() * 0.4F + 0.8F));
	}

	default Vector3f getProjectileShotVector(LivingEntity livingEntity, Vec3 vec3, float f) {
		Vec3 vec32 = vec3.normalize();
		Vec3 vec33 = vec32.cross(new Vec3(0.0, 1.0, 0.0));
		if (vec33.lengthSqr() <= 1.0E-7) {
			vec33 = vec32.cross(livingEntity.getUpVector(1.0F));
		}

		Quaternion quaternion = new Quaternion(new Vector3f(vec33), 90.0F, true);
		Vector3f vector3f = new Vector3f(vec32);
		vector3f.transform(quaternion);
		Quaternion quaternion2 = new Quaternion(vector3f, f, true);
		Vector3f vector3f2 = new Vector3f(vec32);
		vector3f2.transform(quaternion2);
		return vector3f2;
	}
}
