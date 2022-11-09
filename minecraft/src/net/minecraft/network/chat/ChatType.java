package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

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
	public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_INCOMING = create("team_msg_command_incoming");
	public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = create("team_msg_command_outgoing");
	public static final ResourceKey<ChatType> EMOTE_COMMAND = create("emote_command");

	private static ResourceKey<ChatType> create(String string) {
		return ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation(string));
	}

	public static void bootstrap(BootstapContext<ChatType> bootstapContext) {
		bootstapContext.register(CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
		bootstapContext.register(
			SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstapContext.register(
			MSG_COMMAND_INCOMING,
			new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstapContext.register(
			MSG_COMMAND_OUTGOING,
			new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstapContext.register(
			TEAM_MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstapContext.register(
			TEAM_MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.sent"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
		);
		bootstapContext.register(EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
	}

	public static ChatType.Bound bind(ResourceKey<ChatType> resourceKey, Entity entity) {
		return bind(resourceKey, entity.level.registryAccess(), entity.getDisplayName());
	}

	public static ChatType.Bound bind(ResourceKey<ChatType> resourceKey, CommandSourceStack commandSourceStack) {
		return bind(resourceKey, commandSourceStack.registryAccess(), commandSourceStack.getDisplayName());
	}

	public static ChatType.Bound bind(ResourceKey<ChatType> resourceKey, RegistryAccess registryAccess, Component component) {
		Registry<ChatType> registry = registryAccess.registryOrThrow(Registries.CHAT_TYPE);
		return registry.getOrThrow(resourceKey).bind(component);
	}

	public ChatType.Bound bind(Component component) {
		return new ChatType.Bound(this, component);
	}

	public static record Bound(ChatType chatType, Component name, @Nullable Component targetName) {
		Bound(ChatType chatType, Component component) {
			this(chatType, component, null);
		}

		public Component decorate(Component component) {
			return this.chatType.chat().decorate(component, this);
		}

		public Component decorateNarration(Component component) {
			return this.chatType.narration().decorate(component, this);
		}

		public ChatType.Bound withTargetName(Component component) {
			return new ChatType.Bound(this.chatType, this.name, component);
		}

		public ChatType.BoundNetwork toNetwork(RegistryAccess registryAccess) {
			Registry<ChatType> registry = registryAccess.registryOrThrow(Registries.CHAT_TYPE);
			return new ChatType.BoundNetwork(registry.getId(this.chatType), this.name, this.targetName);
		}
	}

	public static record BoundNetwork(int chatType, Component name, @Nullable Component targetName) {
		public BoundNetwork(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readComponent(), friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeVarInt(this.chatType);
			friendlyByteBuf.writeComponent(this.name);
			friendlyByteBuf.writeNullable(this.targetName, FriendlyByteBuf::writeComponent);
		}

		public Optional<ChatType.Bound> resolve(RegistryAccess registryAccess) {
			Registry<ChatType> registry = registryAccess.registryOrThrow(Registries.CHAT_TYPE);
			ChatType chatType = registry.byId(this.chatType);
			return Optional.ofNullable(chatType).map(chatTypex -> new ChatType.Bound(chatTypex, this.name, this.targetName));
		}
	}
}
