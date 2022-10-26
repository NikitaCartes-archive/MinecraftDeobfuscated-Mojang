package net.minecraft.world.entity.monster;

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
import org.joml.Vector3f;

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
		Vector3f vector3f = vec3.toVector3f().normalize();
		Vector3f vector3f2 = vector3f.cross(new Vector3f(0.0F, 1.0F, 0.0F));
		if ((double)vector3f2.lengthSquared() <= 1.0E-7) {
			Vec3 vec32 = livingEntity.getUpVector(1.0F);
			vector3f2 = vector3f.cross(vec32.toVector3f());
		}

		Vector3f vector3f3 = new Vector3f(vector3f).rotateAxis((float) (Math.PI / 2), vector3f2.x, vector3f2.y, vector3f2.z);
		return new Vector3f(vector3f).rotateAxis(f * (float) (Math.PI / 180.0), vector3f3.x, vector3f3.y, vector3f3.z);
	}
}
