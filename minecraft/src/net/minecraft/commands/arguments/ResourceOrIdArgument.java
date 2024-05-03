package net.minecraft.commands.arguments;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceOrIdArgument<T> implements ArgumentType<Holder<T>> {
	private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "012", "{}", "true");
	public static final DynamicCommandExceptionType ERROR_FAILED_TO_PARSE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.resource_or_id.failed_to_parse", object)
	);
	private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.resource_or_id.invalid"));
	private final HolderLookup.Provider registryLookup;
	private final boolean hasRegistry;
	private final Codec<Holder<T>> codec;

	protected ResourceOrIdArgument(CommandBuildContext commandBuildContext, ResourceKey<Registry<T>> resourceKey, Codec<Holder<T>> codec) {
		this.registryLookup = commandBuildContext;
		this.hasRegistry = commandBuildContext.lookup(resourceKey).isPresent();
		this.codec = codec;
	}

	public static ResourceOrIdArgument.LootTableArgument lootTable(CommandBuildContext commandBuildContext) {
		return new ResourceOrIdArgument.LootTableArgument(commandBuildContext);
	}

	public static Holder<LootTable> getLootTable(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getResource(commandContext, string);
	}

	public static ResourceOrIdArgument.LootModifierArgument lootModifier(CommandBuildContext commandBuildContext) {
		return new ResourceOrIdArgument.LootModifierArgument(commandBuildContext);
	}

	public static Holder<LootItemFunction> getLootModifier(CommandContext<CommandSourceStack> commandContext, String string) {
		return getResource(commandContext, string);
	}

	public static ResourceOrIdArgument.LootPredicateArgument lootPredicate(CommandBuildContext commandBuildContext) {
		return new ResourceOrIdArgument.LootPredicateArgument(commandBuildContext);
	}

	public static Holder<LootItemCondition> getLootPredicate(CommandContext<CommandSourceStack> commandContext, String string) {
		return getResource(commandContext, string);
	}

	private static <T> Holder<T> getResource(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, Holder.class);
	}

	@Nullable
	public Holder<T> parse(StringReader stringReader) throws CommandSyntaxException {
		Tag tag = parseInlineOrId(stringReader);
		if (!this.hasRegistry) {
			return null;
		} else {
			RegistryOps<Tag> registryOps = this.registryLookup.createSerializationContext(NbtOps.INSTANCE);
			return this.codec.parse(registryOps, tag).getOrThrow(string -> ERROR_FAILED_TO_PARSE.createWithContext(stringReader, string));
		}
	}

	@VisibleForTesting
	static Tag parseInlineOrId(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		Tag tag = new TagParser(stringReader).readValue();
		if (hasConsumedWholeArg(stringReader)) {
			return tag;
		} else {
			stringReader.setCursor(i);
			ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
			if (hasConsumedWholeArg(stringReader)) {
				return StringTag.valueOf(resourceLocation.toString());
			} else {
				stringReader.setCursor(i);
				throw ERROR_INVALID.createWithContext(stringReader);
			}
		}
	}

	private static boolean hasConsumedWholeArg(StringReader stringReader) {
		return !stringReader.canRead() || stringReader.peek() == ' ';
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class LootModifierArgument extends ResourceOrIdArgument<LootItemFunction> {
		protected LootModifierArgument(CommandBuildContext commandBuildContext) {
			super(commandBuildContext, Registries.ITEM_MODIFIER, LootItemFunctions.CODEC);
		}
	}

	public static class LootPredicateArgument extends ResourceOrIdArgument<LootItemCondition> {
		protected LootPredicateArgument(CommandBuildContext commandBuildContext) {
			super(commandBuildContext, Registries.PREDICATE, LootItemCondition.CODEC);
		}
	}

	public static class LootTableArgument extends ResourceOrIdArgument<LootTable> {
		protected LootTableArgument(CommandBuildContext commandBuildContext) {
			super(commandBuildContext, Registries.LOOT_TABLE, LootTable.CODEC);
		}
	}
}
