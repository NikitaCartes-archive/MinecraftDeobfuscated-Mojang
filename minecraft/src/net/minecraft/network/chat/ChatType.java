package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record ChatType(Optional<ChatType.TextDisplay> chat, Optional<ChatType.TextDisplay> overlay, Optional<ChatType.Narration> narration) {
	public static final Codec<ChatType> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ChatType.TextDisplay.CODEC.optionalFieldOf("chat").forGetter(ChatType::chat),
					ChatType.TextDisplay.CODEC.optionalFieldOf("overlay").forGetter(ChatType::overlay),
					ChatType.Narration.CODEC.optionalFieldOf("narration").forGetter(ChatType::narration)
				)
				.apply(instance, ChatType::new)
	);
	public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
	public static final ResourceKey<ChatType> CHAT = create("chat");
	public static final ResourceKey<ChatType> SYSTEM = create("system");
	public static final ResourceKey<ChatType> GAME_INFO = create("game_info");
	public static final ResourceKey<ChatType> SAY_COMMAND = create("say_command");
	public static final ResourceKey<ChatType> MSG_COMMAND = create("msg_command");
	public static final ResourceKey<ChatType> TEAM_MSG_COMMAND = create("team_msg_command");
	public static final ResourceKey<ChatType> EMOTE_COMMAND = create("emote_command");
	public static final ResourceKey<ChatType> TELLRAW_COMMAND = create("tellraw_command");

	private static ResourceKey<ChatType> create(String string) {
		return ResourceKey.create(Registry.CHAT_TYPE_REGISTRY, new ResourceLocation(string));
	}

	public static Holder<ChatType> bootstrap(Registry<ChatType> registry) {
		BuiltinRegistries.register(
			registry,
			CHAT,
			new ChatType(
				Optional.of(ChatType.TextDisplay.decorated(DEFAULT_CHAT_DECORATION)),
				Optional.empty(),
				Optional.of(ChatType.Narration.decorated(ChatTypeDecoration.withSender("chat.type.text.narrate"), ChatType.Narration.Priority.CHAT))
			)
		);
		BuiltinRegistries.register(
			registry,
			SYSTEM,
			new ChatType(
				Optional.of(ChatType.TextDisplay.undecorated()), Optional.empty(), Optional.of(ChatType.Narration.undecorated(ChatType.Narration.Priority.SYSTEM))
			)
		);
		BuiltinRegistries.register(
			registry,
			GAME_INFO,
			new ChatType(
				Optional.empty(), Optional.of(ChatType.TextDisplay.undecorated()), Optional.of(ChatType.Narration.undecorated(ChatType.Narration.Priority.SYSTEM))
			)
		);
		BuiltinRegistries.register(
			registry,
			SAY_COMMAND,
			new ChatType(
				Optional.of(ChatType.TextDisplay.decorated(ChatTypeDecoration.withSender("chat.type.announcement"))),
				Optional.empty(),
				Optional.of(ChatType.Narration.decorated(ChatTypeDecoration.withSender("chat.type.text.narrate"), ChatType.Narration.Priority.CHAT))
			)
		);
		BuiltinRegistries.register(
			registry,
			MSG_COMMAND,
			new ChatType(
				Optional.of(ChatType.TextDisplay.decorated(ChatTypeDecoration.directMessage("commands.message.display.incoming"))),
				Optional.empty(),
				Optional.of(ChatType.Narration.decorated(ChatTypeDecoration.withSender("chat.type.text.narrate"), ChatType.Narration.Priority.CHAT))
			)
		);
		BuiltinRegistries.register(
			registry,
			TEAM_MSG_COMMAND,
			new ChatType(
				Optional.of(ChatType.TextDisplay.decorated(ChatTypeDecoration.teamMessage("chat.type.team.text"))),
				Optional.empty(),
				Optional.of(ChatType.Narration.decorated(ChatTypeDecoration.withSender("chat.type.text.narrate"), ChatType.Narration.Priority.CHAT))
			)
		);
		BuiltinRegistries.register(
			registry,
			EMOTE_COMMAND,
			new ChatType(
				Optional.of(ChatType.TextDisplay.decorated(ChatTypeDecoration.withSender("chat.type.emote"))),
				Optional.empty(),
				Optional.of(ChatType.Narration.decorated(ChatTypeDecoration.withSender("chat.type.emote"), ChatType.Narration.Priority.CHAT))
			)
		);
		return BuiltinRegistries.register(
			registry,
			TELLRAW_COMMAND,
			new ChatType(
				Optional.of(ChatType.TextDisplay.undecorated()), Optional.empty(), Optional.of(ChatType.Narration.undecorated(ChatType.Narration.Priority.CHAT))
			)
		);
	}

	public static record Narration(Optional<ChatTypeDecoration> decoration, ChatType.Narration.Priority priority) {
		public static final Codec<ChatType.Narration> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ChatTypeDecoration.CODEC.optionalFieldOf("decoration").forGetter(ChatType.Narration::decoration),
						ChatType.Narration.Priority.CODEC.fieldOf("priority").forGetter(ChatType.Narration::priority)
					)
					.apply(instance, ChatType.Narration::new)
		);

		public static ChatType.Narration undecorated(ChatType.Narration.Priority priority) {
			return new ChatType.Narration(Optional.empty(), priority);
		}

		public static ChatType.Narration decorated(ChatTypeDecoration chatTypeDecoration, ChatType.Narration.Priority priority) {
			return new ChatType.Narration(Optional.of(chatTypeDecoration), priority);
		}

		public Component decorate(Component component, @Nullable ChatSender chatSender) {
			return (Component)this.decoration.map(chatTypeDecoration -> chatTypeDecoration.decorate(component, chatSender)).orElse(component);
		}

		public static enum Priority implements StringRepresentable {
			CHAT("chat", false),
			SYSTEM("system", true);

			public static final Codec<ChatType.Narration.Priority> CODEC = StringRepresentable.fromEnum(ChatType.Narration.Priority::values);
			private final String name;
			private final boolean interrupts;

			private Priority(String string2, boolean bl) {
				this.name = string2;
				this.interrupts = bl;
			}

			public boolean interrupts() {
				return this.interrupts;
			}

			@Override
			public String getSerializedName() {
				return this.name;
			}
		}
	}

	public static record TextDisplay(Optional<ChatTypeDecoration> decoration) {
		public static final Codec<ChatType.TextDisplay> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(ChatTypeDecoration.CODEC.optionalFieldOf("decoration").forGetter(ChatType.TextDisplay::decoration))
					.apply(instance, ChatType.TextDisplay::new)
		);

		public static ChatType.TextDisplay undecorated() {
			return new ChatType.TextDisplay(Optional.empty());
		}

		public static ChatType.TextDisplay decorated(ChatTypeDecoration chatTypeDecoration) {
			return new ChatType.TextDisplay(Optional.of(chatTypeDecoration));
		}

		public Component decorate(Component component, @Nullable ChatSender chatSender) {
			return (Component)this.decoration.map(chatTypeDecoration -> chatTypeDecoration.decorate(component, chatSender)).orElse(component);
		}
	}
}
