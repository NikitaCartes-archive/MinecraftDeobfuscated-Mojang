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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ParticleArgument implements ArgumentType<ParticleOptions> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle{foo:bar}");
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_PARTICLE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("particle.notFound", object)
	);
	public static final DynamicCommandExceptionType ERROR_INVALID_OPTIONS = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("particle.invalidOptions", object)
	);
	private final HolderLookup.Provider registries;

	public ParticleArgument(CommandBuildContext commandBuildContext) {
		this.registries = commandBuildContext;
	}

	public static ParticleArgument particle(CommandBuildContext commandBuildContext) {
		return new ParticleArgument(commandBuildContext);
	}

	public static ParticleOptions getParticle(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, ParticleOptions.class);
	}

	public ParticleOptions parse(StringReader stringReader) throws CommandSyntaxException {
		return readParticle(stringReader, this.registries);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static ParticleOptions readParticle(StringReader stringReader, HolderLookup.Provider provider) throws CommandSyntaxException {
		ParticleType<?> particleType = readParticleType(stringReader, provider.lookupOrThrow(Registries.PARTICLE_TYPE));
		return readParticle(stringReader, (ParticleType<ParticleOptions>)particleType, provider);
	}

	private static ParticleType<?> readParticleType(StringReader stringReader, HolderLookup<ParticleType<?>> holderLookup) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
		ResourceKey<ParticleType<?>> resourceKey = ResourceKey.create(Registries.PARTICLE_TYPE, resourceLocation);
		return (ParticleType<?>)((Holder.Reference)holderLookup.get(resourceKey)
				.orElseThrow(() -> ERROR_UNKNOWN_PARTICLE.createWithContext(stringReader, resourceLocation)))
			.value();
	}

	private static <T extends ParticleOptions> T readParticle(StringReader stringReader, ParticleType<T> particleType, HolderLookup.Provider provider) throws CommandSyntaxException {
		CompoundTag compoundTag;
		if (stringReader.canRead() && stringReader.peek() == '{') {
			compoundTag = new TagParser(stringReader).readStruct();
		} else {
			compoundTag = new CompoundTag();
		}

		return particleType.codec().codec().parse(provider.createSerializationContext(NbtOps.INSTANCE), compoundTag).getOrThrow(ERROR_INVALID_OPTIONS::create);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		HolderLookup.RegistryLookup<ParticleType<?>> registryLookup = this.registries.lookupOrThrow(Registries.PARTICLE_TYPE);
		return SharedSuggestionProvider.suggestResource(registryLookup.listElementIds().map(ResourceKey::location), suggestionsBuilder);
	}
}
