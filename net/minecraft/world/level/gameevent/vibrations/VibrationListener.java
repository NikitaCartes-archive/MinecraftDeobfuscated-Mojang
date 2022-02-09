/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent.vibrations;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class VibrationListener
implements GameEventListener {
    protected final PositionSource listenerSource;
    protected final int listenerRange;
    protected final VibrationListenerConfig config;
    protected Optional<GameEvent> receivingEvent = Optional.empty();
    protected int receivingDistance;
    protected int travelTimeInTicks = 0;

    public VibrationListener(PositionSource positionSource, int i, VibrationListenerConfig vibrationListenerConfig) {
        this.listenerSource = positionSource;
        this.listenerRange = i;
        this.config = vibrationListenerConfig;
    }

    public void tick(Level level) {
        if (this.receivingEvent.isPresent()) {
            --this.travelTimeInTicks;
            if (this.travelTimeInTicks <= 0) {
                this.travelTimeInTicks = 0;
                this.config.onSignalReceive(level, this, this.receivingEvent.get(), this.receivingDistance);
                this.receivingEvent = Optional.empty();
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
    public boolean handleGameEvent(Level level, GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos) {
        if (!this.isValidVibration(gameEvent, entity)) {
            return false;
        }
        Optional<BlockPos> optional = this.listenerSource.getPosition(level);
        if (!optional.isPresent()) {
            return false;
        }
        BlockPos blockPos2 = optional.get();
        if (!this.config.shouldListen(level, this, blockPos, gameEvent, entity)) {
            return false;
        }
        if (this.isOccluded(level, blockPos, blockPos2)) {
            return false;
        }
        this.sendSignal(level, gameEvent, blockPos, blockPos2);
        return true;
    }

    private boolean isValidVibration(GameEvent gameEvent, @Nullable Entity entity) {
        if (this.receivingEvent.isPresent()) {
            return false;
        }
        if (!gameEvent.is(GameEventTags.VIBRATIONS)) {
            return false;
        }
        if (entity != null) {
            if (gameEvent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING) && entity.isSteppingCarefully()) {
                return false;
            }
            if (entity.occludesVibrations()) {
                return false;
            }
        }
        return entity == null || !entity.isSpectator();
    }

    private void sendSignal(Level level, GameEvent gameEvent, BlockPos blockPos, BlockPos blockPos2) {
        this.receivingEvent = Optional.of(gameEvent);
        if (level instanceof ServerLevel) {
            this.travelTimeInTicks = this.receivingDistance = Mth.floor(Math.sqrt(blockPos.distSqr(blockPos2)));
            ((ServerLevel)level).sendVibrationParticle(new VibrationPath(blockPos, this.listenerSource, this.travelTimeInTicks));
        }
    }

    private boolean isOccluded(Level level, BlockPos blockPos, BlockPos blockPos2) {
        return level.isBlockInLine(new ClipBlockStateContext(Vec3.atCenterOf(blockPos), Vec3.atCenterOf(blockPos2), blockState -> blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))).getType() == HitResult.Type.BLOCK;
    }

    public static interface VibrationListenerConfig {
        public boolean shouldListen(Level var1, GameEventListener var2, BlockPos var3, GameEvent var4, @Nullable Entity var5);

        public void onSignalReceive(Level var1, GameEventListener var2, GameEvent var3, int var4);
    }
}

