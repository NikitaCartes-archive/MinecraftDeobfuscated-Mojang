/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public record ChatType(Optional<TextDisplay> chat, Optional<TextDisplay> overlay, Optional<Narration> narration) {
    public static final Codec<ChatType> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(TextDisplay.CODEC.optionalFieldOf("chat").forGetter(ChatType::chat), TextDisplay.CODEC.optionalFieldOf("overlay").forGetter(ChatType::overlay), Narration.CODEC.optionalFieldOf("narration").forGetter(ChatType::narration)).apply((Applicative<ChatType, ?>)instance, ChatType::new));
    public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
    public static final ResourceKey<ChatType> CHAT = ChatType.create("chat");
    public static final ResourceKey<ChatType> SYSTEM = ChatType.create("system");
    public static final ResourceKey<ChatType> GAME_INFO = ChatType.create("game_info");
    public static final ResourceKey<ChatType> SAY_COMMAND = ChatType.create("say_command");
    public static final ResourceKey<ChatType> MSG_COMMAND = ChatType.create("msg_command");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND = ChatType.create("team_msg_command");
    public static final ResourceKey<ChatType> EMOTE_COMMAND = ChatType.create("emote_command");
    public static final ResourceKey<ChatType> TELLRAW_COMMAND = ChatType.create("tellraw_command");

    private static ResourceKey<ChatType> create(String string) {
        return ResourceKey.create(Registry.CHAT_TYPE_REGISTRY, new ResourceLocation(string));
    }

    public static Holder<ChatType> bootstrap(Registry<ChatType> registry) {
        BuiltinRegistries.register(registry, CHAT, new ChatType(Optional.of(TextDisplay.decorated(DEFAULT_CHAT_DECORATION)), Optional.empty(), Optional.of(Narration.decorated(ChatTypeDecoration.withSender("chat.type.text.narrate"), Narration.Priority.CHAT))));
        BuiltinRegistries.register(registry, SYSTEM, new ChatType(Optional.of(TextDisplay.undecorated()), Optional.empty(), Optional.of(Narration.undecorated(Narration.Priority.SYSTEM))));
        BuiltinRegistries.register(registry, GAME_INFO, new ChatType(Optional.empty(), Optional.of(TextDisplay.undecorated()), Optional.of(Narration.undecorated(Narration.Priority.SYSTEM))));
        BuiltinRegistries.register(registry, SAY_COMMAND, new ChatType(Optional.of(TextDisplay.decorated(ChatTypeDecoration.withSender("chat.type.announcement"))), Optional.empty(), Optional.of(Narration.decorated(ChatTypeDecoration.withSender("chat.type.text.narrate"), Narration.Priority.CHAT))));
        BuiltinRegistries.register(registry, MSG_COMMAND, new ChatType(Optional.of(TextDisplay.decorated(ChatTypeDecoration.directMessage("commands.message.display.incoming"))), Optional.empty(), Optional.of(Narration.decorated(ChatTypeDecoration.withSender("chat.type.text.narrate"), Narration.Priority.CHAT))));
        BuiltinRegistries.register(registry, TEAM_MSG_COMMAND, new ChatType(Optional.of(TextDisplay.decorated(ChatTypeDecoration.teamMessage("chat.type.team.text"))), Optional.empty(), Optional.of(Narration.decorated(ChatTypeDecoration.withSender("chat.type.text.narrate"), Narration.Priority.CHAT))));
        BuiltinRegistries.register(registry, EMOTE_COMMAND, new ChatType(Optional.of(TextDisplay.decorated(ChatTypeDecoration.withSender("chat.type.emote"))), Optional.empty(), Optional.of(Narration.decorated(ChatTypeDecoration.withSender("chat.type.emote"), Narration.Priority.CHAT))));
        return BuiltinRegistries.register(registry, TELLRAW_COMMAND, new ChatType(Optional.of(TextDisplay.undecorated()), Optional.empty(), Optional.of(Narration.undecorated(Narration.Priority.CHAT))));
    }

    public record TextDisplay(Optional<ChatTypeDecoration> decoration) {
        public static final Codec<TextDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(ChatTypeDecoration.CODEC.optionalFieldOf("decoration").forGetter(TextDisplay::decoration)).apply((Applicative<TextDisplay, ?>)instance, TextDisplay::new));

        public static TextDisplay undecorated() {
            return new TextDisplay(Optional.empty());
        }

        public static TextDisplay decorated(ChatTypeDecoration chatTypeDecoration) {
            return new TextDisplay(Optional.of(chatTypeDecoration));
        }

        public Component decorate(Component component, @Nullable ChatSender chatSender) {
            return this.decoration.map(chatTypeDecoration -> chatTypeDecoration.decorate(component, chatSender)).orElse(component);
        }
    }

    public record Narration(Optional<ChatTypeDecoration> decoration, Priority priority) {
        public static final Codec<Narration> CODEC = RecordCodecBuilder.create(instance -> instance.group(ChatTypeDecoration.CODEC.optionalFieldOf("decoration").forGetter(Narration::decoration), ((MapCodec)Priority.CODEC.fieldOf("priority")).forGetter(Narration::priority)).apply((Applicative<Narration, ?>)instance, Narration::new));

        public static Narration undecorated(Priority priority) {
            return new Narration(Optional.empty(), priority);
        }

        public static Narration decorated(ChatTypeDecoration chatTypeDecoration, Priority priority) {
            return new Narration(Optional.of(chatTypeDecoration), priority);
        }

        public Component decorate(Component component, @Nullable ChatSender chatSender) {
            return this.decoration.map(chatTypeDecoration -> chatTypeDecoration.decorate(component, chatSender)).orElse(component);
        }

        public static enum Priority implements StringRepresentable
        {
            CHAT("chat", false),
            SYSTEM("system", true);

            public static final Codec<Priority> CODEC;
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

            static {
                CODEC = StringRepresentable.fromEnum(Priority::values);
            }
        }
    }
}

