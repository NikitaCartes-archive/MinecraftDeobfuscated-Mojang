/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AmbientMoodSettings {
    public static final Codec<AmbientMoodSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound")).forGetter(ambientMoodSettings -> ambientMoodSettings.soundEvent), ((MapCodec)Codec.INT.fieldOf("tick_delay")).forGetter(ambientMoodSettings -> ambientMoodSettings.tickDelay), ((MapCodec)Codec.INT.fieldOf("block_search_extent")).forGetter(ambientMoodSettings -> ambientMoodSettings.blockSearchExtent), ((MapCodec)Codec.DOUBLE.fieldOf("offset")).forGetter(ambientMoodSettings -> ambientMoodSettings.soundPositionOffset)).apply((Applicative<AmbientMoodSettings, ?>)instance, AmbientMoodSettings::new));
    public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);
    private final SoundEvent soundEvent;
    private final int tickDelay;
    private final int blockSearchExtent;
    private final double soundPositionOffset;

    public AmbientMoodSettings(SoundEvent soundEvent, int i, int j, double d) {
        this.soundEvent = soundEvent;
        this.tickDelay = i;
        this.blockSearchExtent = j;
        this.soundPositionOffset = d;
    }

    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    public int getTickDelay() {
        return this.tickDelay;
    }

    public int getBlockSearchExtent() {
        return this.blockSearchExtent;
    }

    public double getSoundPositionOffset() {
        return this.soundPositionOffset;
    }
}

