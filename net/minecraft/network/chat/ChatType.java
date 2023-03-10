/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
    public static final Codec<ChatType> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(((MapCodec)ChatTypeDecoration.CODEC.fieldOf("chat")).forGetter(ChatType::chat), ((MapCodec)ChatTypeDecoration.CODEC.fieldOf("narration")).forGetter(ChatType::narration)).apply((Applicative<ChatType, ?>)instance, ChatType::new));
    public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
    public static final ResourceKey<ChatType> CHAT = ChatType.create("chat");
    public static final ResourceKey<ChatType> SAY_COMMAND = ChatType.create("say_command");
    public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = ChatType.create("msg_command_incoming");
    public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = ChatType.create("msg_command_outgoing");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_INCOMING = ChatType.create("team_msg_command_incoming");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = ChatType.create("team_msg_command_outgoing");
    public static final ResourceKey<ChatType> EMOTE_COMMAND = ChatType.create("emote_command");

    private static ResourceKey<ChatType> create(String string) {
        return ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation(string));
    }

    public static void bootstrap(BootstapContext<ChatType> bootstapContext) {
        bootstapContext.register(CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstapContext.register(SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstapContext.register(MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstapContext.register(MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstapContext.register(TEAM_MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstapContext.register(TEAM_MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.sent"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstapContext.register(EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
    }

    public static Bound bind(ResourceKey<ChatType> resourceKey, Entity entity) {
        return ChatType.bind(resourceKey, entity.level.registryAccess(), entity.getDisplayName());
    }

    public static Bound bind(ResourceKey<ChatType> resourceKey, CommandSourceStack commandSourceStack) {
        return ChatType.bind(resourceKey, commandSourceStack.registryAccess(), commandSourceStack.getDisplayName());
    }

    public static Bound bind(ResourceKey<ChatType> resourceKey, RegistryAccess registryAccess, Component component) {
        Registry<ChatType> registry = registryAccess.registryOrThrow(Registries.CHAT_TYPE);
        return registry.getOrThrow(resourceKey).bind(component);
    }

    public Bound bind(Component component) {
        return new Bound(this, component);
    }

    public record Bound(ChatType chatType, Component name, @Nullable Component targetName) {
        Bound(ChatType chatType, Component component) {
            this(chatType, component, null);
        }

        public Component decorate(Component component) {
            return this.chatType.chat().decorate(component, this);
        }

        public Component decorateNarration(Component component) {
            return this.chatType.narration().decorate(component, this);
        }

        public Bound withTargetName(Component component) {
            return new Bound(this.chatType, this.name, component);
        }

        public BoundNetwork toNetwork(RegistryAccess registryAccess) {
            Registry<ChatType> registry = registryAccess.registryOrThrow(Registries.CHAT_TYPE);
            return new BoundNetwork(registry.getId(this.chatType), this.name, this.targetName);
        }

        @Nullable
        public Component targetName() {
            return this.targetName;
        }
    }

    public record BoundNetwork(int chatType, Component name, @Nullable Component targetName) {
        public BoundNetwork(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readComponent(), (Component)friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeVarInt(this.chatType);
            friendlyByteBuf.writeComponent(this.name);
            friendlyByteBuf.writeNullable(this.targetName, FriendlyByteBuf::writeComponent);
        }

        public Optional<Bound> resolve(RegistryAccess registryAccess) {
            Registry<ChatType> registry = registryAccess.registryOrThrow(Registries.CHAT_TYPE);
            ChatType chatType2 = (ChatType)registry.byId(this.chatType);
            return Optional.ofNullable(chatType2).map(chatType -> new Bound((ChatType)chatType, this.name, this.targetName));
        }

        @Nullable
        public Component targetName() {
            return this.targetName;
        }
    }
}

