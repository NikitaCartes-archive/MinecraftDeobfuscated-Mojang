package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ParticleArgument implements ArgumentType<ParticleOptions> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle with options");
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_PARTICLE = new DynamicCommandExceptionType(
		object -> Component.translatable("particle.notFound", object)
	);
	private final HolderLookup<ParticleType<?>> particles;

	public ParticleArgument(CommandBuildContext commandBuildContext) {
		this.particles = commandBuildContext.holderLookup(Registries.PARTICLE_TYPE);
	}

	public static ParticleArgument particle(CommandBuildContext commandBuildContext) {
		return new ParticleArgument(commandBuildContext);
	}

	public static ParticleOptions getParticle(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, ParticleOptions.class);
	}

	public ParticleOptions parse(StringReader stringReader) throws CommandSyntaxException {
		return readParticle(stringReader, this.particles);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static ParticleOptions readParticle(StringReader stringReader, HolderLookup<ParticleType<?>> holderLookup) throws CommandSyntaxException {
		ParticleType<?> particleType = readParticleType(stringReader, holderLookup);
		return readParticle(stringReader, (ParticleType<ParticleOptions>)particleType);
	}

	private static ParticleType<?> readParticleType(StringReader stringReader, HolderLookup<ParticleType<?>> holderLookup) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
		ResourceKey<ParticleType<?>> resourceKey = ResourceKey.create(Registries.PARTICLE_TYPE, resourceLocation);
		return (ParticleType<?>)((Holder.Reference)holderLookup.get(resourceKey).orElseThrow(() -> ERROR_UNKNOWN_PARTICLE.create(resourceLocation))).value();
	}

	private static <T extends ParticleOptions> T readParticle(StringReader stringReader, ParticleType<T> particleType) throws CommandSyntaxException {
		return particleType.getDeserializer().fromCommand(particleType, stringReader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggestResource(this.particles.listElementIds().map(ResourceKey::location), suggestionsBuilder);
	}
}
