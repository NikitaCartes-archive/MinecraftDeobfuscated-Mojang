/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public interface RangeArgument<T extends MinMaxBounds<?>>
extends ArgumentType<T> {
    public static Ints intRange() {
        return new Ints();
    }

    public static abstract class Serializer<T extends RangeArgument<?>>
    implements ArgumentSerializer<T> {
        @Override
        public void serializeToNetwork(T rangeArgument, FriendlyByteBuf friendlyByteBuf) {
        }

        @Override
        public void serializeToJson(T rangeArgument, JsonObject jsonObject) {
        }
    }

    public static class Floats
    implements RangeArgument<MinMaxBounds.Floats> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5.2", "0", "-5.4", "-100.76..", "..100");

        @Override
        public MinMaxBounds.Floats parse(StringReader stringReader) throws CommandSyntaxException {
            return MinMaxBounds.Floats.fromReader(stringReader);
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        @Override
        public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
            return this.parse(stringReader);
        }

        public static class Serializer
        extends net.minecraft.commands.arguments.RangeArgument$Serializer<Floats> {
            @Override
            public Floats deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
                return new Floats();
            }

            @Override
            public /* synthetic */ ArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
                return this.deserializeFromNetwork(friendlyByteBuf);
            }
        }
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

        public static class Serializer
        extends net.minecraft.commands.arguments.RangeArgument$Serializer<Ints> {
            @Override
            public Ints deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
                return new Ints();
            }

            @Override
            public /* synthetic */ ArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
                return this.deserializeFromNetwork(friendlyByteBuf);
            }
        }
    }
}

