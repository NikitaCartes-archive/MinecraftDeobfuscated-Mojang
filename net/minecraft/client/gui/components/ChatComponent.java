/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatComponent
extends GuiComponent {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage> allMessages = Lists.newArrayList();
    private final List<GuiMessage> trimmedMessages = Lists.newArrayList();
    private final ArrayDeque<Component> chatQueue = new ArrayDeque();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private long lastMessage = 0L;

    public ChatComponent(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(int i) {
        int r;
        int q;
        int n;
        if (!this.isChatVisible()) {
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
        double d = this.getScale();
        int l = Mth.ceil((double)this.getWidth() / d);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(2.0f, 8.0f, 0.0f);
        RenderSystem.scaled(d, d, 1.0);
        double e = this.minecraft.options.chatOpacity * (double)0.9f + (double)0.1f;
        double f = this.minecraft.options.textBackgroundOpacity;
        double g = 9.0 * (this.minecraft.options.chatLineSpacing + 1.0);
        double h = -8.0 * (this.minecraft.options.chatLineSpacing + 1.0) + 4.0 * this.minecraft.options.chatLineSpacing;
        int m = 0;
        Matrix4f matrix4f = Matrix4f.createTranslateMatrix(0.0f, 0.0f, -100.0f);
        for (n = 0; n + this.chatScrollbarPos < this.trimmedMessages.size() && n < j; ++n) {
            int o;
            GuiMessage guiMessage = this.trimmedMessages.get(n + this.chatScrollbarPos);
            if (guiMessage == null || (o = i - guiMessage.getAddedTime()) >= 200 && !bl) continue;
            double p = bl ? 1.0 : ChatComponent.getTimeFactor(o);
            q = (int)(255.0 * p * e);
            r = (int)(255.0 * p * f);
            ++m;
            if (q <= 3) continue;
            boolean s = false;
            double t = (double)(-n) * g;
            ChatComponent.fill(matrix4f, -2, (int)(t - g), 0 + l + 4, (int)t, r << 24);
            String string = guiMessage.getMessage().getColoredString();
            RenderSystem.enableBlend();
            this.minecraft.font.drawShadow(string, 0.0f, (int)(t + h), 0xFFFFFF + (q << 24));
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
        }
        if (!this.chatQueue.isEmpty()) {
            n = (int)(128.0 * e);
            int u = (int)(255.0 * f);
            ChatComponent.fill(matrix4f, -2, 0, l + 4, 9, u << 24);
            String string2 = new TranslatableComponent("chat.queue", this.chatQueue.size()).getColoredString();
            RenderSystem.enableBlend();
            this.minecraft.font.drawShadow(string2, 0.0f, 1.0f, 0xFFFFFF + (n << 24));
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
        }
        if (bl) {
            n = this.minecraft.font.lineHeight;
            RenderSystem.translatef(-3.0f, 0.0f, 0.0f);
            int u = k * n + k;
            int o = m * n + m;
            int v = this.chatScrollbarPos * o / k;
            int w = o * o / u;
            if (u != o) {
                q = v > 0 ? 170 : 96;
                r = this.newMessageSinceScroll ? 0xCC3333 : 0x3333AA;
                ChatComponent.fill(0, -v, 2, -v - w, r + (q << 24));
                ChatComponent.fill(2, -v, 1, -v - w, 0xCCCCCC + (q << 24));
            }
        }
        RenderSystem.popMatrix();
    }

    private boolean isChatVisible() {
        return this.minecraft.options.chatVisibility != ChatVisiblity.HIDDEN;
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
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (bl) {
            this.recentChat.clear();
        }
    }

    public void addMessage(Component component) {
        this.addMessage(component, 0);
    }

    public void addMessage(Component component, int i) {
        this.addMessage(component, i, this.minecraft.gui.getGuiTicks(), false);
        LOGGER.info("[CHAT] {}", (Object)component.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void addMessage(Component component, int i, int j, boolean bl) {
        if (i != 0) {
            this.removeById(i);
        }
        int k = Mth.floor((double)this.getWidth() / this.getScale());
        List<Component> list = ComponentRenderUtils.wrapComponents(component, k, this.minecraft.font, false, false);
        boolean bl2 = this.isChatFocused();
        for (Component component2 : list) {
            if (bl2 && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1.0);
            }
            this.trimmedMessages.add(0, new GuiMessage(j, component2, i));
        }
        while (this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
        if (!bl) {
            this.allMessages.add(0, new GuiMessage(j, component, i));
            while (this.allMessages.size() > 100) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }
    }

    public void rescaleChat() {
        this.trimmedMessages.clear();
        this.resetChatScroll();
        for (int i = this.allMessages.size() - 1; i >= 0; --i) {
            GuiMessage guiMessage = this.allMessages.get(i);
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
        if (!this.isChatFocused() || this.minecraft.options.hideGui || !this.isChatVisible() || this.chatQueue.isEmpty()) {
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
    public Component getClickedComponentAt(double d, double e) {
        if (!this.isChatFocused() || this.minecraft.options.hideGui || !this.isChatVisible()) {
            return null;
        }
        double f = this.getScale() * (this.minecraft.options.chatLineSpacing + 1.0);
        double g = d - 2.0;
        double h = (double)this.minecraft.getWindow().getGuiScaledHeight() - e - 40.0;
        g = Mth.floor(g / f);
        h = Mth.floor(h / f);
        if (g < 0.0 || h < 0.0) {
            return null;
        }
        int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
        if (g <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && h < (double)(this.minecraft.font.lineHeight * i + i)) {
            int j = (int)(h / (double)this.minecraft.font.lineHeight + (double)this.chatScrollbarPos);
            if (j >= 0 && j < this.trimmedMessages.size()) {
                GuiMessage guiMessage = this.trimmedMessages.get(j);
                int k = 0;
                for (Component component : guiMessage.getMessage()) {
                    if (!(component instanceof TextComponent) || !((double)(k += this.minecraft.font.width(ComponentRenderUtils.stripColor(((TextComponent)component).getText(), false))) > g)) continue;
                    return component;
                }
            }
            return null;
        }
        return null;
    }

    public boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    public void removeById(int i) {
        GuiMessage guiMessage;
        Iterator<GuiMessage> iterator = this.trimmedMessages.iterator();
        while (iterator.hasNext()) {
            guiMessage = iterator.next();
            if (guiMessage.getId() != i) continue;
            iterator.remove();
        }
        iterator = this.allMessages.iterator();
        while (iterator.hasNext()) {
            guiMessage = iterator.next();
            if (guiMessage.getId() != i) continue;
            iterator.remove();
            break;
        }
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

