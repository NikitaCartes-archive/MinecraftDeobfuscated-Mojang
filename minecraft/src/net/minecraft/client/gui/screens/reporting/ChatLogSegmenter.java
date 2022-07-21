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
public class ChatLogSegmenter<T extends LoggedChatMessage> {
	private final Function<ChatLog.Entry<T>, ChatLogSegmenter.MessageType> typeFunction;
	private final List<ChatLog.Entry<T>> messages = new ArrayList();
	@Nullable
	private ChatLogSegmenter.MessageType segmentType;

	public ChatLogSegmenter(Function<ChatLog.Entry<T>, ChatLogSegmenter.MessageType> function) {
		this.typeFunction = function;
	}

	public boolean accept(ChatLog.Entry<T> entry) {
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
	public ChatLogSegmenter.Results<T> build() {
		return !this.messages.isEmpty() && this.segmentType != null ? new ChatLogSegmenter.Results<>(this.messages, this.segmentType) : null;
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
	public static record Results<T extends LoggedChatMessage>(List<ChatLog.Entry<T>> messages, ChatLogSegmenter.MessageType type) {
	}
}
