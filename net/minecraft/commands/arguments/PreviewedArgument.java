/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface PreviewedArgument<T>
extends ArgumentType<T> {
    @Nullable
    default public CompletableFuture<Component> resolvePreview(CommandSourceStack commandSourceStack, ParsedArgument<CommandSourceStack, ?> parsedArgument) throws CommandSyntaxException {
        if (this.getValueType().isInstance(parsedArgument.getResult())) {
            return this.resolvePreview(commandSourceStack, this.getValueType().cast(parsedArgument.getResult()));
        }
        return null;
    }

    public CompletableFuture<Component> resolvePreview(CommandSourceStack var1, T var2) throws CommandSyntaxException;

    public Class<T> getValueType();
}

