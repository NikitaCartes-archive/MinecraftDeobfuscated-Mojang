package net.minecraft.commands.arguments;

import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionTypeArgument implements ArgumentType<DimensionType> {
	private static final Collection<String> EXAMPLES = (Collection<String>)Stream.of(DimensionType.OVERWORLD, DimensionType.NETHER)
		.map(dimensionType -> DimensionType.getName(dimensionType).toString())
		.collect(Collectors.toList());
	public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("argument.dimension.invalid", object)
	);

	public DimensionType parse(StringReader stringReader) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
		return (DimensionType)Registry.DIMENSION_TYPE.getOptional(resourceLocation).orElseThrow(() -> ERROR_INVALID_VALUE.create(resourceLocation));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggestResource(Streams.stream(DimensionType.getAllTypes()).map(DimensionType::getName), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static DimensionTypeArgument dimension() {
		return new DimensionTypeArgument();
	}

	public static DimensionType getDimension(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, DimensionType.class);
	}
}
