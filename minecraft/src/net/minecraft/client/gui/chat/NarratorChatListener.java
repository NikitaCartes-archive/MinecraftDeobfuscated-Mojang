package net.minecraft.client.gui.chat;

import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class NarratorChatListener implements ChatListener {
	public static final Component NO_TITLE = TextComponent.EMPTY;
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final NarratorChatListener INSTANCE = new NarratorChatListener();
	private final Narrator narrator = Narrator.getNarrator();

	@Override
	public void handle(ChatType chatType, Component component, UUID uUID) {
		NarratorStatus narratorStatus = getStatus();
		if (narratorStatus != NarratorStatus.OFF) {
			if (!this.narrator.active()) {
				this.logNarratedMessage(component.getString());
			} else {
				if (narratorStatus == NarratorStatus.ALL
					|| narratorStatus == NarratorStatus.CHAT && chatType == ChatType.CHAT
					|| narratorStatus == NarratorStatus.SYSTEM && chatType == ChatType.SYSTEM) {
					Component component2;
					if (component instanceof TranslatableComponent && "chat.type.text".equals(((TranslatableComponent)component).getKey())) {
						component2 = new TranslatableComponent("chat.type.text.narrate", ((TranslatableComponent)component).getArgs());
					} else {
						component2 = component;
					}

					String string = component2.getString();
					this.logNarratedMessage(string);
					this.narrator.say(string, chatType.shouldInterrupt());
				}
			}
		}
	}

	public void sayNow(Component component) {
		this.sayNow(component.getString());
	}

	public void sayNow(String string) {
		NarratorStatus narratorStatus = getStatus();
		if (narratorStatus != NarratorStatus.OFF && narratorStatus != NarratorStatus.CHAT && !string.isEmpty()) {
			this.logNarratedMessage(string);
			if (this.narrator.active()) {
				this.narrator.clear();
				this.narrator.say(string, true);
			}
		}
	}

	private static NarratorStatus getStatus() {
		return Minecraft.getInstance().options.narrator().get();
	}

	private void logNarratedMessage(String string) {
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			LOGGER.debug("Narrating: {}", string.replaceAll("\n", "\\\\n"));
		}
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
