/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Interaction
extends Entity
implements Attackable,
Targeting {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_RESPONSE_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.BOOLEAN);
    private static final String TAG_WIDTH = "width";
    private static final String TAG_HEIGHT = "height";
    private static final String TAG_ATTACK = "attack";
    private static final String TAG_INTERACTION = "interaction";
    private static final String TAG_RESPONSE = "response";
    @Nullable
    private PlayerAction attack;
    @Nullable
    private PlayerAction interaction;

    public Interaction(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_WIDTH_ID, Float.valueOf(1.0f));
        this.entityData.define(DATA_HEIGHT_ID, Float.valueOf(1.0f));
        this.entityData.define(DATA_RESPONSE_ID, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains(TAG_WIDTH, 99)) {
            this.setWidth(compoundTag.getFloat(TAG_WIDTH));
        }
        if (compoundTag.contains(TAG_HEIGHT, 99)) {
            this.setHeight(compoundTag.getFloat(TAG_HEIGHT));
        }
        if (compoundTag.contains(TAG_ATTACK)) {
            PlayerAction.CODEC.decode(NbtOps.INSTANCE, compoundTag.get(TAG_ATTACK)).resultOrPartial(Util.prefix("Interaction entity", LOGGER::error)).ifPresent(pair -> {
                this.attack = (PlayerAction)pair.getFirst();
            });
        } else {
            this.attack = null;
        }
        if (compoundTag.contains(TAG_INTERACTION)) {
            PlayerAction.CODEC.decode(NbtOps.INSTANCE, compoundTag.get(TAG_INTERACTION)).resultOrPartial(Util.prefix("Interaction entity", LOGGER::error)).ifPresent(pair -> {
                this.interaction = (PlayerAction)pair.getFirst();
            });
        } else {
            this.interaction = null;
        }
        this.setResponse(compoundTag.getBoolean(TAG_RESPONSE));
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putFloat(TAG_WIDTH, this.getWidth());
        compoundTag.putFloat(TAG_HEIGHT, this.getHeight());
        if (this.attack != null) {
            PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.attack).result().ifPresent(tag -> compoundTag.put(TAG_ATTACK, (Tag)tag));
        }
        if (this.interaction != null) {
            PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.interaction).result().ifPresent(tag -> compoundTag.put(TAG_INTERACTION, (Tag)tag));
        }
        compoundTag.putBoolean(TAG_RESPONSE, this.getResponse());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_HEIGHT_ID.equals(entityDataAccessor) || DATA_WIDTH_ID.equals(entityDataAccessor)) {
            this.setBoundingBox(this.makeBoundingBox());
        }
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            this.attack = new PlayerAction(player.getUUID(), this.level.getGameTime());
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(serverPlayer, this, player.damageSources().generic(), 1.0f, 1.0f, false);
            }
            return !this.getResponse();
        }
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (this.level.isClientSide) {
            return this.getResponse() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        this.interaction = new PlayerAction(player.getUUID(), this.level.getGameTime());
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick() {
    }

    @Override
    @Nullable
    public LivingEntity getLastAttacker() {
        if (this.attack != null) {
            return this.level.getPlayerByUUID(this.attack.player());
        }
        return null;
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        if (this.interaction != null) {
            return this.level.getPlayerByUUID(this.interaction.player());
        }
        return null;
    }

    private void setWidth(float f) {
        this.entityData.set(DATA_WIDTH_ID, Float.valueOf(f));
    }

    private float getWidth() {
        return this.entityData.get(DATA_WIDTH_ID).floatValue();
    }

    private void setHeight(float f) {
        this.entityData.set(DATA_HEIGHT_ID, Float.valueOf(f));
    }

    private float getHeight() {
        return this.entityData.get(DATA_HEIGHT_ID).floatValue();
    }

    private void setResponse(boolean bl) {
        this.entityData.set(DATA_RESPONSE_ID, bl);
    }

    private boolean getResponse() {
        return this.entityData.get(DATA_RESPONSE_ID);
    }

    private EntityDimensions getDimensions() {
        return EntityDimensions.scalable(this.getWidth(), this.getHeight());
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.getDimensions();
    }

    @Override
    protected AABB makeBoundingBox() {
        return this.getDimensions().makeBoundingBox(this.position());
    }

    record PlayerAction(UUID player, long timestamp) {
        public static final Codec<PlayerAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)UUIDUtil.CODEC.fieldOf("player")).forGetter(PlayerAction::player), ((MapCodec)Codec.LONG.fieldOf("timestamp")).forGetter(PlayerAction::timestamp)).apply((Applicative<PlayerAction, ?>)instance, PlayerAction::new));
    }
}

