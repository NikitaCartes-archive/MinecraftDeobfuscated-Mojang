/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Vec2Argument
implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "0.1 -0.5", "~1 ~-2");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos2d.incomplete", new Object[0]));
    private final boolean centerCorrect;

    public Vec2Argument(boolean bl) {
        this.centerCorrect = bl;
    }

    public static Vec2Argument vec2() {
        return new Vec2Argument(true);
    }

    public static Vec2 getVec2(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Vec3 vec3 = commandContext.getArgument(string, Coordinates.class).getPosition(commandContext.getSource());
        return new Vec2((float)vec3.x, (float)vec3.z);
    }

    @Override
    public Coordinates parse(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        if (!stringReader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
        }
        WorldCoordinate worldCoordinate = WorldCoordinate.parseDouble(stringReader, this.centerCorrect);
        if (!stringReader.canRead() || stringReader.peek() != ' ') {
            stringReader.setCursor(i);
            throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
        }
        stringReader.skip();
        WorldCoordinate worldCoordinate2 = WorldCoordinate.parseDouble(stringReader, this.centerCorrect);
        return new WorldCoordinates(worldCoordinate, new WorldCoordinate(true, 0.0), worldCoordinate2);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        if (commandContext.getSource() instanceof SharedSuggestionProvider) {
            String string = suggestionsBuilder.getRemaining();
            Collection<SharedSuggestionProvider.TextCoordinates> collection = !string.isEmpty() && string.charAt(0) == '^' ? Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL) : ((SharedSuggestionProvider)commandContext.getSource()).getAbsoluteCoordinates();
            return SharedSuggestionProvider.suggest2DCoordinates(string, collection, suggestionsBuilder, Commands.createValidator(this::parse));
        }
        return Suggestions.empty();
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

