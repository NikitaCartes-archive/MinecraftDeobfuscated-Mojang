/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Deque;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatComponent
extends GuiComponent {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MAX_CHAT_HISTORY = 100;
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage<Component>> allMessages = Lists.newArrayList();
    private final List<GuiMessage<FormattedCharSequence>> trimmedMessages = Lists.newArrayList();
    private final Deque<Component> chatQueue = Queues.newArrayDeque();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private long lastMessage;

    public ChatComponent(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(PoseStack poseStack, int i) {
        int r;
        int q;
        int o;
        int n;
        if (this.isChatHidden()) {
            return;
        }
        this.processPendingMessages();
        int j = this.getLinesPerPage();
        int k = this.trimmedMessages.size();
        if (k <= 0) {
            return;
        }
        boolean bl = false;
        if (this.isChatFocused()) {
            bl = true;
        }
        float f = (float)this.getScale();
        int l = Mth.ceil((float)this.getWidth() / f);
        poseStack.pushPose();
        poseStack.translate(4.0, 8.0, 0.0);
        poseStack.scale(f, f, 1.0f);
        double d = this.minecraft.options.chatOpacity * (double)0.9f + (double)0.1f;
        double e = this.minecraft.options.textBackgroundOpacity;
        double g = 9.0 * (this.minecraft.options.chatLineSpacing + 1.0);
        double h = -8.0 * (this.minecraft.options.chatLineSpacing + 1.0) + 4.0 * this.minecraft.options.chatLineSpacing;
        int m = 0;
        for (n = 0; n + this.chatScrollbarPos < this.trimmedMessages.size() && n < j; ++n) {
            GuiMessage<FormattedCharSequence> guiMessage = this.trimmedMessages.get(n + this.chatScrollbarPos);
            if (guiMessage == null || (o = i - guiMessage.getAddedTime()) >= 200 && !bl) continue;
            double p = bl ? 1.0 : ChatComponent.getTimeFactor(o);
            q = (int)(255.0 * p * d);
            r = (int)(255.0 * p * e);
            ++m;
            if (q <= 3) continue;
            boolean s = false;
            double t = (double)(-n) * g;
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 50.0);
            ChatComponent.fill(poseStack, -4, (int)(t - g), 0 + l + 4, (int)t, r << 24);
            RenderSystem.enableBlend();
            poseStack.translate(0.0, 0.0, 50.0);
            this.minecraft.font.drawShadow(poseStack, guiMessage.getMessage(), 0.0f, (float)((int)(t + h)), 0xFFFFFF + (q << 24));
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
        if (!this.chatQueue.isEmpty()) {
            n = (int)(128.0 * d);
            int u = (int)(255.0 * e);
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 50.0);
            ChatComponent.fill(poseStack, -2, 0, l + 4, 9, u << 24);
            RenderSystem.enableBlend();
            poseStack.translate(0.0, 0.0, 50.0);
            this.minecraft.font.drawShadow(poseStack, new TranslatableComponent("chat.queue", this.chatQueue.size()), 0.0f, 1.0f, 0xFFFFFF + (n << 24));
            poseStack.popPose();
            RenderSystem.disableBlend();
        }
        if (bl) {
            n = this.minecraft.font.lineHeight;
            int u = k * n;
            o = m * n;
            int v = this.chatScrollbarPos * o / k;
            int w = o * o / u;
            if (u != o) {
                q = v > 0 ? 170 : 96;
                r = this.newMessageSinceScroll ? 0xCC3333 : 0x3333AA;
                poseStack.translate(-4.0, 0.0, 0.0);
                ChatComponent.fill(poseStack, 0, -v, 2, -v - w, r + (q << 24));
                ChatComponent.fill(poseStack, 2, -v, 1, -v - w, 0xCCCCCC + (q << 24));
            }
        }
        poseStack.popPose();
    }

    private boolean isChatHidden() {
        return this.minecraft.options.chatVisibility == ChatVisiblity.HIDDEN;
    }

    private static double getTimeFactor(int i) {
        double d = (double)i / 200.0;
        d = 1.0 - d;
        d *= 10.0;
        d = Mth.clamp(d, 0.0, 1.0);
        d *= d;
        return d;
    }

    public void clearMessages(boolean bl) {
        this.chatQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (bl) {
            this.recentChat.clear();
        }
    }

    public void addMessage(Component component) {
        this.addMessage(component, 0);
    }

    private void addMessage(Component component, int i) {
        this.addMessage(component, i, this.minecraft.gui.getGuiTicks(), false);
        LOGGER.info("[CHAT] {}", (Object)component.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void addMessage(Component component, int i, int j, boolean bl) {
        if (i != 0) {
            this.removeById(i);
        }
        int k = Mth.floor((double)this.getWidth() / this.getScale());
        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(component, k, this.minecraft.font);
        boolean bl2 = this.isChatFocused();
        for (FormattedCharSequence formattedCharSequence : list) {
            if (bl2 && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1.0);
            }
            this.trimmedMessages.add(0, new GuiMessage<FormattedCharSequence>(j, formattedCharSequence, i));
        }
        while (this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
        if (!bl) {
            this.allMessages.add(0, new GuiMessage<Component>(j, component, i));
            while (this.allMessages.size() > 100) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }
    }

    public void rescaleChat() {
        this.trimmedMessages.clear();
        this.resetChatScroll();
        for (int i = this.allMessages.size() - 1; i >= 0; --i) {
            GuiMessage<Component> guiMessage = this.allMessages.get(i);
            this.addMessage(guiMessage.getMessage(), guiMessage.getId(), guiMessage.getAddedTime(), true);
        }
    }

    public List<String> getRecentChat() {
        return this.recentChat;
    }

    public void addRecentChat(String string) {
        if (this.recentChat.isEmpty() || !this.recentChat.get(this.recentChat.size() - 1).equals(string)) {
            this.recentChat.add(string);
        }
    }

    public void resetChatScroll() {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(double d) {
        this.chatScrollbarPos = (int)((double)this.chatScrollbarPos + d);
        int i = this.trimmedMessages.size();
        if (this.chatScrollbarPos > i - this.getLinesPerPage()) {
            this.chatScrollbarPos = i - this.getLinesPerPage();
        }
        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    public boolean handleChatQueueClicked(double d, double e) {
        if (!this.isChatFocused() || this.minecraft.options.hideGui || this.isChatHidden() || this.chatQueue.isEmpty()) {
            return false;
        }
        double f = d - 2.0;
        double g = (double)this.minecraft.getWindow().getGuiScaledHeight() - e - 40.0;
        if (f <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && g < 0.0 && g > (double)Mth.floor(-9.0 * this.getScale())) {
            this.addMessage(this.chatQueue.remove());
            this.lastMessage = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Nullable
    public Style getClickedComponentStyleAt(double d, double e) {
        int j;
        if (!this.isChatFocused() || this.minecraft.options.hideGui || this.isChatHidden()) {
            return null;
        }
        double f = d - 2.0;
        double g = (double)this.minecraft.getWindow().getGuiScaledHeight() - e - 40.0;
        f = Mth.floor(f / this.getScale());
        g = Mth.floor(g / (this.getScale() * (this.minecraft.options.chatLineSpacing + 1.0)));
        if (f < 0.0 || g < 0.0) {
            return null;
        }
        int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
        if (f <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && g < (double)(this.minecraft.font.lineHeight * i + i) && (j = (int)(g / (double)this.minecraft.font.lineHeight + (double)this.chatScrollbarPos)) >= 0 && j < this.trimmedMessages.size()) {
            GuiMessage<FormattedCharSequence> guiMessage = this.trimmedMessages.get(j);
            return this.minecraft.font.getSplitter().componentStyleAtWidth(guiMessage.getMessage(), (int)f);
        }
        return null;
    }

    private boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    private void removeById(int i) {
        this.trimmedMessages.removeIf(guiMessage -> guiMessage.getId() == i);
        this.allMessages.removeIf(guiMessage -> guiMessage.getId() == i);
    }

    public int getWidth() {
        return ChatComponent.getWidth(this.minecraft.options.chatWidth);
    }

    public int getHeight() {
        return ChatComponent.getHeight((this.isChatFocused() ? this.minecraft.options.chatHeightFocused : this.minecraft.options.chatHeightUnfocused) / (this.minecraft.options.chatLineSpacing + 1.0));
    }

    public double getScale() {
        return this.minecraft.options.chatScale;
    }

    public static int getWidth(double d) {
        int i = 320;
        int j = 40;
        return Mth.floor(d * 280.0 + 40.0);
    }

    public static int getHeight(double d) {
        int i = 180;
        int j = 20;
        return Mth.floor(d * 160.0 + 20.0);
    }

    public int getLinesPerPage() {
        return this.getHeight() / 9;
    }

    private long getChatRateMillis() {
        return (long)(this.minecraft.options.chatDelay * 1000.0);
    }

    private void processPendingMessages() {
        if (this.chatQueue.isEmpty()) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastMessage >= this.getChatRateMillis()) {
            this.addMessage(this.chatQueue.remove());
            this.lastMessage = l;
        }
    }

    public void enqueueMessage(Component component) {
        if (this.minecraft.options.chatDelay <= 0.0) {
            this.addMessage(component);
        } else {
            long l = System.currentTimeMillis();
            if (l - this.lastMessage >= this.getChatRateMillis()) {
                this.addMessage(component);
                this.lastMessage = l;
            } else {
                this.chatQueue.add(component);
            }
        }
    }
}

