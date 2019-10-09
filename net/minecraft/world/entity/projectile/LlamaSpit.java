/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LlamaSpit
extends Entity
implements Projectile {
    public Llama owner;
    private CompoundTag ownerTag;

    public LlamaSpit(EntityType<? extends LlamaSpit> entityType, Level level) {
        super(entityType, level);
    }

    public LlamaSpit(Level level, Llama llama) {
        this((EntityType<? extends LlamaSpit>)EntityType.LLAMA_SPIT, level);
        this.owner = llama;
        this.setPos(llama.getX() - (double)(llama.getBbWidth() + 1.0f) * 0.5 * (double)Mth.sin(llama.yBodyRot * ((float)Math.PI / 180)), llama.getEyeY() - (double)0.1f, llama.getZ() + (double)(llama.getBbWidth() + 1.0f) * 0.5 * (double)Mth.cos(llama.yBodyRot * ((float)Math.PI / 180)));
    }

    @Environment(value=EnvType.CLIENT)
    public LlamaSpit(Level level, double d, double e, double f, double g, double h, double i) {
        this((EntityType<? extends LlamaSpit>)EntityType.LLAMA_SPIT, level);
        this.setPos(d, e, f);
        for (int j = 0; j < 7; ++j) {
            double k = 0.4 + 0.1 * (double)j;
            level.addParticle(ParticleTypes.SPIT, d, e, f, g * k, h, i * k);
        }
        this.setDeltaMovement(g, h, i);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ownerTag != null) {
            this.restoreOwnerFromSave();
        }
        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResult(this, this.getBoundingBox().expandTowards(vec3).inflate(1.0), entity -> !entity.isSpectator() && entity != this.owner, ClipContext.Block.OUTLINE, true);
        if (hitResult != null) {
            this.onHit(hitResult);
        }
        double d = this.getX() + vec3.x;
        double e = this.getY() + vec3.y;
        double f = this.getZ() + vec3.z;
        float g = Mth.sqrt(LlamaSpit.getHorizontalDistanceSqr(vec3));
        this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875);
        this.xRot = (float)(Mth.atan2(vec3.y, g) * 57.2957763671875);
        while (this.xRot - this.xRotO < -180.0f) {
            this.xRotO -= 360.0f;
        }
        while (this.xRot - this.xRotO >= 180.0f) {
            this.xRotO += 360.0f;
        }
        while (this.yRot - this.yRotO < -180.0f) {
            this.yRotO -= 360.0f;
        }
        while (this.yRot - this.yRotO >= 180.0f) {
            this.yRotO += 360.0f;
        }
        this.xRot = Mth.lerp(0.2f, this.xRotO, this.xRot);
        this.yRot = Mth.lerp(0.2f, this.yRotO, this.yRot);
        float h = 0.99f;
        float i = 0.06f;
        if (!this.level.containsMaterial(this.getBoundingBox(), Material.AIR)) {
            this.remove();
            return;
        }
        if (this.isInWaterOrBubble()) {
            this.remove();
            return;
        }
        this.setDeltaMovement(vec3.scale(0.99f));
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.06f, 0.0));
        }
        this.setPos(d, e, f);
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

    @Override
    public void shoot(double d, double e, double f, float g, float h) {
        Vec3 vec3 = new Vec3(d, e, f).normalize().add(this.random.nextGaussian() * (double)0.0075f * (double)h, this.random.nextGaussian() * (double)0.0075f * (double)h, this.random.nextGaussian() * (double)0.0075f * (double)h).scale(g);
        this.setDeltaMovement(vec3);
        float i = Mth.sqrt(LlamaSpit.getHorizontalDistanceSqr(vec3));
        this.yRot = (float)(Mth.atan2(vec3.x, f) * 57.2957763671875);
        this.xRot = (float)(Mth.atan2(vec3.y, i) * 57.2957763671875);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    public void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY && this.owner != null) {
            ((EntityHitResult)hitResult).getEntity().hurt(DamageSource.indirectMobAttack(this, this.owner).setProjectile(), 1.0f);
        } else if (type == HitResult.Type.BLOCK && !this.level.isClientSide) {
            this.remove();
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("Owner", 10)) {
            this.ownerTag = compoundTag.getCompound("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.owner != null) {
            CompoundTag compoundTag2 = new CompoundTag();
            UUID uUID = this.owner.getUUID();
            compoundTag2.putUUID("OwnerUUID", uUID);
            compoundTag.put("Owner", compoundTag2);
        }
    }

    private void restoreOwnerFromSave() {
        if (this.ownerTag != null && this.ownerTag.hasUUID("OwnerUUID")) {
            UUID uUID = this.ownerTag.getUUID("OwnerUUID");
            List<Llama> list = this.level.getEntitiesOfClass(Llama.class, this.getBoundingBox().inflate(15.0));
            for (Llama llama : list) {
                if (!llama.getUUID().equals(uUID)) continue;
                this.owner = llama;
                break;
            }
        }
        this.ownerTag = null;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

