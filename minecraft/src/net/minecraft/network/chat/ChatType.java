package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
	public static final Codec<ChatType> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ChatTypeDecoration.CODEC.fieldOf("chat").forGetter(ChatType::chat), ChatTypeDecoration.CODEC.fieldOf("narration").forGetter(ChatType::narration)
				)
				.apply(instance, ChatType::new)
	);
	public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
	public static final ResourceKey<ChatType> CHAT = create("chat");
	public static final ResourceKey<ChatType> SAY_COMMAND = create("say_command");
	public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = create("msg_command_incoming");
	public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = create("msg_command_outgoing");
	public static final ResourceKey<ChatType> TEAM_MSG_COMMAND = create("team_msg_command");
	public static final ResourceKey<ChatType> EMOTE_COMMAND = create("emote_command");

	private static ResourceKey<ChatType> create(String string) {
		return ResourceKey.create(Registry.CHAT_TYPE_REGISTRY, new ResourceLocation(string));
	}

	public static Holder<ChatType> bootstrap(Registry<ChatType> registry) {
		BuiltinRegistries.register(registry, CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
		BuiltinRegistries.register(
			registry, SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		BuiltinRegistries.register(
			registry,
			MSG_COMMAND_INCOMING,
			new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		BuiltinRegistries.register(
			registry,
			MSG_COMMAND_OUTGOING,
			new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		BuiltinRegistries.register(
			registry, TEAM_MSG_COMMAND, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		return BuiltinRegistries.register(
			registry, EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote"))
		);
	}
}
