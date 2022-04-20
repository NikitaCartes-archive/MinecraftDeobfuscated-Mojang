/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WeighedSoundEvents
implements Weighted<Sound> {
    private final List<Weighted<Sound>> list = Lists.newArrayList();
    private final RandomSource random = RandomSource.create();
    private final ResourceLocation location;
    @Nullable
    private final Component subtitle;

    public WeighedSoundEvents(ResourceLocation resourceLocation, @Nullable String string) {
        this.location = resourceLocation;
        this.subtitle = string == null ? null : Component.translatable(string);
    }

    @Override
    public int getWeight() {
        int i = 0;
        for (Weighted<Sound> weighted : this.list) {
            i += weighted.getWeight();
        }
        return i;
    }

    @Override
    public Sound getSound(RandomSource randomSource) {
        int i = this.getWeight();
        if (this.list.isEmpty() || i == 0) {
            return SoundManager.EMPTY_SOUND;
        }
        int j = randomSource.nextInt(i);
        for (Weighted<Sound> weighted : this.list) {
            if ((j -= weighted.getWeight()) >= 0) continue;
            return weighted.getSound(randomSource);
        }
        return SoundManager.EMPTY_SOUND;
    }

    public void addSound(Weighted<Sound> weighted) {
        this.list.add(weighted);
    }

    public ResourceLocation getResourceLocation() {
        return this.location;
    }

    @Nullable
    public Component getSubtitle() {
        return this.subtitle;
    }

    @Override
    public void preloadIfRequired(SoundEngine soundEngine) {
        for (Weighted<Sound> weighted : this.list) {
            weighted.preloadIfRequired(soundEngine);
        }
    }

    @Override
    public /* synthetic */ Object getSound(RandomSource randomSource) {
        return this.getSound(randomSource);
    }
}

