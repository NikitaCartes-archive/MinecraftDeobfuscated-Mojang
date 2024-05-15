package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
	public static final Codec<ChatType> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ChatTypeDecoration.CODEC.fieldOf("chat").forGetter(ChatType::chat), ChatTypeDecoration.CODEC.fieldOf("narration").forGetter(ChatType::narration)
				)
				.apply(instance, ChatType::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ChatType> DIRECT_STREAM_CODEC = StreamCodec.composite(
		ChatTypeDecoration.STREAM_CODEC, ChatType::chat, ChatTypeDecoration.STREAM_CODEC, ChatType::narration, ChatType::new
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ChatType>> STREAM_CODEC = ByteBufCodecs.holder(Registries.CHAT_TYPE, DIRECT_STREAM_CODEC);
	public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
	public static final ResourceKey<ChatType> CHAT = create("chat");
	public static final ResourceKey<ChatType> SAY_COMMAND = create("say_command");
	public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = create("msg_command_incoming");
	public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = create("msg_command_outgoing");
	public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_INCOMING = create("team_msg_command_incoming");
	public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = create("team_msg_command_outgoing");
	public static final ResourceKey<ChatType> EMOTE_COMMAND = create("emote_command");

	private static ResourceKey<ChatType> create(String string) {
		return ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation(string));
	}

	public static void bootstrap(BootstrapContext<ChatType> bootstrapContext) {
		bootstrapContext.register(CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
		bootstrapContext.register(
			SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstrapContext.register(
			MSG_COMMAND_INCOMING,
			new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstrapContext.register(
			MSG_COMMAND_OUTGOING,
			new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstrapContext.register(
			TEAM_MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstrapContext.register(
			TEAM_MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.sent"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstrapContext.register(EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
	}

	public static ChatType.Bound bind(ResourceKey<ChatType> resourceKey, Entity entity) {
		return bind(resourceKey, entity.level().registryAccess(), entity.getDisplayName());
	}

	public static ChatType.Bound bind(ResourceKey<ChatType> resourceKey, CommandSourceStack commandSourceStack) {
		return bind(resourceKey, commandSourceStack.registryAccess(), commandSourceStack.getDisplayName());
	}

	public static ChatType.Bound bind(ResourceKey<ChatType> resourceKey, RegistryAccess registryAccess, Component component) {
		Registry<ChatType> registry = registryAccess.registryOrThrow(Registries.CHAT_TYPE);
		return new ChatType.Bound(registry.getHolderOrThrow(resourceKey), component);
	}

	public static record Bound(Holder<ChatType> chatType, Component name, Optional<Component> targetName) {
		public static final StreamCodec<RegistryFriendlyByteBuf, ChatType.Bound> STREAM_CODEC = StreamCodec.composite(
			ChatType.STREAM_CODEC,
			ChatType.Bound::chatType,
			ComponentSerialization.TRUSTED_STREAM_CODEC,
			ChatType.Bound::name,
			ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC,
			ChatType.Bound::targetName,
			ChatType.Bound::new
		);

		Bound(Holder<ChatType> holder, Component component) {
			this(holder, component, Optional.empty());
		}

		public Component decorate(Component component) {
			return this.chatType.value().chat().decorate(component, this);
		}

		public Component decorateNarration(Component component) {
			return this.chatType.value().narration().decorate(component, this);
		}

		public ChatType.Bound withTargetName(Component component) {
			return new ChatType.Bound(this.chatType, this.name, Optional.of(component));
		}
	}
}
