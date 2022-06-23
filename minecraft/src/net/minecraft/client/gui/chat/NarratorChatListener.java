package net.minecraft.client.gui.chat;

import com.mojang.logging.LogUtils;
import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class NarratorChatListener implements ChatListener {
	public static final Component NO_TITLE = CommonComponents.EMPTY;
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final NarratorChatListener INSTANCE = new NarratorChatListener();
	private final Narrator narrator = Narrator.getNarrator();

	@Override
	public void handle(ChatType chatType, Component component, @Nullable ChatSender chatSender) {
		NarratorStatus narratorStatus = getStatus();
		if (narratorStatus != NarratorStatus.OFF) {
			if (!this.narrator.active()) {
				this.logNarratedMessage(component.getString());
			} else {
				chatType.narration().ifPresent(narration -> {
					if (narratorStatus.shouldNarrate(narration.priority())) {
						Component component2 = narration.decorate(component, chatSender);
						String string = component2.getString();
						this.logNarratedMessage(string);
						this.narrator.say(string, narration.priority().interrupts());
					}
				});
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
		this.narrator.say(Component.translatable("options.narrator").append(" : ").append(narratorStatus.getName()).getString(), true);
		ToastComponent toastComponent = Minecraft.getInstance().getToasts();
		if (this.narrator.active()) {
			if (narratorStatus == NarratorStatus.OFF) {
				SystemToast.addOrUpdate(toastComponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, Component.translatable("narrator.toast.disabled"), null);
			} else {
				SystemToast.addOrUpdate(
					toastComponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, Component.translatable("narrator.toast.enabled"), narratorStatus.getName()
				);
			}
		} else {
			SystemToast.addOrUpdate(
				toastComponent,
				SystemToast.SystemToastIds.NARRATOR_TOGGLE,
				Component.translatable("narrator.toast.disabled"),
				Component.translatable("options.narrator.notavailable")
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
