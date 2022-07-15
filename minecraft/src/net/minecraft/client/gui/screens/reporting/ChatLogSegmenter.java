package net.minecraft.client.gui.screens.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;

@Environment(EnvType.CLIENT)
public class ChatLogSegmenter {
	private final Function<ChatLog.Entry<LoggedChatMessage>, ChatLogSegmenter.MessageType> typeFunction;
	private final List<ChatLog.Entry<LoggedChatMessage>> messages = new ArrayList();
	@Nullable
	private ChatLogSegmenter.MessageType segmentType;

	public ChatLogSegmenter(Function<ChatLog.Entry<LoggedChatMessage>, ChatLogSegmenter.MessageType> function) {
		this.typeFunction = function;
	}

	public boolean accept(ChatLog.Entry<LoggedChatMessage> entry) {
		ChatLogSegmenter.MessageType messageType = (ChatLogSegmenter.MessageType)this.typeFunction.apply(entry);
		if (this.segmentType != null && messageType != this.segmentType) {
			return false;
		} else {
			this.segmentType = messageType;
			this.messages.add(entry);
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
	public static record Results(List<ChatLog.Entry<LoggedChatMessage>> messages, ChatLogSegmenter.MessageType type) {
	}
}
