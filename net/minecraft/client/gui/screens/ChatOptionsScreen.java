/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public class ChatOptionsScreen
extends OptionsSubScreen {
    private static final Option[] CHAT_OPTIONS = new Option[]{Option.CHAT_VISIBILITY, Option.CHAT_COLOR, Option.CHAT_LINKS, Option.CHAT_LINKS_PROMPT, Option.CHAT_OPACITY, Option.TEXT_BACKGROUND_OPACITY, Option.CHAT_SCALE, Option.CHAT_LINE_SPACING, Option.CHAT_WIDTH, Option.CHAT_HEIGHT_FOCUSED, Option.CHAT_HEIGHT_UNFOCUSED, Option.NARRATOR, Option.AUTO_SUGGESTIONS, Option.REDUCED_DEBUG_INFO};
    private AbstractWidget narratorButton;

    public ChatOptionsScreen(Screen screen, Options options) {
        super(screen, options, new TranslatableComponent("options.chat.title"));
    }

    @Override
    protected void init() {
        int i = 0;
        for (Option option : CHAT_OPTIONS) {
            int j = this.width / 2 - 155 + i % 2 * 160;
            int k = this.height / 6 + 24 * (i >> 1);
            AbstractWidget abstractWidget = this.addButton(option.createButton(this.minecraft.options, j, k, 150));
            if (option == Option.NARRATOR) {
                this.narratorButton = abstractWidget;
                abstractWidget.active = NarratorChatListener.INSTANCE.isActive();
            }
            ++i;
        }
        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 24 * (i + 1) / 2, 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    public void updateNarratorButton() {
        this.narratorButton.setMessage(Option.NARRATOR.getMessage(this.options));
    }
}

