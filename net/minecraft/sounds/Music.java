/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.sounds;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;

public class Music {
    public static final Codec<Music> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SoundEvent.CODEC.fieldOf("sound")).forGetter(music -> music.event), ((MapCodec)Codec.INT.fieldOf("min_delay")).forGetter(music -> music.minDelay), ((MapCodec)Codec.INT.fieldOf("max_delay")).forGetter(music -> music.maxDelay), ((MapCodec)Codec.BOOL.fieldOf("replace_current_music")).forGetter(music -> music.replaceCurrentMusic)).apply((Applicative<Music, ?>)instance, Music::new));
    private final SoundEvent event;
    private final int minDelay;
    private final int maxDelay;
    private final boolean replaceCurrentMusic;

    public Music(SoundEvent soundEvent, int i, int j, boolean bl) {
        this.event = soundEvent;
        this.minDelay = i;
        this.maxDelay = j;
        this.replaceCurrentMusic = bl;
    }

    @Environment(value=EnvType.CLIENT)
    public SoundEvent getEvent() {
        return this.event;
    }

    @Environment(value=EnvType.CLIENT)
    public int getMinDelay() {
        return this.minDelay;
    }

    @Environment(value=EnvType.CLIENT)
    public int getMaxDelay() {
        return this.maxDelay;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean replaceCurrentMusic() {
        return this.replaceCurrentMusic;
    }
}

