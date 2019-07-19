/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class DemoIntroScreen
extends Screen {
    private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");

    public DemoIntroScreen() {
        super(new TranslatableComponent("demo.help.title", new Object[0]));
    }

    @Override
    protected void init() {
        int i = -16;
        this.addButton(new Button(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, I18n.get("demo.help.buy", new Object[0]), button -> {
            button.active = false;
            Util.getPlatform().openUri("http://www.minecraft.net/store?source=demo");
        }));
        this.addButton(new Button(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, I18n.get("demo.help.later", new Object[0]), button -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }));
    }

    @Override
    public void renderBackground() {
        super.renderBackground();
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(DEMO_BACKGROUND_LOCATION);
        int i = (this.width - 248) / 2;
        int j = (this.height - 166) / 2;
        this.blit(i, j, 0, 0, 248, 166);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        int k = (this.width - 248) / 2 + 10;
        int l = (this.height - 166) / 2 + 8;
        this.font.draw(this.title.getColoredString(), k, l, 0x1F1F1F);
        Options options = this.minecraft.options;
        this.font.draw(I18n.get("demo.help.movementShort", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()), k, l += 12, 0x4F4F4F);
        this.font.draw(I18n.get("demo.help.movementMouse", new Object[0]), k, l + 12, 0x4F4F4F);
        this.font.draw(I18n.get("demo.help.jump", options.keyJump.getTranslatedKeyMessage()), k, l + 24, 0x4F4F4F);
        this.font.draw(I18n.get("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()), k, l + 36, 0x4F4F4F);
        this.font.drawWordWrap(I18n.get("demo.help.fullWrapped", new Object[0]), k, l + 68, 218, 0x1F1F1F);
        super.render(i, j, f);
    }
}

