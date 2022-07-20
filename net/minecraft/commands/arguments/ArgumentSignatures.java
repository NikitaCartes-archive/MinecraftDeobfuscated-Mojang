/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.PreviewedArgument;
import net.minecraft.commands.arguments.SignedArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PreviewableCommand;

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

    public static boolean hasSignableArguments(PreviewableCommand<?> previewableCommand) {
        return previewableCommand.arguments().stream().anyMatch(argument -> argument.previewType() instanceof SignedArgument);
    }

    public static ArgumentSignatures signCommand(PreviewableCommand<?> previewableCommand, Signer signer) {
        List<Entry> list = ArgumentSignatures.collectPlainSignableArguments(previewableCommand).stream().map(pair -> {
            MessageSignature messageSignature = signer.sign((String)pair.getFirst(), (String)pair.getSecond());
            return new Entry((String)pair.getFirst(), messageSignature);
        }).toList();
        return new ArgumentSignatures(list);
    }

    public static List<Pair<String, String>> collectPlainSignableArguments(PreviewableCommand<?> previewableCommand) {
        ArrayList<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        for (PreviewableCommand.Argument<?> argument : previewableCommand.arguments()) {
            PreviewedArgument<?> previewedArgument = argument.previewType();
            if (!(previewedArgument instanceof SignedArgument)) continue;
            SignedArgument signedArgument = (SignedArgument)previewedArgument;
            String string = ArgumentSignatures.getSignableText(signedArgument, argument.parsedValue());
            list.add(Pair.of(argument.name(), string));
        }
        return list;
    }

    private static <T> String getSignableText(SignedArgument<T> signedArgument, ParsedArgument<?, ?> parsedArgument) {
        return signedArgument.getSignableText(parsedArgument.getResult());
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
        public MessageSignature sign(String var1, String var2);
    }
}

