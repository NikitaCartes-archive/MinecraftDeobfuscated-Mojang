/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatScreen
extends Screen {
    public static final double MOUSE_SCROLL_SPEED = 7.0;
    private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
    private static final int TOOLTIP_MAX_WIDTH = 210;
    private String historyBuffer = "";
    private int historyPos = -1;
    protected EditBox input;
    private String initial;
    CommandSuggestions commandSuggestions;

    public ChatScreen(String string) {
        super(Component.translatable("chat_screen.title"));
        this.initial = string;
    }

    @Override
    protected void init() {
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new EditBox(this.minecraft.fontFilterFishy, 4, this.height - 12, this.width - 4, 12, (Component)Component.translatable("chat.editBox")){

            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setValue(this.initial);
        this.input.setResponder(this::onEdited);
        this.input.setCanLoseFocus(false);
        this.addWidget(this.input);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestions.updateCommandInfo();
        this.setInitialFocus(this.input);
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.input.getValue();
        this.init(minecraft, i, j);
        this.setChatLine(string);
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public void removed() {
        this.minecraft.gui.getChat().resetChatScroll();
    }

    @Override
    public void tick() {
        this.input.tick();
    }

    private void onEdited(String string) {
        String string2 = this.input.getValue();
        this.commandSuggestions.setAllowSuggestions(!string2.equals(this.initial));
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.commandSuggestions.keyPressed(i, j, k)) {
            return true;
        }
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (i == 256) {
            this.minecraft.setScreen(null);
            return true;
        }
        if (i == 257 || i == 335) {
            if (this.handleChatInput(this.input.getValue(), true)) {
                this.minecraft.setScreen(null);
            }
            return true;
        }
        if (i == 265) {
            this.moveInHistory(-1);
            return true;
        }
        if (i == 264) {
            this.moveInHistory(1);
            return true;
        }
        if (i == 266) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
        }
        if (i == 267) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (this.commandSuggestions.mouseScrolled(f = Mth.clamp(f, -1.0, 1.0))) {
            return true;
        }
        if (!ChatScreen.hasShiftDown()) {
            f *= 7.0;
        }
        this.minecraft.gui.getChat().scrollChat((int)f);
        return true;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.commandSuggestions.mouseClicked((int)d, (int)e, i)) {
            return true;
        }
        if (i == 0) {
            ChatComponent chatComponent = this.minecraft.gui.getChat();
            if (chatComponent.handleChatQueueClicked(d, e)) {
                return true;
            }
            Style style = this.getComponentStyleAt(d, e);
            if (style != null && this.handleComponentClicked(style)) {
                this.initial = this.input.getValue();
                return true;
            }
        }
        if (this.input.mouseClicked(d, e, i)) {
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    protected void insertText(String string, boolean bl) {
        if (bl) {
            this.input.setValue(string);
        } else {
            this.input.insertText(string);
        }
    }

    public void moveInHistory(int i) {
        int j = this.historyPos + i;
        int k = this.minecraft.gui.getChat().getRecentChat().size();
        if ((j = Mth.clamp(j, 0, k)) == this.historyPos) {
            return;
        }
        if (j == k) {
            this.historyPos = k;
            this.input.setValue(this.historyBuffer);
            return;
        }
        if (this.historyPos == k) {
            this.historyBuffer = this.input.getValue();
        }
        this.input.setValue(this.minecraft.gui.getChat().getRecentChat().get(j));
        this.commandSuggestions.setAllowSuggestions(false);
        this.historyPos = j;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        ChatScreen.fill(poseStack, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
        this.commandSuggestions.render(poseStack, i, j);
        GuiMessageTag guiMessageTag = this.minecraft.gui.getChat().getMessageTagAt(i, j);
        if (guiMessageTag != null && guiMessageTag.text() != null) {
            this.renderTooltip(poseStack, this.font.split(guiMessageTag.text(), 210), i, j);
        } else {
            Style style = this.getComponentStyleAt(i, j);
            if (style != null && style.getHoverEvent() != null) {
                this.renderComponentHoverEffect(poseStack, style, i, j);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void setChatLine(String string) {
        this.input.setValue(string);
    }

    @Override
    protected void updateNarrationState(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getTitle());
        narrationElementOutput.add(NarratedElementType.USAGE, USAGE_TEXT);
        String string = this.input.getValue();
        if (!string.isEmpty()) {
            narrationElementOutput.nest().add(NarratedElementType.TITLE, (Component)Component.translatable("chat_screen.message", string));
        }
    }

    @Nullable
    private Style getComponentStyleAt(double d, double e) {
        return this.minecraft.gui.getChat().getClickedComponentStyleAt(d, e);
    }

    public boolean handleChatInput(String string, boolean bl) {
        if ((string = this.normalizeChatMessage(string)).isEmpty()) {
            return true;
        }
        if (bl) {
            this.minecraft.gui.getChat().addRecentChat(string);
        }
        if (string.startsWith("/")) {
            this.minecraft.player.connection.sendCommand(string.substring(1));
        } else {
            this.minecraft.player.connection.sendChat(string);
        }
        return true;
    }

    public String normalizeChatMessage(String string) {
        return StringUtil.trimChatMessage(StringUtils.normalizeSpace(string.trim()));
    }
}

