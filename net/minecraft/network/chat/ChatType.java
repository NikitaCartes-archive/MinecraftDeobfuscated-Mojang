/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
    public static final Codec<ChatType> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(((MapCodec)ChatTypeDecoration.CODEC.fieldOf("chat")).forGetter(ChatType::chat), ((MapCodec)ChatTypeDecoration.CODEC.fieldOf("narration")).forGetter(ChatType::narration)).apply((Applicative<ChatType, ?>)instance, ChatType::new));
    public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
    public static final ResourceKey<ChatType> CHAT = ChatType.create("chat");
    public static final ResourceKey<ChatType> SAY_COMMAND = ChatType.create("say_command");
    public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = ChatType.create("msg_command_incoming");
    public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = ChatType.create("msg_command_outgoing");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND = ChatType.create("team_msg_command");
    public static final ResourceKey<ChatType> EMOTE_COMMAND = ChatType.create("emote_command");

    private static ResourceKey<ChatType> create(String string) {
        return ResourceKey.create(Registry.CHAT_TYPE_REGISTRY, new ResourceLocation(string));
    }

    public static Holder<ChatType> bootstrap(Registry<ChatType> registry) {
        BuiltinRegistries.register(registry, CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
        BuiltinRegistries.register(registry, SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        BuiltinRegistries.register(registry, MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        BuiltinRegistries.register(registry, MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        BuiltinRegistries.register(registry, TEAM_MSG_COMMAND, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        return BuiltinRegistries.register(registry, EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
    }
}

