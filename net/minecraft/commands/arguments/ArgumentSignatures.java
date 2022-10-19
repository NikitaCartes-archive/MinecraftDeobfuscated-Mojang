/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignableCommand;
import org.jetbrains.annotations.Nullable;

public record ArgumentSignatures(List<Entry> entries) {
    public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
    private static final int MAX_ARGUMENT_COUNT = 8;
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

    public ArgumentSignatures(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), Entry::new));
    }

    @Nullable
    public MessageSignature get(String string) {
        for (Entry entry : this.entries) {
            if (!entry.name.equals(string)) continue;
            return entry.signature;
        }
        return null;
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.entries, (friendlyByteBuf, entry) -> entry.write((FriendlyByteBuf)friendlyByteBuf));
    }

    public static ArgumentSignatures signCommand(SignableCommand<?> signableCommand, Signer signer) {
        List<Entry> list = signableCommand.arguments().stream().map(argument -> {
            MessageSignature messageSignature = signer.sign(argument.value());
            if (messageSignature != null) {
                return new Entry(argument.name(), messageSignature);
            }
            return null;
        }).filter(Objects::nonNull).toList();
        return new ArgumentSignatures(list);
    }

    public record Entry(String name, MessageSignature signature) {
        public Entry(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readUtf(16), MessageSignature.read(friendlyByteBuf));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.name, 16);
            MessageSignature.write(friendlyByteBuf, this.signature);
        }
    }

    @FunctionalInterface
    public static interface Signer {
        @Nullable
        public MessageSignature sign(String var1);
    }
}

