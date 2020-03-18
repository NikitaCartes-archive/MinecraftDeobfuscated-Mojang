/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class Projectile
extends Entity {
    private UUID ownerUUID;

    Projectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public void setOwner(@Nullable Entity entity) {
        this.ownerUUID = entity == null ? null : entity.getUUID();
    }

    @Nullable
    public Entity getOwner() {
        if (this.ownerUUID != null && this.level instanceof ServerLevel) {
            return ((ServerLevel)this.level).getEntity(this.ownerUUID);
        }
        return null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
    }

    public void shoot(double d, double e, double f, float g, float h) {
        Vec3 vec3 = new Vec3(d, e, f).normalize().add(this.random.nextGaussian() * (double)0.0075f * (double)h, this.random.nextGaussian() * (double)0.0075f * (double)h, this.random.nextGaussian() * (double)0.0075f * (double)h).scale(g);
        this.setDeltaMovement(vec3);
        float i = Mth.sqrt(Projectile.getHorizontalDistanceSqr(vec3));
        this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875);
        this.xRot = (float)(Mth.atan2(vec3.y, i) * 57.2957763671875);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    public void shootFromRotation(Entity entity, float f, float g, float h, float i, float j) {
        float k = -Mth.sin(g * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        float l = -Mth.sin((f + h) * ((float)Math.PI / 180));
        float m = Mth.cos(g * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        this.shoot(k, l, m, i, j);
        Vec3 vec3 = entity.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.isOnGround() ? 0.0 : vec3.y, vec3.z));
    }

    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)hitResult);
        } else if (type == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult)hitResult);
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockHitResult blockHitResult2 = blockHitResult;
        BlockState blockState = this.level.getBlockState(blockHitResult2.getBlockPos());
        blockState.onProjectileHit(this.level, blockState, blockHitResult2, this);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void lerpMotion(double d, double e, double f) {
        this.setDeltaMovement(d, e, f);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            float g = Mth.sqrt(d * d + f * f);
            this.xRot = (float)(Mth.atan2(e, g) * 57.2957763671875);
            this.yRot = (float)(Mth.atan2(d, f) * 57.2957763671875);
            this.xRotO = this.xRot;
            this.yRotO = this.yRot;
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
        }
    }
}

