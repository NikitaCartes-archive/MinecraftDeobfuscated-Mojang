/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LlamaSpit
extends Projectile {
    public LlamaSpit(EntityType<? extends LlamaSpit> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    public LlamaSpit(Level level, Llama llama) {
        this((EntityType<? extends LlamaSpit>)EntityType.LLAMA_SPIT, level);
        super.setOwner(llama);
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
        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity, ClipContext.Block.OUTLINE);
        if (hitResult != null) {
            this.onHit(hitResult);
        }
        double d = this.getX() + vec3.x;
        double e = this.getY() + vec3.y;
        double f = this.getZ() + vec3.z;
        this.updateRotation();
        float g = 0.99f;
        float h = 0.06f;
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
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            entityHitResult.getEntity().hurt(DamageSource.indirectMobAttack(this, (LivingEntity)entity).setProjectile(), 1.0f);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (!this.level.isClientSide) {
            this.remove();
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

