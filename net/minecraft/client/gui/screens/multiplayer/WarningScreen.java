/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class WarningScreen
extends Screen {
    private final Component content;
    @Nullable
    private final Component check;
    private final Component narration;
    @Nullable
    protected Checkbox stopShowing;
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    protected WarningScreen(Component component, Component component2, Component component3) {
        this(component, component2, null, component3);
    }

    protected WarningScreen(Component component, Component component2, @Nullable Component component3, Component component4) {
        super(component);
        this.content = component2;
        this.check = component3;
        this.narration = component4;
    }

    protected abstract void initButtons(int var1);

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, (FormattedText)this.content, this.width - 100);
        int i = (this.message.getLineCount() + 1) * this.getLineHeight();
        if (this.check != null) {
            int j = this.font.width(this.check);
            this.stopShowing = new Checkbox(this.width / 2 - j / 2 - 8, 76 + i, j + 24, 20, this.check, false);
            this.addRenderableWidget(this.stopShowing);
        }
        this.initButtons(i);
    }

    @Override
    public Component getNarrationMessage() {
        return this.narration;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.renderTitle(poseStack);
        int k = this.width / 2 - this.message.getWidth() / 2;
        this.message.renderLeftAligned(poseStack, k, 70, this.getLineHeight(), 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    protected void renderTitle(PoseStack poseStack) {
        WarningScreen.drawString(poseStack, this.font, this.title, 25, 30, 0xFFFFFF);
    }

    protected int getLineHeight() {
        return this.font.lineHeight * 2;
    }
}

