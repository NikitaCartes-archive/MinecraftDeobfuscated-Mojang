/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.SignedArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

public record ArgumentSignatures(List<Entry> entries) {
    public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
    private static final int MAX_ARGUMENT_COUNT = 8;
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

    public ArgumentSignatures(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), Entry::new));
    }

    public MessageSignature get(String string) {
        for (Entry entry : this.entries) {
            if (!entry.name.equals(string)) continue;
            return entry.signature;
        }
        return MessageSignature.EMPTY;
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.entries, (friendlyByteBuf, entry) -> entry.write((FriendlyByteBuf)friendlyByteBuf));
    }

    public static ArgumentSignatures signCommand(CommandContextBuilder<?> commandContextBuilder, Signer signer) {
        List<Entry> list = ArgumentSignatures.collectLastChildPlainSignableComponents(commandContextBuilder).stream().map(pair -> {
            MessageSignature messageSignature = signer.sign((String)pair.getFirst(), (Component)pair.getSecond());
            return new Entry((String)pair.getFirst(), messageSignature);
        }).toList();
        return new ArgumentSignatures(list);
    }

    private static List<Pair<String, Component>> collectLastChildPlainSignableComponents(CommandContextBuilder<?> commandContextBuilder) {
        CommandContextBuilder<?> commandContextBuilder2 = commandContextBuilder.getLastChild();
        ArrayList<Pair<String, Component>> list = new ArrayList<Pair<String, Component>>();
        for (ParsedCommandNode<?> parsedCommandNode : commandContextBuilder2.getNodes()) {
            ArgumentCommandNode argumentCommandNode;
            CommandNode<?> commandNode = parsedCommandNode.getNode();
            if (!(commandNode instanceof ArgumentCommandNode) || !((commandNode = (argumentCommandNode = (ArgumentCommandNode)commandNode).getType()) instanceof SignedArgument)) continue;
            SignedArgument signedArgument = (SignedArgument)((Object)commandNode);
            ParsedArgument<?, ?> parsedArgument = commandContextBuilder2.getArguments().get(argumentCommandNode.getName());
            if (parsedArgument == null) continue;
            Component component = ArgumentSignatures.getPlainComponentUnchecked(signedArgument, parsedArgument);
            list.add(Pair.of(argumentCommandNode.getName(), component));
        }
        return list;
    }

    private static <T> Component getPlainComponentUnchecked(SignedArgument<T> signedArgument, ParsedArgument<?, ?> parsedArgument) {
        return signedArgument.getPlainSignableComponent(parsedArgument.getResult());
    }

    public record Entry(String name, MessageSignature signature) {
        public Entry(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readUtf(16), new MessageSignature(friendlyByteBuf));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.name, 16);
            this.signature.write(friendlyByteBuf);
        }
    }

    @FunctionalInterface
    public static interface Signer {
        public MessageSignature sign(String var1, Component var2);
    }
}

