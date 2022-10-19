/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(value=EnvType.CLIENT)
public class OptionsSubScreen
extends Screen {
    protected final Screen lastScreen;
    protected final Options options;

    public OptionsSubScreen(Screen screen, Options options, Component component) {
        super(component);
        this.lastScreen = screen;
        this.options = options;
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    public static List<FormattedCharSequence> tooltipAt(OptionsList optionsList, int i, int j) {
        Optional<AbstractWidget> optional = optionsList.getMouseOver(i, j);
        if (optional.isPresent() && optional.get() instanceof TooltipAccessor) {
            return ((TooltipAccessor)((Object)optional.get())).getTooltip();
        }
        return ImmutableList.of();
    }

    protected void basicListRender(PoseStack poseStack, OptionsList optionsList, int i, int j, float f) {
        this.renderBackground(poseStack);
        optionsList.render(poseStack, i, j, f);
        OptionsSubScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, i, j, f);
        List<FormattedCharSequence> list = OptionsSubScreen.tooltipAt(optionsList, i, j);
        this.renderTooltip(poseStack, list, i, j);
    }
}

