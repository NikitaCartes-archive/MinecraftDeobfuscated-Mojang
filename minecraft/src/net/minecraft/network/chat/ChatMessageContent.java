package net.minecraft.network.chat;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.network.FilteredText;

public record ChatMessageContent(Component plain, Component decorated) {
	public ChatMessageContent(Component component) {
		this(component, component);
	}

	public ChatMessageContent(String string, Component component) {
		this(Component.literal(string), component);
	}

	public ChatMessageContent(String string) {
		this(Component.literal(string));
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
		return !this.decorated.equals(this.plain);
	}

	public static ChatMessageContent read(FriendlyByteBuf friendlyByteBuf) {
		Component component = friendlyByteBuf.readComponent();
		Component component2 = friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
		return new ChatMessageContent(component, (Component)Objects.requireNonNullElse(component2, component));
	}

	public static void write(FriendlyByteBuf friendlyByteBuf, ChatMessageContent chatMessageContent) {
		friendlyByteBuf.writeComponent(chatMessageContent.plain());
		Component component = chatMessageContent.isDecorated() ? chatMessageContent.decorated() : null;
		friendlyByteBuf.writeNullable(component, FriendlyByteBuf::writeComponent);
	}
}
