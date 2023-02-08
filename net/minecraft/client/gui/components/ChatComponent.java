/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ChatComponent
extends GuiComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_CHAT_HISTORY = 100;
    private static final int MESSAGE_NOT_FOUND = -1;
    private static final int MESSAGE_INDENT = 4;
    private static final int MESSAGE_TAG_MARGIN_LEFT = 4;
    private static final int BOTTOM_MARGIN = 40;
    private static final int TIME_BEFORE_MESSAGE_DELETION = 60;
    private static final Component DELETED_CHAT_MESSAGE = Component.translatable("chat.deleted_marker").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage> allMessages = Lists.newArrayList();
    private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private final List<DelayedMessageDeletion> messageDeletionQueue = new ArrayList<DelayedMessageDeletion>();

    public ChatComponent(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void tick() {
        if (!this.messageDeletionQueue.isEmpty()) {
            this.processMessageDeletionQueue();
        }
    }

    public void render(PoseStack poseStack, int i, int j, int k) {
        int aa;
        int z;
        int y;
        int x;
        int w;
        if (this.isChatHidden()) {
            return;
        }
        int l = this.getLinesPerPage();
        int m = this.trimmedMessages.size();
        if (m <= 0) {
            return;
        }
        boolean bl = this.isChatFocused();
        float f = (float)this.getScale();
        int n = Mth.ceil((float)this.getWidth() / f);
        int o = this.minecraft.getWindow().getGuiScaledHeight();
        poseStack.pushPose();
        poseStack.scale(f, f, 1.0f);
        poseStack.translate(4.0f, 0.0f, 0.0f);
        int p = Mth.floor((float)(o - 40) / f);
        int q = this.getMessageEndIndexAt(this.screenToChatX(j), this.screenToChatY(k));
        double d = this.minecraft.options.chatOpacity().get() * (double)0.9f + (double)0.1f;
        double e = this.minecraft.options.textBackgroundOpacity().get();
        double g = this.minecraft.options.chatLineSpacing().get();
        int r = this.getLineHeight();
        int s = (int)Math.round(-8.0 * (g + 1.0) + 4.0 * g);
        int t = 0;
        for (int u = 0; u + this.chatScrollbarPos < this.trimmedMessages.size() && u < l; ++u) {
            int v = u + this.chatScrollbarPos;
            GuiMessage.Line line = this.trimmedMessages.get(v);
            if (line == null || (w = i - line.addedTime()) >= 200 && !bl) continue;
            double h = bl ? 1.0 : ChatComponent.getTimeFactor(w);
            x = (int)(255.0 * h * d);
            y = (int)(255.0 * h * e);
            ++t;
            if (x <= 3) continue;
            z = 0;
            aa = p - u * r;
            int ab = aa + s;
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 50.0f);
            ChatComponent.fill(poseStack, -4, aa - r, 0 + n + 4 + 4, aa, y << 24);
            GuiMessageTag guiMessageTag = line.tag();
            if (guiMessageTag != null) {
                int ac = guiMessageTag.indicatorColor() | x << 24;
                ChatComponent.fill(poseStack, -4, aa - r, -2, aa, ac);
                if (v == q && guiMessageTag.icon() != null) {
                    int ad = this.getTagIconLeft(line);
                    int ae = ab + this.minecraft.font.lineHeight;
                    this.drawTagIcon(poseStack, ad, ae, guiMessageTag.icon());
                }
            }
            RenderSystem.enableBlend();
            poseStack.translate(0.0f, 0.0f, 50.0f);
            this.minecraft.font.drawShadow(poseStack, line.content(), 0.0f, (float)ab, 0xFFFFFF + (x << 24));
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
        long af = this.minecraft.getChatListener().queueSize();
        if (af > 0L) {
            int ag = (int)(128.0 * d);
            w = (int)(255.0 * e);
            poseStack.pushPose();
            poseStack.translate(0.0f, p, 50.0f);
            ChatComponent.fill(poseStack, -2, 0, n + 4, 9, w << 24);
            RenderSystem.enableBlend();
            poseStack.translate(0.0f, 0.0f, 50.0f);
            this.minecraft.font.drawShadow(poseStack, Component.translatable("chat.queue", af), 0.0f, 1.0f, 0xFFFFFF + (ag << 24));
            poseStack.popPose();
            RenderSystem.disableBlend();
        }
        if (bl) {
            int ag = this.getLineHeight();
            w = m * ag;
            int ah = t * ag;
            int ai = this.chatScrollbarPos * ah / m - p;
            x = ah * ah / w;
            if (w != ah) {
                y = ai > 0 ? 170 : 96;
                z = this.newMessageSinceScroll ? 0xCC3333 : 0x3333AA;
                aa = n + 4;
                ChatComponent.fill(poseStack, aa, -ai, aa + 2, -ai - x, z + (y << 24));
                ChatComponent.fill(poseStack, aa + 2, -ai, aa + 1, -ai - x, 0xCCCCCC + (y << 24));
            }
        }
        poseStack.popPose();
    }

    private void drawTagIcon(PoseStack poseStack, int i, int j, GuiMessageTag.Icon icon) {
        int k = j - icon.height - 1;
        icon.draw(poseStack, i, k);
    }

    private int getTagIconLeft(GuiMessage.Line line) {
        return this.minecraft.font.width(line.content()) + 4;
    }

    private boolean isChatHidden() {
        return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
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
        this.minecraft.getChatListener().clearQueue();
        this.messageDeletionQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (bl) {
            this.recentChat.clear();
        }
    }

    public void addMessage(Component component) {
        this.addMessage(component, null, this.minecraft.isSingleplayer() ? GuiMessageTag.systemSinglePlayer() : GuiMessageTag.system());
    }

    public void addMessage(Component component, @Nullable MessageSignature messageSignature, @Nullable GuiMessageTag guiMessageTag) {
        this.logChatMessage(component, guiMessageTag);
        this.addMessage(component, messageSignature, this.minecraft.gui.getGuiTicks(), guiMessageTag, false);
    }

    private void logChatMessage(Component component, @Nullable GuiMessageTag guiMessageTag) {
        String string = component.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String string2 = Optionull.map(guiMessageTag, GuiMessageTag::logTag);
        if (string2 != null) {
            LOGGER.info("[{}] [CHAT] {}", (Object)string2, (Object)string);
        } else {
            LOGGER.info("[CHAT] {}", (Object)string);
        }
    }

    private void addMessage(Component component, @Nullable MessageSignature messageSignature, int i, @Nullable GuiMessageTag guiMessageTag, boolean bl) {
        int j = Mth.floor((double)this.getWidth() / this.getScale());
        if (guiMessageTag != null && guiMessageTag.icon() != null) {
            j -= guiMessageTag.icon().width + 4 + 2;
        }
        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(component, j, this.minecraft.font);
        boolean bl2 = this.isChatFocused();
        for (int k = 0; k < list.size(); ++k) {
            FormattedCharSequence formattedCharSequence = list.get(k);
            if (bl2 && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }
            boolean bl3 = k == list.size() - 1;
            this.trimmedMessages.add(0, new GuiMessage.Line(i, formattedCharSequence, guiMessageTag, bl3));
        }
        while (this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
        if (!bl) {
            this.allMessages.add(0, new GuiMessage(i, component, messageSignature, guiMessageTag));
            while (this.allMessages.size() > 100) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }
    }

    private void processMessageDeletionQueue() {
        int i = this.minecraft.gui.getGuiTicks();
        this.messageDeletionQueue.removeIf(delayedMessageDeletion -> {
            if (i >= delayedMessageDeletion.deletableAfter()) {
                return this.deleteMessageOrDelay(delayedMessageDeletion.signature()) == null;
            }
            return false;
        });
    }

    public void deleteMessage(MessageSignature messageSignature) {
        DelayedMessageDeletion delayedMessageDeletion = this.deleteMessageOrDelay(messageSignature);
        if (delayedMessageDeletion != null) {
            this.messageDeletionQueue.add(delayedMessageDeletion);
        }
    }

    @Nullable
    private DelayedMessageDeletion deleteMessageOrDelay(MessageSignature messageSignature) {
        int i = this.minecraft.gui.getGuiTicks();
        ListIterator<GuiMessage> listIterator = this.allMessages.listIterator();
        while (listIterator.hasNext()) {
            GuiMessage guiMessage = listIterator.next();
            if (!messageSignature.equals(guiMessage.signature())) continue;
            int j = guiMessage.addedTime() + 60;
            if (i >= j) {
                listIterator.set(this.createDeletedMarker(guiMessage));
                this.refreshTrimmedMessage();
                return null;
            }
            return new DelayedMessageDeletion(messageSignature, j);
        }
        return null;
    }

    private GuiMessage createDeletedMarker(GuiMessage guiMessage) {
        return new GuiMessage(guiMessage.addedTime(), DELETED_CHAT_MESSAGE, null, GuiMessageTag.system());
    }

    public void rescaleChat() {
        this.resetChatScroll();
        this.refreshTrimmedMessage();
    }

    private void refreshTrimmedMessage() {
        this.trimmedMessages.clear();
        for (int i = this.allMessages.size() - 1; i >= 0; --i) {
            GuiMessage guiMessage = this.allMessages.get(i);
            this.addMessage(guiMessage.content(), guiMessage.signature(), guiMessage.addedTime(), guiMessage.tag(), true);
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

    public void scrollChat(int i) {
        this.chatScrollbarPos += i;
        int j = this.trimmedMessages.size();
        if (this.chatScrollbarPos > j - this.getLinesPerPage()) {
            this.chatScrollbarPos = j - this.getLinesPerPage();
        }
        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    public boolean handleChatQueueClicked(double d, double e) {
        if (!this.isChatFocused() || this.minecraft.options.hideGui || this.isChatHidden()) {
            return false;
        }
        ChatListener chatListener = this.minecraft.getChatListener();
        if (chatListener.queueSize() == 0L) {
            return false;
        }
        double f = d - 2.0;
        double g = (double)this.minecraft.getWindow().getGuiScaledHeight() - e - 40.0;
        if (f <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && g < 0.0 && g > (double)Mth.floor(-9.0 * this.getScale())) {
            chatListener.acceptNextDelayedMessage();
            return true;
        }
        return false;
    }

    @Nullable
    public Style getClickedComponentStyleAt(double d, double e) {
        double g;
        double f = this.screenToChatX(d);
        int i = this.getMessageLineIndexAt(f, g = this.screenToChatY(e));
        if (i >= 0 && i < this.trimmedMessages.size()) {
            GuiMessage.Line line = this.trimmedMessages.get(i);
            return this.minecraft.font.getSplitter().componentStyleAtWidth(line.content(), Mth.floor(f));
        }
        return null;
    }

    @Nullable
    public GuiMessageTag getMessageTagAt(double d, double e) {
        GuiMessage.Line line;
        GuiMessageTag guiMessageTag;
        double g;
        double f = this.screenToChatX(d);
        int i = this.getMessageEndIndexAt(f, g = this.screenToChatY(e));
        if (i >= 0 && i < this.trimmedMessages.size() && (guiMessageTag = (line = this.trimmedMessages.get(i)).tag()) != null && this.hasSelectedMessageTag(f, line, guiMessageTag)) {
            return guiMessageTag;
        }
        return null;
    }

    private boolean hasSelectedMessageTag(double d, GuiMessage.Line line, GuiMessageTag guiMessageTag) {
        if (d < 0.0) {
            return true;
        }
        GuiMessageTag.Icon icon = guiMessageTag.icon();
        if (icon != null) {
            int i = this.getTagIconLeft(line);
            int j = i + icon.width;
            return d >= (double)i && d <= (double)j;
        }
        return false;
    }

    private double screenToChatX(double d) {
        return d / this.getScale() - 4.0;
    }

    private double screenToChatY(double d) {
        double e = (double)this.minecraft.getWindow().getGuiScaledHeight() - d - 40.0;
        return e / (this.getScale() * (double)this.getLineHeight());
    }

    private int getMessageEndIndexAt(double d, double e) {
        int i = this.getMessageLineIndexAt(d, e);
        if (i == -1) {
            return -1;
        }
        while (i >= 0) {
            if (this.trimmedMessages.get(i).endOfEntry()) {
                return i;
            }
            --i;
        }
        return i;
    }

    private int getMessageLineIndexAt(double d, double e) {
        int j;
        if (!this.isChatFocused() || this.minecraft.options.hideGui || this.isChatHidden()) {
            return -1;
        }
        if (d < -4.0 || d > (double)Mth.floor((double)this.getWidth() / this.getScale())) {
            return -1;
        }
        int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
        if (e >= 0.0 && e < (double)i && (j = Mth.floor(e + (double)this.chatScrollbarPos)) >= 0 && j < this.trimmedMessages.size()) {
            return j;
        }
        return -1;
    }

    private boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    public int getWidth() {
        return ChatComponent.getWidth(this.minecraft.options.chatWidth().get());
    }

    public int getHeight() {
        return ChatComponent.getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get());
    }

    public double getScale() {
        return this.minecraft.options.chatScale().get();
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

    public static double defaultUnfocusedPct() {
        int i = 180;
        int j = 20;
        return 70.0 / (double)(ChatComponent.getHeight(1.0) - 20);
    }

    public int getLinesPerPage() {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight() {
        return (int)((double)this.minecraft.font.lineHeight * (this.minecraft.options.chatLineSpacing().get() + 1.0));
    }

    @Environment(value=EnvType.CLIENT)
    record DelayedMessageDeletion(MessageSignature signature, int deletableAfter) {
    }
}

