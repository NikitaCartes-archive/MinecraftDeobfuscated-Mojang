/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class AccessibilityOptionsScreen
extends SimpleOptionsSubScreen {
    private static final String GUIDE_LINK = "https://aka.ms/MinecraftJavaAccessibility";

    private static OptionInstance<?>[] options(Options options) {
        return new OptionInstance[]{options.narrator(), options.showSubtitles(), options.textBackgroundOpacity(), options.backgroundForChatOnly(), options.chatOpacity(), options.chatLineSpacing(), options.chatDelay(), options.notificationDisplayTime(), options.toggleCrouch(), options.toggleSprint(), options.screenEffectScale(), options.fovEffectScale(), options.darknessEffectScale(), options.damageTiltStrength(), options.glintSpeed(), options.glintStrength(), options.hideLightningFlash(), options.autoJump(), options.panoramaSpeed(), options.darkMojangStudiosBackground()};
    }

    public AccessibilityOptionsScreen(Screen screen, Options options) {
        super(screen, options, Component.translatable("options.accessibility.title"), AccessibilityOptionsScreen.options(options));
    }

    @Override
    protected void createFooter() {
        this.addRenderableWidget(Button.builder(Component.translatable("options.accessibility.link"), button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(GUIDE_LINK);
            }
            this.minecraft.setScreen(this);
        }, GUIDE_LINK, true))).bounds(this.width / 2 - 155, this.height - 27, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 5, this.height - 27, 150, 20).build());
    }
}

