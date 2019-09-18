/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class ThrowableProjectile
extends Entity
implements Projectile {
    private int xBlock = -1;
    private int yBlock = -1;
    private int zBlock = -1;
    protected boolean inGround;
    public int shakeTime;
    protected LivingEntity owner;
    private UUID ownerId;
    private Entity entityToIgnore;
    private int timeToIgnore;

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, double d, double e, double f, Level level) {
        this(entityType, level);
        this.setPos(d, e, f);
    }

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, LivingEntity livingEntity, Level level) {
        this(entityType, livingEntity.x, livingEntity.y + (double)livingEntity.getEyeHeight() - (double)0.1f, livingEntity.z, level);
        this.owner = livingEntity;
        this.ownerId = livingEntity.getUUID();
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean shouldRenderAtSqrDistance(double d) {
        double e = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(e)) {
            e = 4.0;
        }
        return d < (e *= 64.0) * e;
    }

    public void shootFromRotation(Entity entity, float f, float g, float h, float i, float j) {
        float k = -Mth.sin(g * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        float l = -Mth.sin((f + h) * ((float)Math.PI / 180));
        float m = Mth.cos(g * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        this.shoot(k, l, m, i, j);
        Vec3 vec3 = entity.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.onGround ? 0.0 : vec3.y, vec3.z));
    }

    @Override
    public void shoot(double d, double e, double f, float g, float h) {
        Vec3 vec3 = new Vec3(d, e, f).normalize().add(this.random.nextGaussian() * (double)0.0075f * (double)h, this.random.nextGaussian() * (double)0.0075f * (double)h, this.random.nextGaussian() * (double)0.0075f * (double)h).scale(g);
        this.setDeltaMovement(vec3);
        float i = Mth.sqrt(ThrowableProjectile.getHorizontalDistanceSqr(vec3));
        this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875);
        this.xRot = (float)(Mth.atan2(vec3.y, i) * 57.2957763671875);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void lerpMotion(double d, double e, double f) {
        this.setDeltaMovement(d, e, f);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            float g = Mth.sqrt(d * d + f * f);
            this.yRot = (float)(Mth.atan2(d, f) * 57.2957763671875);
            this.xRot = (float)(Mth.atan2(e, g) * 57.2957763671875);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }
    }

    @Override
    public void tick() {
        float h;
        this.xOld = this.x;
        this.yOld = this.y;
        this.zOld = this.z;
        super.tick();
        if (this.shakeTime > 0) {
            --this.shakeTime;
        }
        if (this.inGround) {
            this.inGround = false;
            this.setDeltaMovement(this.getDeltaMovement().multiply(this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f));
        }
        AABB aABB = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
        for (Entity entity2 : this.level.getEntities(this, aABB, entity -> !entity.isSpectator() && entity.isPickable())) {
            if (entity2 == this.entityToIgnore) {
                ++this.timeToIgnore;
                break;
            }
            if (this.owner == null || this.tickCount >= 2 || this.entityToIgnore != null) continue;
            this.entityToIgnore = entity2;
            this.timeToIgnore = 3;
            break;
        }
        HitResult hitResult = ProjectileUtil.getHitResult(this, aABB, entity -> !entity.isSpectator() && entity.isPickable() && entity != this.entityToIgnore, ClipContext.Block.OUTLINE, true);
        if (this.entityToIgnore != null && this.timeToIgnore-- <= 0) {
            this.entityToIgnore = null;
        }
        if (hitResult.getType() != HitResult.Type.MISS) {
            if (hitResult.getType() == HitResult.Type.BLOCK && this.level.getBlockState(((BlockHitResult)hitResult).getBlockPos()).getBlock() == Blocks.NETHER_PORTAL) {
                this.handleInsidePortal(((BlockHitResult)hitResult).getBlockPos());
            } else {
                this.onHit(hitResult);
            }
        }
        Vec3 vec3 = this.getDeltaMovement();
        this.x += vec3.x;
        this.y += vec3.y;
        this.z += vec3.z;
        float f = Mth.sqrt(ThrowableProjectile.getHorizontalDistanceSqr(vec3));
        this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875);
        this.xRot = (float)(Mth.atan2(vec3.y, f) * 57.2957763671875);
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
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float g = 0.25f;
                this.level.addParticle(ParticleTypes.BUBBLE, this.x - vec3.x * 0.25, this.y - vec3.y * 0.25, this.z - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
            }
            h = 0.8f;
        } else {
            h = 0.99f;
        }
        this.setDeltaMovement(vec3.scale(h));
        if (!this.isNoGravity()) {
            Vec3 vec32 = this.getDeltaMovement();
            this.setDeltaMovement(vec32.x, vec32.y - (double)this.getGravity(), vec32.z);
        }
        this.setPos(this.x, this.y, this.z);
    }

    protected float getGravity() {
        return 0.03f;
    }

    protected abstract void onHit(HitResult var1);

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("xTile", this.xBlock);
        compoundTag.putInt("yTile", this.yBlock);
        compoundTag.putInt("zTile", this.zBlock);
        compoundTag.putByte("shake", (byte)this.shakeTime);
        compoundTag.putByte("inGround", (byte)(this.inGround ? 1 : 0));
        if (this.ownerId != null) {
            compoundTag.put("owner", NbtUtils.createUUIDTag(this.ownerId));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.xBlock = compoundTag.getInt("xTile");
        this.yBlock = compoundTag.getInt("yTile");
        this.zBlock = compoundTag.getInt("zTile");
        this.shakeTime = compoundTag.getByte("shake") & 0xFF;
        this.inGround = compoundTag.getByte("inGround") == 1;
        this.owner = null;
        if (compoundTag.contains("owner", 10)) {
            this.ownerId = NbtUtils.loadUUIDTag(compoundTag.getCompound("owner"));
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        if ((this.owner == null || this.owner.removed) && this.ownerId != null && this.level instanceof ServerLevel) {
            Entity entity = ((ServerLevel)this.level).getEntity(this.ownerId);
            this.owner = entity instanceof LivingEntity ? (LivingEntity)entity : null;
        }
        return this.owner;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

