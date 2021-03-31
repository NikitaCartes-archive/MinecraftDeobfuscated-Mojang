package net.minecraft.network.protocol.game;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;

public class ClientboundCommandSuggestionsPacket implements Packet<ClientGamePacketListener> {
	private final int id;
	private final Suggestions suggestions;

	public ClientboundCommandSuggestionsPacket(int i, Suggestions suggestions) {
		this.id = i;
		this.suggestions = suggestions;
	}

	public ClientboundCommandSuggestionsPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		int i = friendlyByteBuf.readVarInt();
		int j = friendlyByteBuf.readVarInt();
		StringRange stringRange = StringRange.between(i, i + j);
		List<Suggestion> list = friendlyByteBuf.readList(friendlyByteBufx -> {
			String string = friendlyByteBufx.readUtf();
			Component component = friendlyByteBufx.readBoolean() ? friendlyByteBufx.readComponent() : null;
			return new Suggestion(stringRange, string, component);
		});
		this.suggestions = new Suggestions(stringRange, list);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeVarInt(this.suggestions.getRange().getStart());
		friendlyByteBuf.writeVarInt(this.suggestions.getRange().getLength());
		friendlyByteBuf.writeCollection(this.suggestions.getList(), (friendlyByteBufx, suggestion) -> {
			friendlyByteBufx.writeUtf(suggestion.getText());
			friendlyByteBufx.writeBoolean(suggestion.getTooltip() != null);
			if (suggestion.getTooltip() != null) {
				friendlyByteBufx.writeComponent(ComponentUtils.fromMessage(suggestion.getTooltip()));
			}
		});
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleCommandSuggestions(this);
	}

	public int getId() {
		return this.id;
	}

	public Suggestions getSuggestions() {
		return this.suggestions;
	}
}
