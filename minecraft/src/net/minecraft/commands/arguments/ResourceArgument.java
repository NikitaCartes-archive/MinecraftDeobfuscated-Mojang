package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceArgument<T> implements ArgumentType<Holder.Reference<T>> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
	private static final DynamicCommandExceptionType ERROR_NOT_SUMMONABLE_ENTITY = new DynamicCommandExceptionType(
		object -> Component.translatable("entity.not_summonable", object)
	);
	public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_RESOURCE = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatable("argument.resource.not_found", object, object2)
	);
	public static final Dynamic3CommandExceptionType ERROR_INVALID_RESOURCE_TYPE = new Dynamic3CommandExceptionType(
		(object, object2, object3) -> Component.translatable("argument.resource.invalid_type", object, object2, object3)
	);
	final ResourceKey<? extends Registry<T>> registryKey;
	private final HolderLookup<T> registryLookup;

	public ResourceArgument(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey) {
		this.registryKey = resourceKey;
		this.registryLookup = commandBuildContext.holderLookup(resourceKey);
	}

	public static <T> ResourceArgument<T> resource(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey) {
		return new ResourceArgument<>(commandBuildContext, resourceKey);
	}

	public static <T> Holder.Reference<T> getResource(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey) throws CommandSyntaxException {
		Holder.Reference<T> reference = commandContext.getArgument(string, Holder.Reference.class);
		ResourceKey<?> resourceKey2 = reference.key();
		if (resourceKey2.isFor(resourceKey)) {
			return reference;
		} else {
			throw ERROR_INVALID_RESOURCE_TYPE.create(resourceKey2.location(), resourceKey2.registry(), resourceKey.location());
		}
	}

	public static Holder.Reference<Attribute> getAttribute(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getResource(commandContext, string, Registries.ATTRIBUTE);
	}

	public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getResource(commandContext, string, Registries.CONFIGURED_FEATURE);
	}

	public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getResource(commandContext, string, Registries.STRUCTURE);
	}

	public static Holder.Reference<EntityType<?>> getEntityType(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getResource(commandContext, string, Registries.ENTITY_TYPE);
	}

	public static Holder.Reference<EntityType<?>> getSummonableEntityType(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		Holder.Reference<EntityType<?>> reference = getResource(commandContext, string, Registries.ENTITY_TYPE);
		if (!reference.value().canSummon()) {
			throw ERROR_NOT_SUMMONABLE_ENTITY.create(reference.key().location().toString());
		} else {
			return reference;
		}
	}

	public static Holder.Reference<MobEffect> getMobEffect(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getResource(commandContext, string, Registries.MOB_EFFECT);
	}

	public static Holder.Reference<Enchantment> getEnchantment(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getResource(commandContext, string, Registries.ENCHANTMENT);
	}

	public Holder.Reference<T> parse(StringReader stringReader) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
		ResourceKey<T> resourceKey = ResourceKey.create(this.registryKey, resourceLocation);
		return (Holder.Reference<T>)this.registryLookup
			.get(resourceKey)
			.orElseThrow(() -> ERROR_UNKNOWN_RESOURCE.create(resourceLocation, this.registryKey.location()));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggestResource(this.registryLookup.listElementIds().map(ResourceKey::location), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class Info<T> implements ArgumentTypeInfo<ResourceArgument<T>, ResourceArgument.Info<T>.Template> {
		public void serializeToNetwork(ResourceArgument.Info<T>.Template template, FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeResourceLocation(template.registryKey.location());
		}

		public ResourceArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			return new ResourceArgument.Info.Template(ResourceKey.createRegistryKey(resourceLocation));
		}

		public void serializeToJson(ResourceArgument.Info<T>.Template template, JsonObject jsonObject) {
			jsonObject.addProperty("registry", template.registryKey.location().toString());
		}

		public ResourceArgument.Info<T>.Template unpack(ResourceArgument<T> resourceArgument) {
			return new ResourceArgument.Info.Template(resourceArgument.registryKey);
		}

		public final class Template implements ArgumentTypeInfo.Template<ResourceArgument<T>> {
			final ResourceKey<? extends Registry<T>> registryKey;

			Template(ResourceKey<? extends Registry<T>> resourceKey) {
				this.registryKey = resourceKey;
			}

			public ResourceArgument<T> instantiate(CommandBuildContext commandBuildContext) {
				return new ResourceArgument<>(commandBuildContext, this.registryKey);
			}

			@Override
			public ArgumentTypeInfo<ResourceArgument<T>, ?> type() {
				return Info.this;
			}
		}
	}
}
