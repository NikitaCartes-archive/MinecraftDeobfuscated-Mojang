/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
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
        Projectile entity = projectile;
        double d = livingEntity2.getX() - livingEntity.getX();
        double e = livingEntity2.getZ() - livingEntity.getZ();
        double h = Math.sqrt(d * d + e * e);
        double i = livingEntity2.getY(0.3333333333333333) - entity.getY() + h * (double)0.2f;
        Vector3f vector3f = this.getProjectileShotVector(livingEntity, new Vec3(d, i, e), f);
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), g, 14 - livingEntity.level.getDifficulty().getId() * 4);
        livingEntity.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.0f / (livingEntity.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    default public Vector3f getProjectileShotVector(LivingEntity livingEntity, Vec3 vec3, float f) {
        Vec3 vec32 = vec3.normalize();
        Vec3 vec33 = vec32.cross(new Vec3(0.0, 1.0, 0.0));
        if (vec33.lengthSqr() <= 1.0E-7) {
            vec33 = vec32.cross(livingEntity.getUpVector(1.0f));
        }
        Quaternion quaternion = new Quaternion(new Vector3f(vec33), 90.0f, true);
        Vector3f vector3f = new Vector3f(vec32);
        vector3f.transform(quaternion);
        Quaternion quaternion2 = new Quaternion(vector3f, f, true);
        Vector3f vector3f2 = new Vector3f(vec32);
        vector3f2.transform(quaternion2);
        return vector3f2;
    }
}

