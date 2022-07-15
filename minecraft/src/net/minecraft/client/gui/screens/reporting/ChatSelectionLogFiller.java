package net.minecraft.client.gui.screens.reporting;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChatSelectionLogFiller {
	private static final int CONTEXT_FOLDED_SIZE = 4;
	private final ChatLog log;
	private final Predicate<LoggedChatMessage> canReport;
	private int nextMessageId;

	public ChatSelectionLogFiller(ChatLog chatLog, Predicate<LoggedChatMessage> predicate) {
		this.log = chatLog;
		this.canReport = predicate;
		this.nextMessageId = chatLog.newest();
	}

	public void fillNextPage(int i, ChatSelectionLogFiller.Output output) {
		int j = 0;

		while (j < i) {
			ChatLogSegmenter.Results results = this.nextSegment();
			if (results == null) {
				break;
			}

			if (results.type().foldable()) {
				j += addFoldedMessagesTo(results.messages(), output);
			} else {
				output.acceptMessages(results.messages());
				j += results.messages().size();
			}
		}
	}

	private static int addFoldedMessagesTo(List<ChatLog.Entry<LoggedChatMessage>> list, ChatSelectionLogFiller.Output output) {
		int i = 8;
		if (list.size() > 8) {
			int j = list.size() - 8;
			output.acceptMessages(list.subList(0, 4));
			output.acceptDivider(Component.translatable("gui.chatSelection.fold", j));
			output.acceptMessages(list.subList(list.size() - 4, list.size()));
			return 9;
		} else {
			output.acceptMessages(list);
			return list.size();
		}
	}

	@Nullable
	private ChatLogSegmenter.Results nextSegment() {
		ChatLogSegmenter chatLogSegmenter = new ChatLogSegmenter(entry -> this.getMessageType((LoggedChatMessage)entry.event()));
		OptionalInt optionalInt = this.log
			.selectBefore(this.nextMessageId)
			.entries()
			.map(entry -> entry.tryCast(LoggedChatMessage.class))
			.filter(Objects::nonNull)
			.takeWhile(chatLogSegmenter::accept)
			.mapToInt(ChatLog.Entry::id)
			.reduce((i, j) -> j);
		if (optionalInt.isPresent()) {
			this.nextMessageId = this.log.before(optionalInt.getAsInt());
		}

		return chatLogSegmenter.build();
	}

	private ChatLogSegmenter.MessageType getMessageType(LoggedChatMessage loggedChatMessage) {
		return this.canReport.test(loggedChatMessage) ? ChatLogSegmenter.MessageType.REPORTABLE : ChatLogSegmenter.MessageType.CONTEXT;
	}

	@Environment(EnvType.CLIENT)
	public interface Output {
		default void acceptMessages(Iterable<ChatLog.Entry<LoggedChatMessage>> iterable) {
			for (ChatLog.Entry<LoggedChatMessage> entry : iterable) {
				this.acceptMessage(entry.id(), entry.event());
			}
		}

		void acceptMessage(int i, LoggedChatMessage loggedChatMessage);

		void acceptDivider(Component component);
	}
}
