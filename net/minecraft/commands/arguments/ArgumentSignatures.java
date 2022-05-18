/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.arguments.SignedArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Crypt;
import org.jetbrains.annotations.Nullable;

public record ArgumentSignatures(long salt, Map<String, byte[]> signatures) {
    private static final int MAX_ARGUMENT_COUNT = 8;
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

    public ArgumentSignatures(FriendlyByteBuf friendlyByteBuf2) {
        this(friendlyByteBuf2.readLong(), friendlyByteBuf2.readMap(FriendlyByteBuf.limitValue(HashMap::new, 8), friendlyByteBuf -> friendlyByteBuf.readUtf(16), FriendlyByteBuf::readByteArray));
    }

    public static ArgumentSignatures empty() {
        return new ArgumentSignatures(0L, Map.of());
    }

    @Nullable
    public Crypt.SaltSignaturePair get(String string) {
        byte[] bs = this.signatures.get(string);
        if (bs != null) {
            return new Crypt.SaltSignaturePair(this.salt, bs);
        }
        return null;
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeLong(this.salt);
        friendlyByteBuf2.writeMap(this.signatures, (friendlyByteBuf, string) -> friendlyByteBuf.writeUtf((String)string, 16), FriendlyByteBuf::writeByteArray);
    }

    public static Map<String, Component> collectLastChildPlainSignableComponents(CommandContextBuilder<?> commandContextBuilder) {
        CommandContextBuilder<?> commandContextBuilder2 = commandContextBuilder.getLastChild();
        Object2ObjectArrayMap<String, Component> map = new Object2ObjectArrayMap<String, Component>();
        for (ParsedCommandNode<?> parsedCommandNode : commandContextBuilder2.getNodes()) {
            ArgumentCommandNode argumentCommandNode;
            CommandNode<?> commandNode = parsedCommandNode.getNode();
            if (!(commandNode instanceof ArgumentCommandNode) || !((commandNode = (argumentCommandNode = (ArgumentCommandNode)commandNode).getType()) instanceof SignedArgument)) continue;
            SignedArgument signedArgument = (SignedArgument)((Object)commandNode);
            ParsedArgument<?, ?> parsedArgument = commandContextBuilder2.getArguments().get(argumentCommandNode.getName());
            if (parsedArgument == null) continue;
            map.put(argumentCommandNode.getName(), ArgumentSignatures.getPlainComponentUnchecked(signedArgument, parsedArgument));
        }
        return map;
    }

    private static <T> Component getPlainComponentUnchecked(SignedArgument<T> signedArgument, ParsedArgument<?, ?> parsedArgument) {
        return signedArgument.getPlainSignableComponent(parsedArgument.getResult());
    }
}

