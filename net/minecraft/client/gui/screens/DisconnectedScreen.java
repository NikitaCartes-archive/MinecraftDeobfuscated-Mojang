/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DisconnectedScreen
extends Screen {
    private final Component reason;
    @Nullable
    private List<Component> lines;
    private final Screen parent;
    private int textHeight;

    public DisconnectedScreen(Screen screen, String string, Component component) {
        super(new TranslatableComponent(string));
        this.parent = screen;
        this.reason = component;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.lines = this.font.split(this.reason, this.width - 50);
        this.textHeight = this.lines.size() * this.font.lineHeight;
        this.addButton(new Button(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.font.lineHeight, this.height - 30), 200, 20, new TranslatableComponent("gui.toMenu"), button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - this.font.lineHeight * 2, 0xAAAAAA);
        int k = this.height / 2 - this.textHeight / 2;
        if (this.lines != null) {
            for (Component component : this.lines) {
                this.drawCenteredString(poseStack, this.font, component, this.width / 2, k, 0xFFFFFF);
                k += this.font.lineHeight;
            }
        }
        super.render(poseStack, i, j, f);
    }
}

