package net.minecraft.commands.arguments;

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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionTypeArgument implements ArgumentType<ResourceLocation> {
	private static final Collection<String> EXAMPLES = (Collection<String>)Stream.of(DimensionType.OVERWORLD_LOCATION, DimensionType.NETHER_LOCATION)
		.map(resourceKey -> resourceKey.location().toString())
		.collect(Collectors.toList());
	private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("argument.dimension.invalid", object)
	);

	public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
		return ResourceLocation.read(stringReader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return commandContext.getSource() instanceof SharedSuggestionProvider
			? SharedSuggestionProvider.suggestResource(
				((SharedSuggestionProvider)commandContext.getSource()).registryAccess().dimensionTypes().keySet().stream(), suggestionsBuilder
			)
			: Suggestions.empty();
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static DimensionTypeArgument dimension() {
		return new DimensionTypeArgument();
	}

	public static ResourceKey<DimensionType> getDimension(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		ResourceLocation resourceLocation = commandContext.getArgument(string, ResourceLocation.class);
		ResourceKey<DimensionType> resourceKey = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, resourceLocation);
		if (!commandContext.getSource().getServer().registryAccess().dimensionTypes().containsKey(resourceKey)) {
			throw ERROR_INVALID_VALUE.create(resourceLocation);
		} else {
			return resourceKey;
		}
	}
}
