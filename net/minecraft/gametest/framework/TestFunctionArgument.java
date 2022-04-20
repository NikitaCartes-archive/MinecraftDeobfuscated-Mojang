/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TestFunctionArgument
implements ArgumentType<TestFunction> {
    private static final Collection<String> EXAMPLES = Arrays.asList("techtests.piston", "techtests");

    @Override
    public TestFunction parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        Optional<TestFunction> optional = GameTestRegistry.findTestFunction(string);
        if (optional.isPresent()) {
            return optional.get();
        }
        MutableComponent message = Component.literal("No such test: " + string);
        throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
    }

    public static TestFunctionArgument testFunctionArgument() {
        return new TestFunctionArgument();
    }

    public static TestFunction getTestFunction(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, TestFunction.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        Stream<String> stream = GameTestRegistry.getAllTestFunctions().stream().map(TestFunction::getTestName);
        return SharedSuggestionProvider.suggest(stream, suggestionsBuilder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

