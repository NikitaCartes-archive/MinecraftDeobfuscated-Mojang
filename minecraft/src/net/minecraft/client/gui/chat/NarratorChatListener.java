package net.minecraft.client.gui.chat;

import com.mojang.text2speech.Narrator;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class NarratorChatListener implements ChatListener {
	public static final Component NO_TITLE = TextComponent.EMPTY;
	private static final Logger LOGGER = LogManager.getLogger();
	public static final NarratorChatListener INSTANCE = new NarratorChatListener();
	private final Narrator narrator = Narrator.getNarrator();

	@Override
	public void handle(ChatType chatType, Component component, UUID uUID) {
		if (!Minecraft.getInstance().isBlocked(uUID)) {
			NarratorStatus narratorStatus = getStatus();
			if (narratorStatus != NarratorStatus.OFF && this.narrator.active()) {
				if (narratorStatus == NarratorStatus.ALL
					|| narratorStatus == NarratorStatus.CHAT && chatType == ChatType.CHAT
					|| narratorStatus == NarratorStatus.SYSTEM && chatType == ChatType.SYSTEM) {
					Component component2;
					if (component instanceof TranslatableComponent && "chat.type.text".equals(((TranslatableComponent)component).getKey())) {
						component2 = new TranslatableComponent("chat.type.text.narrate", ((TranslatableComponent)component).getArgs());
					} else {
						component2 = component;
					}

					this.doSay(chatType.shouldInterrupt(), component2.getString());
				}
			}
		}
	}

	public void sayNow(String string) {
		NarratorStatus narratorStatus = getStatus();
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
			LOGGER.debug("Narrating: {}", string);
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
				SystemToast.addOrUpdate(
					toastComponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, new TranslatableComponent("narrator.toast.enabled"), narratorStatus.getName()
				);
			}
		} else {
			SystemToast.addOrUpdate(
				toastComponent,
				SystemToast.SystemToastIds.NARRATOR_TOGGLE,
				new TranslatableComponent("narrator.toast.disabled"),
				new TranslatableComponent("options.narrator.notavailable")
			);
		}
	}

	public boolean isActive() {
		return this.narrator.active();
	}

	public void clear() {
		if (getStatus() != NarratorStatus.OFF && this.narrator.active()) {
			this.narrator.clear();
		}
	}

	public void destroy() {
		this.narrator.destroy();
	}
}
