/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.sounds.SoundSource;

@Environment(value=EnvType.CLIENT)
public class VolumeSlider
extends AbstractSliderButton {
    private final SoundSource source;

    public VolumeSlider(Minecraft minecraft, int i, int j, SoundSource soundSource, int k) {
        super(minecraft.options, i, j, k, 20, minecraft.options.getSoundSourceVolume(soundSource));
        this.source = soundSource;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        String string = (float)this.value == (float)this.getYImage(false) ? I18n.get("options.off", new Object[0]) : (int)((float)this.value * 100.0f) + "%";
        this.setMessage(I18n.get("soundCategory." + this.source.getName(), new Object[0]) + ": " + string);
    }

    @Override
    protected void applyValue() {
        this.options.setSoundCategoryVolume(this.source, (float)this.value);
        this.options.save();
    }
}

