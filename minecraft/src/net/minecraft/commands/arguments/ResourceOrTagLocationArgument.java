package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagLocationArgument<T> implements ArgumentType<ResourceOrTagLocationArgument.Result<T>> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
	final ResourceKey<? extends Registry<T>> registryKey;

	public ResourceOrTagLocationArgument(ResourceKey<? extends Registry<T>> resourceKey) {
		this.registryKey = resourceKey;
	}

	public static <T> ResourceOrTagLocationArgument<T> resourceOrTag(ResourceKey<? extends Registry<T>> resourceKey) {
		return new ResourceOrTagLocationArgument<>(resourceKey);
	}

	public static <T> ResourceOrTagLocationArgument.Result<T> getRegistryType(
		CommandContext<CommandSourceStack> commandContext,
		String string,
		ResourceKey<Registry<T>> resourceKey,
		DynamicCommandExceptionType dynamicCommandExceptionType
	) throws CommandSyntaxException {
		ResourceOrTagLocationArgument.Result<?> result = commandContext.getArgument(string, ResourceOrTagLocationArgument.Result.class);
		Optional<ResourceOrTagLocationArgument.Result<T>> optional = result.cast(resourceKey);
		return (ResourceOrTagLocationArgument.Result<T>)optional.orElseThrow(() -> dynamicCommandExceptionType.create(result));
	}

	public ResourceOrTagLocationArgument.Result<T> parse(StringReader stringReader) throws CommandSyntaxException {
		if (stringReader.canRead() && stringReader.peek() == '#') {
			int i = stringReader.getCursor();

			try {
				stringReader.skip();
				ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
				return new ResourceOrTagLocationArgument.TagResult<>(TagKey.create(this.registryKey, resourceLocation));
			} catch (CommandSyntaxException var4) {
				stringReader.setCursor(i);
				throw var4;
			}
		} else {
			ResourceLocation resourceLocation2 = ResourceLocation.read(stringReader);
			return new ResourceOrTagLocationArgument.ResourceResult<>(ResourceKey.create(this.registryKey, resourceLocation2));
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return commandContext.getSource() instanceof SharedSuggestionProvider sharedSuggestionProvider
			? sharedSuggestionProvider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL, suggestionsBuilder, commandContext)
			: suggestionsBuilder.buildFuture();
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, ResourceOrTagLocationArgument.Info<T>.Template> {
		public void serializeToNetwork(ResourceOrTagLocationArgument.Info<T>.Template template, FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeResourceLocation(template.registryKey.location());
		}

		public ResourceOrTagLocationArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			return new ResourceOrTagLocationArgument.Info.Template(ResourceKey.createRegistryKey(resourceLocation));
		}

		public void serializeToJson(ResourceOrTagLocationArgument.Info<T>.Template template, JsonObject jsonObject) {
			jsonObject.addProperty("registry", template.registryKey.location().toString());
		}

		public ResourceOrTagLocationArgument.Info<T>.Template unpack(ResourceOrTagLocationArgument<T> resourceOrTagLocationArgument) {
			return new ResourceOrTagLocationArgument.Info.Template(resourceOrTagLocationArgument.registryKey);
		}

		public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagLocationArgument<T>> {
			final ResourceKey<? extends Registry<T>> registryKey;

			Template(ResourceKey<? extends Registry<T>> resourceKey) {
				this.registryKey = resourceKey;
			}

			public ResourceOrTagLocationArgument<T> instantiate(CommandBuildContext commandBuildContext) {
				return new ResourceOrTagLocationArgument<>(this.registryKey);
			}

			@Override
			public ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, ?> type() {
				return Info.this;
			}
		}
	}

	static record ResourceResult<T>(ResourceKey<T> key) implements ResourceOrTagLocationArgument.Result<T> {
		@Override
		public Either<ResourceKey<T>, TagKey<T>> unwrap() {
			return Either.left(this.key);
		}

		@Override
		public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
			return this.key.cast(resourceKey).map(ResourceOrTagLocationArgument.ResourceResult::new);
		}

		public boolean test(Holder<T> holder) {
			return holder.is(this.key);
		}

		@Override
		public String asPrintable() {
			return this.key.location().toString();
		}
	}

	public interface Result<T> extends Predicate<Holder<T>> {
		Either<ResourceKey<T>, TagKey<T>> unwrap();

		<E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey);

		String asPrintable();
	}

	static record TagResult<T>(TagKey<T> key) implements ResourceOrTagLocationArgument.Result<T> {
		@Override
		public Either<ResourceKey<T>, TagKey<T>> unwrap() {
			return Either.right(this.key);
		}

		@Override
		public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
			return this.key.cast(resourceKey).map(ResourceOrTagLocationArgument.TagResult::new);
		}

		public boolean test(Holder<T> holder) {
			return holder.is(this.key);
		}

		@Override
		public String asPrintable() {
			return "#" + this.key.location();
		}
	}
}
