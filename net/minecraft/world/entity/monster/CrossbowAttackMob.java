/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public interface CrossbowAttackMob
extends RangedAttackMob {
    public void setChargingCrossbow(boolean var1);

    public void shootCrossbowProjectile(LivingEntity var1, ItemStack var2, Projectile var3, float var4);

    @Nullable
    public LivingEntity getTarget();

    public void onCrossbowAttackPerformed();

    default public void performCrossbowAttack(LivingEntity livingEntity, float f) {
        InteractionHand interactionHand = ProjectileUtil.getWeaponHoldingHand(livingEntity, Items.CROSSBOW);
        ItemStack itemStack = livingEntity.getItemInHand(interactionHand);
        if (livingEntity.isHolding(Items.CROSSBOW)) {
            CrossbowItem.performShooting(livingEntity.level, livingEntity, interactionHand, itemStack, f, 14 - livingEntity.level.getDifficulty().getId() * 4);
        }
        this.onCrossbowAttackPerformed();
    }

    default public void shootCrossbowProjectile(LivingEntity livingEntity, LivingEntity livingEntity2, Projectile projectile, float f, float g) {
        double d = livingEntity2.getX() - livingEntity.getX();
        double e = livingEntity2.getZ() - livingEntity.getZ();
        double h = Math.sqrt(d * d + e * e);
        double i = livingEntity2.getY(0.3333333333333333) - projectile.getY() + h * (double)0.2f;
        Vector3f vector3f = this.getProjectileShotVector(livingEntity, new Vec3(d, i, e), f);
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), g, 14 - livingEntity.level.getDifficulty().getId() * 4);
        livingEntity.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.0f / (livingEntity.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    default public Vector3f getProjectileShotVector(LivingEntity livingEntity, Vec3 vec3, float f) {
        Vector3f vector3f = vec3.toVector3f().normalize();
        Vector3f vector3f2 = new Vector3f(vector3f).cross(new Vector3f(0.0f, 1.0f, 0.0f));
        if ((double)vector3f2.lengthSquared() <= 1.0E-7) {
            Vec3 vec32 = livingEntity.getUpVector(1.0f);
            vector3f2 = new Vector3f(vector3f).cross(vec32.toVector3f());
        }
        Vector3f vector3f3 = new Vector3f(vector3f).rotateAxis(1.5707964f, vector3f2.x, vector3f2.y, vector3f2.z);
        return new Vector3f(vector3f).rotateAxis(f * ((float)Math.PI / 180), vector3f3.x, vector3f3.y, vector3f3.z);
    }
}

