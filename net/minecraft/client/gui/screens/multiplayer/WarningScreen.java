/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class WarningScreen
extends Screen {
    private final Component titleComponent;
    private final Component content;
    private final Component check;
    private final Component narration;
    protected final Screen previous;
    @Nullable
    protected Checkbox stopShowing;
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    protected WarningScreen(Component component, Component component2, Component component3, Component component4, Screen screen) {
        super(NarratorChatListener.NO_TITLE);
        this.titleComponent = component;
        this.content = component2;
        this.check = component3;
        this.narration = component4;
        this.previous = screen;
    }

    protected abstract void initButtons(int var1);

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, (FormattedText)this.content, this.width - 50);
        int i = (this.message.getLineCount() + 1) * this.font.lineHeight * 2;
        this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, this.check, false);
        this.addRenderableWidget(this.stopShowing);
        this.initButtons(i);
    }

    @Override
    public Component getNarrationMessage() {
        return this.narration;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(0);
        WarningScreen.drawString(poseStack, this.font, this.titleComponent, 25, 30, 0xFFFFFF);
        this.message.renderLeftAligned(poseStack, 25, 70, this.font.lineHeight * 2, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }
}

