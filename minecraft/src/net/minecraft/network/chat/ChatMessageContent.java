package net.minecraft.network.chat;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.network.FilteredText;

public record ChatMessageContent(String plain, Component decorated) {
	public ChatMessageContent(String string) {
		this(string, Component.literal(string));
	}

	public static FilteredText<ChatMessageContent> fromFiltered(FilteredText<String> filteredText) {
		return filteredText.map(ChatMessageContent::new);
	}

	public static FilteredText<ChatMessageContent> fromFiltered(FilteredText<String> filteredText, FilteredText<Component> filteredText2) {
		return filteredText.map(
			string -> new ChatMessageContent(string, filteredText2.raw()),
			string -> filteredText2.filtered() != null ? new ChatMessageContent(string, filteredText2.filtered()) : null
		);
	}

	public boolean isDecorated() {
		return !this.decorated.equals(Component.literal(this.plain));
	}

	public static ChatMessageContent read(FriendlyByteBuf friendlyByteBuf) {
		String string = friendlyByteBuf.readUtf(256);
		Component component = friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
		return new ChatMessageContent(string, (Component)Objects.requireNonNullElse(component, Component.literal(string)));
	}

	public static void write(FriendlyByteBuf friendlyByteBuf, ChatMessageContent chatMessageContent) {
		friendlyByteBuf.writeUtf(chatMessageContent.plain(), 256);
		Component component = chatMessageContent.isDecorated() ? chatMessageContent.decorated() : null;
		friendlyByteBuf.writeNullable(component, FriendlyByteBuf::writeComponent);
	}
}
