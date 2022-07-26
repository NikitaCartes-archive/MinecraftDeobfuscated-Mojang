package net.minecraft.network.chat;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

public record ChatMessageContent(String plain, Component decorated) {
	public ChatMessageContent(String string) {
		this(string, Component.literal(string));
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
