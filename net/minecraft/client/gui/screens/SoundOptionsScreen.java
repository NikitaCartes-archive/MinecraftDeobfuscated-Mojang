/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

@Environment(value=EnvType.CLIENT)
public class SoundOptionsScreen
extends OptionsSubScreen {
    private OptionsList list;

    private static OptionInstance<?>[] buttonOptions(Options options) {
        return new OptionInstance[]{options.showSubtitles(), options.directionalAudio()};
    }

    public SoundOptionsScreen(Screen screen, Options options) {
        super(screen, options, Component.translatable("options.sounds.title"));
    }

    @Override
    protected void init() {
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addBig(this.options.getSoundSourceOptionInstance(SoundSource.MASTER));
        this.list.addSmall(this.getAllSoundOptionsExceptMaster());
        this.list.addBig(this.options.soundDevice());
        this.list.addSmall(SoundOptionsScreen.buttonOptions(this.options));
        this.addWidget(this.list);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.minecraft.options.save();
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    private OptionInstance<?>[] getAllSoundOptionsExceptMaster() {
        return (OptionInstance[])Arrays.stream(SoundSource.values()).filter(soundSource -> soundSource != SoundSource.MASTER).map(soundSource -> this.options.getSoundSourceOptionInstance((SoundSource)((Object)soundSource))).toArray(OptionInstance[]::new);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.basicListRender(poseStack, this.list, i, j, f);
    }
}

