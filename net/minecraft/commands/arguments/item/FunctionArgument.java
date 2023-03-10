/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FunctionArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(object -> Component.translatable("arguments.function.tag.unknown", object));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType(object -> Component.translatable("arguments.function.unknown", object));

    public static FunctionArgument functions() {
        return new FunctionArgument();
    }

    @Override
    public Result parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            stringReader.skip();
            final ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
            return new Result(){

                @Override
                public Collection<CommandFunction> create(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                    return FunctionArgument.getFunctionTag(commandContext, resourceLocation);
                }

                @Override
                public Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                    return Pair.of(resourceLocation, Either.right(FunctionArgument.getFunctionTag(commandContext, resourceLocation)));
                }
            };
        }
        final ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
        return new Result(){

            @Override
            public Collection<CommandFunction> create(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                return Collections.singleton(FunctionArgument.getFunction(commandContext, resourceLocation));
            }

            @Override
            public Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                return Pair.of(resourceLocation, Either.left(FunctionArgument.getFunction(commandContext, resourceLocation)));
            }
        };
    }

    static CommandFunction getFunction(CommandContext<CommandSourceStack> commandContext, ResourceLocation resourceLocation) throws CommandSyntaxException {
        return commandContext.getSource().getServer().getFunctions().get(resourceLocation).orElseThrow(() -> ERROR_UNKNOWN_FUNCTION.create(resourceLocation.toString()));
    }

    static Collection<CommandFunction> getFunctionTag(CommandContext<CommandSourceStack> commandContext, ResourceLocation resourceLocation) throws CommandSyntaxException {
        Collection<CommandFunction> collection = commandContext.getSource().getServer().getFunctions().getTag(resourceLocation);
        if (collection == null) {
            throw ERROR_UNKNOWN_TAG.create(resourceLocation.toString());
        }
        return collection;
    }

    public static Collection<CommandFunction> getFunctions(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, Result.class).create(commandContext);
    }

    public static Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> getFunctionOrTag(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, Result.class).unwrap(commandContext);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static interface Result {
        public Collection<CommandFunction> create(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }
}

