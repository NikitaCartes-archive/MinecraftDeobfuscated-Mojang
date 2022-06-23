package net.minecraft.client.gui.screens.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.LoggedChat;

@Environment(EnvType.CLIENT)
public class ChatLogSegmenter {
	private final Function<LoggedChat.WithId, ChatLogSegmenter.MessageType> typeFunction;
	private final List<LoggedChat.WithId> messages = new ArrayList();
	@Nullable
	private ChatLogSegmenter.MessageType segmentType;

	public ChatLogSegmenter(Function<LoggedChat.WithId, ChatLogSegmenter.MessageType> function) {
		this.typeFunction = function;
	}

	public boolean accept(LoggedChat.WithId withId) {
		ChatLogSegmenter.MessageType messageType = (ChatLogSegmenter.MessageType)this.typeFunction.apply(withId);
		if (this.segmentType != null && messageType != this.segmentType) {
			return false;
		} else {
			this.segmentType = messageType;
			this.messages.add(withId);
			return true;
		}
	}

	@Nullable
	public ChatLogSegmenter.Results build() {
		return !this.messages.isEmpty() && this.segmentType != null ? new ChatLogSegmenter.Results(this.messages, this.segmentType) : null;
	}

	@Environment(EnvType.CLIENT)
	public static enum MessageType {
		REPORTABLE,
		CONTEXT;

		public boolean foldable() {
			return this == CONTEXT;
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Results(List<LoggedChat.WithId> messages, ChatLogSegmenter.MessageType type) {
	}
}
