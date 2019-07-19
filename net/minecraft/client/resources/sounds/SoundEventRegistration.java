/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.Sound;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SoundEventRegistration {
    private final List<Sound> sounds;
    private final boolean replace;
    private final String subtitle;

    public SoundEventRegistration(List<Sound> list, boolean bl, String string) {
        this.sounds = list;
        this.replace = bl;
        this.subtitle = string;
    }

    public List<Sound> getSounds() {
        return this.sounds;
    }

    public boolean isReplace() {
        return this.replace;
    }

    @Nullable
    public String getSubtitle() {
        return this.subtitle;
    }
}

