/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

@Environment(value=EnvType.CLIENT)
public class AlertScreen
extends Screen {
    private final Runnable callback;
    protected final Component text;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    protected final Component okButton;

    public AlertScreen(Runnable runnable, Component component, Component component2) {
        this(runnable, component, component2, CommonComponents.GUI_BACK);
    }

    public AlertScreen(Runnable runnable, Component component, Component component2, Component component3) {
        super(component);
        this.callback = runnable;
        this.text = component2;
        this.okButton = component3;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.okButton, button -> this.callback.run()));
        this.message = MultiLineLabel.create(this.font, (FormattedText)this.text, this.width - 50);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        AlertScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 70, 0xFFFFFF);
        this.message.renderCentered(poseStack, this.width / 2, 90);
        super.render(poseStack, i, j, f);
    }
}

