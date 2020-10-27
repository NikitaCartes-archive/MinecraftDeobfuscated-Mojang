/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.chat;

import com.mojang.text2speech.Narrator;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.gui.chat.ChatListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class NarratorChatListener
implements ChatListener {
    public static final Component NO_TITLE = TextComponent.EMPTY;
    private static final Logger LOGGER = LogManager.getLogger();
    public static final NarratorChatListener INSTANCE = new NarratorChatListener();
    private final Narrator narrator = Narrator.getNarrator();

    @Override
    public void handle(ChatType chatType, Component component, UUID uUID) {
        NarratorStatus narratorStatus = NarratorChatListener.getStatus();
        if (narratorStatus == NarratorStatus.OFF || !this.narrator.active()) {
            return;
        }
        if (narratorStatus == NarratorStatus.ALL || narratorStatus == NarratorStatus.CHAT && chatType == ChatType.CHAT || narratorStatus == NarratorStatus.SYSTEM && chatType == ChatType.SYSTEM) {
            Component component2 = component instanceof TranslatableComponent && "chat.type.text".equals(((TranslatableComponent)component).getKey()) ? new TranslatableComponent("chat.type.text.narrate", ((TranslatableComponent)component).getArgs()) : component;
            this.doSay(chatType.shouldInterrupt(), component2.getString());
        }
    }

    public void sayNow(String string) {
        NarratorStatus narratorStatus = NarratorChatListener.getStatus();
        if (this.narrator.active() && narratorStatus != NarratorStatus.OFF && narratorStatus != NarratorStatus.CHAT && !string.isEmpty()) {
            this.narrator.clear();
            this.doSay(true, string);
        }
    }

    private static NarratorStatus getStatus() {
        return Minecraft.getInstance().options.narratorStatus;
    }

    private void doSay(boolean bl, String string) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.debug("Narrating: {}", (Object)string.replaceAll("\n", "\\\\n"));
        }
        this.narrator.say(string, bl);
    }

    public void updateNarratorStatus(NarratorStatus narratorStatus) {
        this.clear();
        this.narrator.say(new TranslatableComponent("options.narrator").append(" : ").append(narratorStatus.getName()).getString(), true);
        ToastComponent toastComponent = Minecraft.getInstance().getToasts();
        if (this.narrator.active()) {
            if (narratorStatus == NarratorStatus.OFF) {
                SystemToast.addOrUpdate(toastComponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, new TranslatableComponent("narrator.toast.disabled"), null);
            } else {
                SystemToast.addOrUpdate(toastComponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, new TranslatableComponent("narrator.toast.enabled"), narratorStatus.getName());
            }
        } else {
            SystemToast.addOrUpdate(toastComponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, new TranslatableComponent("narrator.toast.disabled"), new TranslatableComponent("options.narrator.notavailable"));
        }
    }

    public boolean isActive() {
        return this.narrator.active();
    }

    public void clear() {
        if (NarratorChatListener.getStatus() == NarratorStatus.OFF || !this.narrator.active()) {
            return;
        }
        this.narrator.clear();
    }

    public void destroy() {
        this.narrator.destroy();
    }
}

