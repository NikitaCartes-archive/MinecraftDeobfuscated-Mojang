package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagArgument<T> implements ArgumentType<ResourceOrTagArgument.Result<T>> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
	private static final Dynamic2CommandExceptionType ERROR_UNKNOWN_TAG = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatable("argument.resource_tag.not_found", object, object2)
	);
	private static final Dynamic3CommandExceptionType ERROR_INVALID_TAG_TYPE = new Dynamic3CommandExceptionType(
		(object, object2, object3) -> Component.translatable("argument.resource_tag.invalid_type", object, object2, object3)
	);
	private final HolderLookup<T> registryLookup;
	final ResourceKey<? extends Registry<T>> registryKey;

	public ResourceOrTagArgument(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey) {
		this.registryKey = resourceKey;
		this.registryLookup = commandBuildContext.holderLookup(resourceKey);
	}

	public static <T> ResourceOrTagArgument<T> resourceOrTag(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey) {
		return new ResourceOrTagArgument<>(commandBuildContext, resourceKey);
	}

	public static <T> ResourceOrTagArgument.Result<T> getResourceOrTag(
		CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey
	) throws CommandSyntaxException {
		ResourceOrTagArgument.Result<?> result = commandContext.getArgument(string, ResourceOrTagArgument.Result.class);
		Optional<ResourceOrTagArgument.Result<T>> optional = result.cast(resourceKey);
		return (ResourceOrTagArgument.Result<T>)optional.orElseThrow(() -> result.unwrap().map(reference -> {
				ResourceKey<?> resourceKey2 = reference.key();
				return ResourceArgument.ERROR_INVALID_RESOURCE_TYPE.create(resourceKey2.location(), resourceKey2.registry(), resourceKey.location());
			}, named -> {
				TagKey<?> tagKey = named.key();
				return ERROR_INVALID_TAG_TYPE.create(tagKey.location(), tagKey.registry(), resourceKey.location());
			}));
	}

	public ResourceOrTagArgument.Result<T> parse(StringReader stringReader) throws CommandSyntaxException {
		if (stringReader.canRead() && stringReader.peek() == '#') {
			int i = stringReader.getCursor();

			try {
				stringReader.skip();
				ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
				TagKey<T> tagKey = TagKey.create(this.registryKey, resourceLocation);
				HolderSet.Named<T> named = (HolderSet.Named<T>)this.registryLookup
					.get(tagKey)
					.orElseThrow(() -> ERROR_UNKNOWN_TAG.create(resourceLocation, this.registryKey.location()));
				return new ResourceOrTagArgument.TagResult<>(named);
			} catch (CommandSyntaxException var6) {
				stringReader.setCursor(i);
				throw var6;
			}
		} else {
			ResourceLocation resourceLocation2 = ResourceLocation.read(stringReader);
			ResourceKey<T> resourceKey = ResourceKey.create(this.registryKey, resourceLocation2);
			Holder.Reference<T> reference = (Holder.Reference<T>)this.registryLookup
				.get(resourceKey)
				.orElseThrow(() -> ResourceArgument.ERROR_UNKNOWN_RESOURCE.create(resourceLocation2, this.registryKey.location()));
			return new ResourceOrTagArgument.ResourceResult<>(reference);
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		SharedSuggestionProvider.suggestResource(this.registryLookup.listTagIds().map(TagKey::location), suggestionsBuilder, "#");
		return SharedSuggestionProvider.suggestResource(this.registryLookup.listElementIds().map(ResourceKey::location), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagArgument<T>, ResourceOrTagArgument.Info<T>.Template> {
		public void serializeToNetwork(ResourceOrTagArgument.Info<T>.Template template, FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeResourceLocation(template.registryKey.location());
		}

		public ResourceOrTagArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			return new ResourceOrTagArgument.Info.Template(ResourceKey.createRegistryKey(resourceLocation));
		}

		public void serializeToJson(ResourceOrTagArgument.Info<T>.Template template, JsonObject jsonObject) {
			jsonObject.addProperty("registry", template.registryKey.location().toString());
		}

		public ResourceOrTagArgument.Info<T>.Template unpack(ResourceOrTagArgument<T> resourceOrTagArgument) {
			return new ResourceOrTagArgument.Info.Template(resourceOrTagArgument.registryKey);
		}

		public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagArgument<T>> {
			final ResourceKey<? extends Registry<T>> registryKey;

			Template(ResourceKey<? extends Registry<T>> resourceKey) {
				this.registryKey = resourceKey;
			}

			public ResourceOrTagArgument<T> instantiate(CommandBuildContext commandBuildContext) {
				return new ResourceOrTagArgument<>(commandBuildContext, this.registryKey);
			}

			@Override
			public ArgumentTypeInfo<ResourceOrTagArgument<T>, ?> type() {
				return Info.this;
			}
		}
	}

	static record ResourceResult<T>(Holder.Reference<T> value) implements ResourceOrTagArgument.Result<T> {
		@Override
		public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap() {
			return Either.left(this.value);
		}

		@Override
		public <E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
			return this.value.key().isFor(resourceKey) ? Optional.of(this) : Optional.empty();
		}

		public boolean test(Holder<T> holder) {
			return holder.equals(this.value);
		}

		@Override
		public String asPrintable() {
			return this.value.key().location().toString();
		}
	}

	public interface Result<T> extends Predicate<Holder<T>> {
		Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap();

		<E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey);

		String asPrintable();
	}

	static record TagResult<T>(HolderSet.Named<T> tag) implements ResourceOrTagArgument.Result<T> {
		@Override
		public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap() {
			return Either.right(this.tag);
		}

		@Override
		public <E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
			return this.tag.key().isFor(resourceKey) ? Optional.of(this) : Optional.empty();
		}

		public boolean test(Holder<T> holder) {
			return this.tag.contains(holder);
		}

		@Override
		public String asPrintable() {
			return "#" + this.tag.key().location();
		}
	}
}
