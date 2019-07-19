/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
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
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;

    public ChatComponent(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(int i) {
        int q;
        int p;
        int o;
        int n;
        if (this.minecraft.options.chatVisibility == ChatVisiblity.HIDDEN) {
            return;
        }
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
        GlStateManager.pushMatrix();
        GlStateManager.translatef(2.0f, 8.0f, 0.0f);
        GlStateManager.scaled(d, d, 1.0);
        double e = this.minecraft.options.chatOpacity * (double)0.9f + (double)0.1f;
        double f = this.minecraft.options.textBackgroundOpacity;
        int m = 0;
        for (n = 0; n + this.chatScrollbarPos < this.trimmedMessages.size() && n < j; ++n) {
            GuiMessage guiMessage = this.trimmedMessages.get(n + this.chatScrollbarPos);
            if (guiMessage == null || (o = i - guiMessage.getAddedTime()) >= 200 && !bl) continue;
            double g = bl ? 1.0 : ChatComponent.getTimeFactor(o);
            p = (int)(255.0 * g * e);
            q = (int)(255.0 * g * f);
            ++m;
            if (p <= 3) continue;
            boolean r = false;
            int s = -n * 9;
            ChatComponent.fill(-2, s - 9, 0 + l + 4, s, q << 24);
            String string = guiMessage.getMessage().getColoredString();
            GlStateManager.enableBlend();
            this.minecraft.font.drawShadow(string, 0.0f, s - 8, 0xFFFFFF + (p << 24));
            GlStateManager.disableAlphaTest();
            GlStateManager.disableBlend();
        }
        if (bl) {
            n = this.minecraft.font.lineHeight;
            GlStateManager.translatef(-3.0f, 0.0f, 0.0f);
            int t = k * n + k;
            o = m * n + m;
            int u = this.chatScrollbarPos * o / k;
            int v = o * o / t;
            if (t != o) {
                p = u > 0 ? 170 : 96;
                q = this.newMessageSinceScroll ? 0xCC3333 : 0x3333AA;
                ChatComponent.fill(0, -u, 2, -u - v, q + (p << 24));
                ChatComponent.fill(2, -u, 1, -u - v, 0xCCCCCC + (p << 24));
            }
        }
        GlStateManager.popMatrix();
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

    @Nullable
    public Component getClickedComponentAt(double d, double e) {
        if (!this.isChatFocused()) {
            return null;
        }
        double f = this.getScale();
        double g = d - 2.0;
        double h = (double)this.minecraft.window.getGuiScaledHeight() - e - 40.0;
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
        return ChatComponent.getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused : this.minecraft.options.chatHeightUnfocused);
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
}

