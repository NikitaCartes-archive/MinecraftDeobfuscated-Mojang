/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ChatDecoration;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public record ChatType(Optional<TextDisplay> chat, Optional<TextDisplay> overlay, Optional<Narration> narration) {
    public static final Codec<ChatType> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(TextDisplay.CODEC.optionalFieldOf("chat").forGetter(ChatType::chat), TextDisplay.CODEC.optionalFieldOf("overlay").forGetter(ChatType::overlay), Narration.CODEC.optionalFieldOf("narration").forGetter(ChatType::narration)).apply((Applicative<ChatType, ?>)instance, ChatType::new));
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

    public static ChatType bootstrap(Registry<ChatType> registry) {
        ChatType chatType = Registry.register(registry, CHAT, new ChatType(Optional.of(TextDisplay.decorated(ChatDecoration.withSender("chat.type.text"))), Optional.empty(), Optional.of(Narration.decorated(ChatDecoration.withSender("chat.type.text.narrate"), Narration.Priority.CHAT))));
        Registry.register(registry, SYSTEM, new ChatType(Optional.of(TextDisplay.undecorated()), Optional.empty(), Optional.of(Narration.undecorated(Narration.Priority.SYSTEM))));
        Registry.register(registry, GAME_INFO, new ChatType(Optional.empty(), Optional.of(TextDisplay.undecorated()), Optional.empty()));
        Registry.register(registry, SAY_COMMAND, new ChatType(Optional.of(TextDisplay.decorated(ChatDecoration.withSender("chat.type.announcement"))), Optional.empty(), Optional.of(Narration.decorated(ChatDecoration.withSender("chat.type.text.narrate"), Narration.Priority.CHAT))));
        Registry.register(registry, MSG_COMMAND, new ChatType(Optional.of(TextDisplay.decorated(ChatDecoration.directMessage("commands.message.display.incoming"))), Optional.empty(), Optional.of(Narration.decorated(ChatDecoration.withSender("chat.type.text.narrate"), Narration.Priority.CHAT))));
        Registry.register(registry, TEAM_MSG_COMMAND, new ChatType(Optional.of(TextDisplay.decorated(ChatDecoration.teamMessage("chat.type.team.text"))), Optional.empty(), Optional.of(Narration.decorated(ChatDecoration.withSender("chat.type.text.narrate"), Narration.Priority.CHAT))));
        Registry.register(registry, EMOTE_COMMAND, new ChatType(Optional.of(TextDisplay.decorated(ChatDecoration.withSender("chat.type.emote"))), Optional.empty(), Optional.of(Narration.decorated(ChatDecoration.withSender("chat.type.emote"), Narration.Priority.CHAT))));
        Registry.register(registry, TELLRAW_COMMAND, new ChatType(Optional.of(TextDisplay.undecorated()), Optional.empty(), Optional.of(Narration.undecorated(Narration.Priority.CHAT))));
        return chatType;
    }

    public record TextDisplay(Optional<ChatDecoration> decoration) {
        public static final Codec<TextDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(ChatDecoration.CODEC.optionalFieldOf("decoration").forGetter(TextDisplay::decoration)).apply((Applicative<TextDisplay, ?>)instance, TextDisplay::new));

        public static TextDisplay undecorated() {
            return new TextDisplay(Optional.empty());
        }

        public static TextDisplay decorated(ChatDecoration chatDecoration) {
            return new TextDisplay(Optional.of(chatDecoration));
        }

        public Component decorate(Component component, @Nullable ChatSender chatSender) {
            return this.decoration.map(chatDecoration -> chatDecoration.decorate(component, chatSender)).orElse(component);
        }
    }

    public record Narration(Optional<ChatDecoration> decoration, Priority priority) {
        public static final Codec<Narration> CODEC = RecordCodecBuilder.create(instance -> instance.group(ChatDecoration.CODEC.optionalFieldOf("decoration").forGetter(Narration::decoration), ((MapCodec)Priority.CODEC.fieldOf("priority")).forGetter(Narration::priority)).apply((Applicative<Narration, ?>)instance, Narration::new));

        public static Narration undecorated(Priority priority) {
            return new Narration(Optional.empty(), priority);
        }

        public static Narration decorated(ChatDecoration chatDecoration, Priority priority) {
            return new Narration(Optional.of(chatDecoration), priority);
        }

        public Component decorate(Component component, @Nullable ChatSender chatSender) {
            return this.decoration.map(chatDecoration -> chatDecoration.decorate(component, chatSender)).orElse(component);
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

