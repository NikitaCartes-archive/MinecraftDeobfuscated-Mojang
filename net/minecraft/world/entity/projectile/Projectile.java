/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class Projectile
extends Entity {
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    private boolean leftOwner;
    private boolean hasBeenShot;

    Projectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null) {
            return this.cachedOwner;
        }
        if (this.ownerUUID != null && this.level instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel)this.level).getEntity(this.ownerUUID);
            return this.cachedOwner;
        }
        return null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
        if (this.leftOwner) {
            compoundTag.putBoolean("LeftOwner", true);
        }
        compoundTag.putBoolean("HasBeenShot", this.hasBeenShot);
    }

    protected boolean ownedBy(Entity entity) {
        return entity.getUUID().equals(this.ownerUUID);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
        this.leftOwner = compoundTag.getBoolean("LeftOwner");
        this.hasBeenShot = compoundTag.getBoolean("HasBeenShot");
    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner(), this.blockPosition());
            this.hasBeenShot = true;
        }
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }
        super.tick();
    }

    private boolean checkLeftOwner() {
        Entity entity2 = this.getOwner();
        if (entity2 != null) {
            for (Entity entity22 : this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), entity -> !entity.isSpectator() && entity.isPickable())) {
                if (entity22.getRootVehicle() != entity2.getRootVehicle()) continue;
                return false;
            }
        }
        return true;
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
        if (type != HitResult.Type.MISS) {
            this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockState blockState = this.level.getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.level, blockState, blockHitResult, this);
    }

    @Override
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

    protected boolean canHitEntity(Entity entity) {
        if (entity.isSpectator() || !entity.isAlive() || !entity.isPickable()) {
            return false;
        }
        Entity entity2 = this.getOwner();
        return entity2 == null || this.leftOwner || !entity2.isPassengerOfSameVehicle(entity);
    }

    protected void updateRotation() {
        Vec3 vec3 = this.getDeltaMovement();
        float f = Mth.sqrt(Projectile.getHorizontalDistanceSqr(vec3));
        this.xRot = Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, f) * 57.2957763671875));
        this.yRot = Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
    }

    protected static float lerpRotation(float f, float g) {
        while (g - f < -180.0f) {
            f -= 360.0f;
        }
        while (g - f >= 180.0f) {
            f += 360.0f;
        }
        return Mth.lerp(0.2f, f, g);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        Entity entity = this.level.getEntity(clientboundAddEntityPacket.getData());
        if (entity != null) {
            this.setOwner(entity);
        }
    }
}

