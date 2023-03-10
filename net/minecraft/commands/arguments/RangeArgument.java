/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;

public interface RangeArgument<T extends MinMaxBounds<?>>
extends ArgumentType<T> {
    public static Ints intRange() {
        return new Ints();
    }

    public static Floats floatRange() {
        return new Floats();
    }

    public static class Ints
    implements RangeArgument<MinMaxBounds.Ints> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5", "0", "-5", "-100..", "..100");

        public static MinMaxBounds.Ints getRange(CommandContext<CommandSourceStack> commandContext, String string) {
            return commandContext.getArgument(string, MinMaxBounds.Ints.class);
        }

        @Override
        public MinMaxBounds.Ints parse(StringReader stringReader) throws CommandSyntaxException {
            return MinMaxBounds.Ints.fromReader(stringReader);
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

    public static class Floats
    implements RangeArgument<MinMaxBounds.Doubles> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5.2", "0", "-5.4", "-100.76..", "..100");

        public static MinMaxBounds.Doubles getRange(CommandContext<CommandSourceStack> commandContext, String string) {
            return commandContext.getArgument(string, MinMaxBounds.Doubles.class);
        }

        @Override
        public MinMaxBounds.Doubles parse(StringReader stringReader) throws CommandSyntaxException {
            return MinMaxBounds.Doubles.fromReader(stringReader);
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
}

