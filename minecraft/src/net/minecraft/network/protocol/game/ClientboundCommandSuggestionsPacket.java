package net.minecraft.network.protocol.game;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundCommandSuggestionsPacket(int id, int start, int length, List<ClientboundCommandSuggestionsPacket.Entry> suggestions)
	implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCommandSuggestionsPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		ClientboundCommandSuggestionsPacket::id,
		ByteBufCodecs.VAR_INT,
		ClientboundCommandSuggestionsPacket::start,
		ByteBufCodecs.VAR_INT,
		ClientboundCommandSuggestionsPacket::length,
		ClientboundCommandSuggestionsPacket.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
		ClientboundCommandSuggestionsPacket::suggestions,
		ClientboundCommandSuggestionsPacket::new
	);

	public ClientboundCommandSuggestionsPacket(int i, Suggestions suggestions) {
		this(
			i,
			suggestions.getRange().getStart(),
			suggestions.getRange().getLength(),
			suggestions.getList()
				.stream()
				.map(
					suggestion -> new ClientboundCommandSuggestionsPacket.Entry(
							suggestion.getText(), Optional.ofNullable(suggestion.getTooltip()).map(ComponentUtils::fromMessage)
						)
				)
				.toList()
		);
	}

	@Override
	public PacketType<ClientboundCommandSuggestionsPacket> type() {
		return GamePacketTypes.CLIENTBOUND_COMMAND_SUGGESTIONS;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleCommandSuggestions(this);
	}

	public Suggestions toSuggestions() {
		StringRange stringRange = StringRange.between(this.start, this.start + this.length);
		return new Suggestions(
			stringRange, this.suggestions.stream().map(entry -> new Suggestion(stringRange, entry.text(), (Message)entry.tooltip().orElse(null))).toList()
		);
	}

	public static record Entry(String text, Optional<Component> tooltip) {
		public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCommandSuggestionsPacket.Entry> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			ClientboundCommandSuggestionsPacket.Entry::text,
			ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC,
			ClientboundCommandSuggestionsPacket.Entry::tooltip,
			ClientboundCommandSuggestionsPacket.Entry::new
		);
	}
}
