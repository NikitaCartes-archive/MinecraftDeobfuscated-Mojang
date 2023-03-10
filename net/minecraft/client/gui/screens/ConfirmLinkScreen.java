/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(value=EnvType.CLIENT)
public class ConfirmLinkScreen
extends ConfirmScreen {
    private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
    private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
    private final String url;
    private final boolean showWarning;

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, String string, boolean bl) {
        this(booleanConsumer, ConfirmLinkScreen.confirmMessage(bl), Component.literal(string), string, bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, bl);
    }

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, String string, boolean bl) {
        this(booleanConsumer, component, string, bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, bl);
    }

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, String string, Component component2, boolean bl) {
        this(booleanConsumer, component, ConfirmLinkScreen.confirmMessage(bl, string), string, component2, bl);
    }

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, Component component2, String string, Component component3, boolean bl) {
        super(booleanConsumer, component, component2);
        this.yesButton = bl ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES;
        this.noButton = component3;
        this.showWarning = !bl;
        this.url = string;
    }

    protected static MutableComponent confirmMessage(boolean bl, String string) {
        return ConfirmLinkScreen.confirmMessage(bl).append(CommonComponents.SPACE).append(Component.literal(string));
    }

    protected static MutableComponent confirmMessage(boolean bl) {
        return Component.translatable(bl ? "chat.link.confirmTrusted" : "chat.link.confirm");
    }

    @Override
    protected void addButtons(int i) {
        this.addRenderableWidget(Button.builder(this.yesButton, button -> this.callback.accept(true)).bounds(this.width / 2 - 50 - 105, i, 100, 20).build());
        this.addRenderableWidget(Button.builder(COPY_BUTTON_TEXT, button -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }).bounds(this.width / 2 - 50, i, 100, 20).build());
        this.addRenderableWidget(Button.builder(this.noButton, button -> this.callback.accept(false)).bounds(this.width / 2 - 50 + 105, i, 100, 20).build());
    }

    public void copyToClipboard() {
        this.minecraft.keyboardHandler.setClipboard(this.url);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
        if (this.showWarning) {
            ConfirmLinkScreen.drawCenteredString(poseStack, this.font, WARNING_TEXT, this.width / 2, 110, 0xFFCCCC);
        }
    }

    public static void confirmLinkNow(String string, Screen screen, boolean bl2) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(string);
            }
            minecraft.setScreen(screen);
        }, string, bl2));
    }

    public static Button.OnPress confirmLink(String string, Screen screen, boolean bl) {
        return button -> ConfirmLinkScreen.confirmLinkNow(string, screen, bl);
    }
}

