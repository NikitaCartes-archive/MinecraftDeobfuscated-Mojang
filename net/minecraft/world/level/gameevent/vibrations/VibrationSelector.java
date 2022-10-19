/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import org.apache.commons.lang3.tuple.Pair;

public class VibrationSelector {
    public static final Codec<VibrationSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(VibrationInfo.CODEC.optionalFieldOf("event").forGetter(vibrationSelector -> vibrationSelector.currentVibrationData.map(Pair::getLeft)), ((MapCodec)Codec.LONG.fieldOf("tick")).forGetter(vibrationSelector -> vibrationSelector.currentVibrationData.map(Pair::getRight).orElse(-1L))).apply((Applicative<VibrationSelector, ?>)instance, VibrationSelector::new));
    private Optional<Pair<VibrationInfo, Long>> currentVibrationData;

    public VibrationSelector(Optional<VibrationInfo> optional, long l) {
        this.currentVibrationData = optional.map(vibrationInfo -> Pair.of(vibrationInfo, l));
    }

    public VibrationSelector() {
        this.currentVibrationData = Optional.empty();
    }

    public void addCandidate(VibrationInfo vibrationInfo, long l) {
        if (this.shouldReplaceVibration(vibrationInfo, l)) {
            this.currentVibrationData = Optional.of(Pair.of(vibrationInfo, l));
        }
    }

    private boolean shouldReplaceVibration(VibrationInfo vibrationInfo, long l) {
        if (this.currentVibrationData.isEmpty()) {
            return true;
        }
        Pair<VibrationInfo, Long> pair = this.currentVibrationData.get();
        long m = pair.getRight();
        if (l != m) {
            return false;
        }
        VibrationInfo vibrationInfo2 = pair.getLeft();
        if (vibrationInfo.distance() < vibrationInfo2.distance()) {
            return true;
        }
        if (vibrationInfo.distance() > vibrationInfo2.distance()) {
            return false;
        }
        return VibrationListener.getGameEventFrequency(vibrationInfo.gameEvent()) > VibrationListener.getGameEventFrequency(vibrationInfo2.gameEvent());
    }

    public Optional<VibrationInfo> chosenCandidate(long l) {
        if (this.currentVibrationData.isEmpty()) {
            return Optional.empty();
        }
        if (this.currentVibrationData.get().getRight() < l) {
            return Optional.of(this.currentVibrationData.get().getLeft());
        }
        return Optional.empty();
    }

    public void startOver() {
        this.currentVibrationData = Optional.empty();
    }
}

