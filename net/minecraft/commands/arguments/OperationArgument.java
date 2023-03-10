/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.Score;

public class OperationArgument
implements ArgumentType<Operation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
    private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION = new SimpleCommandExceptionType(Component.translatable("arguments.operation.invalid"));
    private static final SimpleCommandExceptionType ERROR_DIVIDE_BY_ZERO = new SimpleCommandExceptionType(Component.translatable("arguments.operation.div0"));

    public static OperationArgument operation() {
        return new OperationArgument();
    }

    public static Operation getOperation(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, Operation.class);
    }

    @Override
    public Operation parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead()) {
            int i = stringReader.getCursor();
            while (stringReader.canRead() && stringReader.peek() != ' ') {
                stringReader.skip();
            }
            return OperationArgument.getOperation(stringReader.getString().substring(i, stringReader.getCursor()));
        }
        throw ERROR_INVALID_OPERATION.create();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.suggest(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, suggestionsBuilder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static Operation getOperation(String string) throws CommandSyntaxException {
        if (string.equals("><")) {
            return (score, score2) -> {
                int i = score.getScore();
                score.setScore(score2.getScore());
                score2.setScore(i);
            };
        }
        return OperationArgument.getSimpleOperation(string);
    }

    private static SimpleOperation getSimpleOperation(String string) throws CommandSyntaxException {
        switch (string) {
            case "=": {
                return (i, j) -> j;
            }
            case "+=": {
                return (i, j) -> i + j;
            }
            case "-=": {
                return (i, j) -> i - j;
            }
            case "*=": {
                return (i, j) -> i * j;
            }
            case "/=": {
                return (i, j) -> {
                    if (j == 0) {
                        throw ERROR_DIVIDE_BY_ZERO.create();
                    }
                    return Mth.floorDiv(i, j);
                };
            }
            case "%=": {
                return (i, j) -> {
                    if (j == 0) {
                        throw ERROR_DIVIDE_BY_ZERO.create();
                    }
                    return Mth.positiveModulo(i, j);
                };
            }
            case "<": {
                return Math::min;
            }
            case ">": {
                return Math::max;
            }
        }
        throw ERROR_INVALID_OPERATION.create();
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    @FunctionalInterface
    public static interface Operation {
        public void apply(Score var1, Score var2) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface SimpleOperation
    extends Operation {
        public int apply(int var1, int var2) throws CommandSyntaxException;

        @Override
        default public void apply(Score score, Score score2) throws CommandSyntaxException {
            score.setScore(this.apply(score.getScore(), score2.getScore()));
        }
    }
}

