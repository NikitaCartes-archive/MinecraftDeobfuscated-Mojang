/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface PreviewedArgument<T>
extends ArgumentType<T> {
    @Nullable
    public static CompletableFuture<Component> resolvePreviewed(ArgumentCommandNode<?, ?> argumentCommandNode, CommandContextBuilder<CommandSourceStack> commandContextBuilder) throws CommandSyntaxException {
        ArgumentType<?> argumentType = argumentCommandNode.getType();
        if (argumentType instanceof PreviewedArgument) {
            PreviewedArgument previewedArgument = (PreviewedArgument)argumentType;
            return previewedArgument.resolvePreview(commandContextBuilder, argumentCommandNode.getName());
        }
        return null;
    }

    public static boolean isPreviewed(CommandNode<?> commandNode) {
        ArgumentCommandNode argumentCommandNode;
        return commandNode instanceof ArgumentCommandNode && (argumentCommandNode = (ArgumentCommandNode)commandNode).getType() instanceof PreviewedArgument;
    }

    @Nullable
    default public CompletableFuture<Component> resolvePreview(CommandContextBuilder<CommandSourceStack> commandContextBuilder, String string) throws CommandSyntaxException {
        ParsedArgument<CommandSourceStack, ?> parsedArgument = commandContextBuilder.getArguments().get(string);
        if (parsedArgument != null && this.getValueType().isInstance(parsedArgument.getResult())) {
            return this.resolvePreview(commandContextBuilder.getSource(), this.getValueType().cast(parsedArgument.getResult()));
        }
        return null;
    }

    public CompletableFuture<Component> resolvePreview(CommandSourceStack var1, T var2) throws CommandSyntaxException;

    public Class<T> getValueType();
}

