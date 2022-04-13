/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class VibrationListener
implements GameEventListener {
    protected final PositionSource listenerSource;
    protected final int listenerRange;
    protected final VibrationListenerConfig config;
    @Nullable
    protected ReceivingEvent receivingEvent;
    protected int receivingDistance;
    protected int travelTimeInTicks;

    public static Codec<VibrationListener> codec(VibrationListenerConfig vibrationListenerConfig) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)PositionSource.CODEC.fieldOf("source")).forGetter(vibrationListener -> vibrationListener.listenerSource), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range")).forGetter(vibrationListener -> vibrationListener.listenerRange), ReceivingEvent.CODEC.optionalFieldOf("event").forGetter(vibrationListener -> Optional.ofNullable(vibrationListener.receivingEvent)), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_distance")).orElse(0).forGetter(vibrationListener -> vibrationListener.receivingDistance), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay")).orElse(0).forGetter(vibrationListener -> vibrationListener.travelTimeInTicks)).apply((Applicative<VibrationListener, ?>)instance, (positionSource, integer, optional, integer2, integer3) -> new VibrationListener((PositionSource)positionSource, (int)integer, vibrationListenerConfig, optional.orElse(null), (int)integer2, (int)integer3)));
    }

    public VibrationListener(PositionSource positionSource, int i, VibrationListenerConfig vibrationListenerConfig, @Nullable ReceivingEvent receivingEvent, int j, int k) {
        this.listenerSource = positionSource;
        this.listenerRange = i;
        this.config = vibrationListenerConfig;
        this.receivingEvent = receivingEvent;
        this.receivingDistance = j;
        this.travelTimeInTicks = k;
    }

    public void tick(Level level) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.receivingEvent != null) {
                --this.travelTimeInTicks;
                if (this.travelTimeInTicks <= 0) {
                    this.travelTimeInTicks = 0;
                    this.config.onSignalReceive(serverLevel, this, new BlockPos(this.receivingEvent.pos), this.receivingEvent.gameEvent, this.receivingEvent.getEntity(serverLevel).orElse(null), this.receivingEvent.getProjectileOwner(serverLevel).orElse(null), this.receivingDistance);
                    this.receivingEvent = null;
                }
            }
        }
    }

    @Override
    public PositionSource getListenerSource() {
        return this.listenerSource;
    }

    @Override
    public int getListenerRadius() {
        return this.listenerRange;
    }

    @Override
    public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
        if (this.receivingEvent != null) {
            return false;
        }
        if (!this.config.isValidVibration(gameEvent, context)) {
            return false;
        }
        Optional<Vec3> optional = this.listenerSource.getPosition(serverLevel);
        if (optional.isEmpty()) {
            return false;
        }
        Vec3 vec32 = optional.get();
        if (!this.config.shouldListen(serverLevel, this, new BlockPos(vec3), gameEvent, context)) {
            return false;
        }
        if (VibrationListener.isOccluded(serverLevel, vec3, vec32)) {
            return false;
        }
        this.scheduleSignal(serverLevel, gameEvent, context, vec3, vec32);
        return true;
    }

    private void scheduleSignal(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3, Vec3 vec32) {
        this.receivingDistance = Mth.floor(vec3.distanceTo(vec32));
        this.receivingEvent = new ReceivingEvent(gameEvent, this.receivingDistance, vec3, context.sourceEntity());
        this.travelTimeInTicks = this.receivingDistance;
        serverLevel.sendParticles(new VibrationParticleOption(this.listenerSource, this.travelTimeInTicks), vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
        this.config.onSignalSchedule();
    }

    private static boolean isOccluded(Level level, Vec3 vec3, Vec3 vec32) {
        Vec3 vec33 = new Vec3((double)Mth.floor(vec3.x) + 0.5, (double)Mth.floor(vec3.y) + 0.5, (double)Mth.floor(vec3.z) + 0.5);
        Vec3 vec34 = new Vec3((double)Mth.floor(vec32.x) + 0.5, (double)Mth.floor(vec32.y) + 0.5, (double)Mth.floor(vec32.z) + 0.5);
        for (Direction direction : Direction.values()) {
            Vec3 vec35 = vec33.relative(direction, 1.0E-5f);
            if (level.isBlockInLine(new ClipBlockStateContext(vec35, vec34, blockState -> blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))).getType() == HitResult.Type.BLOCK) continue;
            return false;
        }
        return true;
    }

    public static interface VibrationListenerConfig {
        default public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.VIBRATIONS;
        }

        default public boolean isValidVibration(GameEvent gameEvent, GameEvent.Context context) {
            if (!gameEvent.is(this.getListenableEvents())) {
                return false;
            }
            Entity entity = context.sourceEntity();
            if (entity != null) {
                if (entity.isSpectator()) {
                    return false;
                }
                if (entity.isSteppingCarefully() && gameEvent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                    if (entity instanceof ServerPlayer) {
                        ServerPlayer serverPlayer = (ServerPlayer)entity;
                        CriteriaTriggers.AVOID_VIBRATION.trigger(serverPlayer);
                    }
                    return false;
                }
                if (entity.dampensVibrations()) {
                    return false;
                }
                if (gameEvent.is(GameEventTags.DAMPENABLE_VIBRATIONS)) {
                    return !entity.getBlockStateOn().is(BlockTags.DAMPENS_VIBRATIONS);
                }
            }
            if (context.affectedState() != null) {
                return !context.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
            }
            return true;
        }

        public boolean shouldListen(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, GameEvent.Context var5);

        public void onSignalReceive(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, @Nullable Entity var5, @Nullable Entity var6, int var7);

        default public void onSignalSchedule() {
        }
    }

    public record ReceivingEvent(GameEvent gameEvent, int distance, Vec3 pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity) {
        public static final Codec<ReceivingEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.GAME_EVENT.byNameCodec().fieldOf("game_event")).forGetter(ReceivingEvent::gameEvent), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("distance")).forGetter(ReceivingEvent::distance), ((MapCodec)Vec3.CODEC.fieldOf("pos")).forGetter(ReceivingEvent::pos), ExtraCodecs.UUID.optionalFieldOf("source").forGetter(receivingEvent -> Optional.ofNullable(receivingEvent.uuid())), ExtraCodecs.UUID.optionalFieldOf("projectile_owner").forGetter(receivingEvent -> Optional.ofNullable(receivingEvent.projectileOwnerUuid()))).apply((Applicative<ReceivingEvent, ?>)instance, (gameEvent, integer, vec3, optional, optional2) -> new ReceivingEvent((GameEvent)gameEvent, (int)integer, (Vec3)vec3, optional.orElse(null), optional2.orElse(null))));

        public ReceivingEvent(GameEvent gameEvent, int i, Vec3 vec3, @Nullable UUID uUID, @Nullable UUID uUID2) {
            this(gameEvent, i, vec3, uUID, uUID2, null);
        }

        public ReceivingEvent(GameEvent gameEvent, int i, Vec3 vec3, @Nullable Entity entity) {
            this(gameEvent, i, vec3, entity == null ? null : entity.getUUID(), ReceivingEvent.getProjectileOwner(entity), entity);
        }

        @Nullable
        private static UUID getProjectileOwner(@Nullable Entity entity) {
            Projectile projectile;
            if (entity instanceof Projectile && (projectile = (Projectile)entity).getOwner() != null) {
                return projectile.getOwner().getUUID();
            }
            return null;
        }

        public Optional<Entity> getEntity(ServerLevel serverLevel) {
            return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(serverLevel::getEntity));
        }

        public Optional<Entity> getProjectileOwner(ServerLevel serverLevel) {
            return this.getEntity(serverLevel).filter(entity -> entity instanceof Projectile).map(entity -> (Projectile)entity).map(Projectile::getOwner).or(() -> Optional.ofNullable(this.projectileOwnerUuid).map(serverLevel::getEntity));
        }

        @Nullable
        public UUID uuid() {
            return this.uuid;
        }

        @Nullable
        public UUID projectileOwnerUuid() {
            return this.projectileOwnerUuid;
        }

        @Nullable
        public Entity entity() {
            return this.entity;
        }
    }
}

