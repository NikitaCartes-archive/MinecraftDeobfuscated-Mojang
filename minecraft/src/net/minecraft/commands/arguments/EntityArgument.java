package net.minecraft.commands.arguments;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntityArgument implements ArgumentType<EntitySelector> {
	private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "@e", "@e[type=foo]", "dd12be42-52a9-4a91-a8a1-11c01849e498");
	public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_ENTITY = new SimpleCommandExceptionType(new TranslatableComponent("argument.entity.toomany"));
	public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER = new SimpleCommandExceptionType(new TranslatableComponent("argument.player.toomany"));
	public static final SimpleCommandExceptionType ERROR_ONLY_PLAYERS_ALLOWED = new SimpleCommandExceptionType(
		new TranslatableComponent("argument.player.entities")
	);
	public static final SimpleCommandExceptionType NO_ENTITIES_FOUND = new SimpleCommandExceptionType(new TranslatableComponent("argument.entity.notfound.entity"));
	public static final SimpleCommandExceptionType NO_PLAYERS_FOUND = new SimpleCommandExceptionType(new TranslatableComponent("argument.entity.notfound.player"));
	public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(
		new TranslatableComponent("argument.entity.selector.not_allowed")
	);
	private final boolean single;
	private final boolean playersOnly;

	protected EntityArgument(boolean bl, boolean bl2) {
		this.single = bl;
		this.playersOnly = bl2;
	}

	public static EntityArgument entity() {
		return new EntityArgument(true, false);
	}

	public static Entity getEntity(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<EntitySelector>getArgument(string, EntitySelector.class).findSingleEntity(commandContext.getSource());
	}

	public static EntityArgument entities() {
		return new EntityArgument(false, false);
	}

	public static Collection<? extends Entity> getEntities(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		Collection<? extends Entity> collection = getOptionalEntities(commandContext, string);
		if (collection.isEmpty()) {
			throw NO_ENTITIES_FOUND.create();
		} else {
			return collection;
		}
	}

	public static Collection<? extends Entity> getOptionalEntities(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<EntitySelector>getArgument(string, EntitySelector.class).findEntities(commandContext.getSource());
	}

	public static Collection<ServerPlayer> getOptionalPlayers(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<EntitySelector>getArgument(string, EntitySelector.class).findPlayers(commandContext.getSource());
	}

	public static EntityArgument player() {
		return new EntityArgument(true, true);
	}

	public static ServerPlayer getPlayer(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<EntitySelector>getArgument(string, EntitySelector.class).findSinglePlayer(commandContext.getSource());
	}

	public static EntityArgument players() {
		return new EntityArgument(false, true);
	}

	public static Collection<ServerPlayer> getPlayers(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		List<ServerPlayer> list = commandContext.<EntitySelector>getArgument(string, EntitySelector.class).findPlayers(commandContext.getSource());
		if (list.isEmpty()) {
			throw NO_PLAYERS_FOUND.create();
		} else {
			return list;
		}
	}

	public EntitySelector parse(StringReader stringReader) throws CommandSyntaxException {
		int i = 0;
		EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
		EntitySelector entitySelector = entitySelectorParser.parse();
		if (entitySelector.getMaxResults() > 1 && this.single) {
			if (this.playersOnly) {
				stringReader.setCursor(0);
				throw ERROR_NOT_SINGLE_PLAYER.createWithContext(stringReader);
			} else {
				stringReader.setCursor(0);
				throw ERROR_NOT_SINGLE_ENTITY.createWithContext(stringReader);
			}
		} else if (entitySelector.includesEntities() && this.playersOnly && !entitySelector.isSelfSelector()) {
			stringReader.setCursor(0);
			throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext(stringReader);
		} else {
			return entitySelector;
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		if (commandContext.getSource() instanceof SharedSuggestionProvider) {
			StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
			stringReader.setCursor(suggestionsBuilder.getStart());
			SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)commandContext.getSource();
			EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader, sharedSuggestionProvider.hasPermission(2));

			try {
				entitySelectorParser.parse();
			} catch (CommandSyntaxException var7) {
			}

			return entitySelectorParser.fillSuggestions(
				suggestionsBuilder,
				suggestionsBuilderx -> {
					Collection<String> collection = sharedSuggestionProvider.getOnlinePlayerNames();
					Iterable<String> iterable = (Iterable<String>)(this.playersOnly
						? collection
						: Iterables.concat(collection, sharedSuggestionProvider.getSelectedEntities()));
					SharedSuggestionProvider.suggest(iterable, suggestionsBuilderx);
				}
			);
		} else {
			return Suggestions.empty();
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class Serializer implements ArgumentSerializer<EntityArgument> {
		public void serializeToNetwork(EntityArgument entityArgument, FriendlyByteBuf friendlyByteBuf) {
			byte b = 0;
			if (entityArgument.single) {
				b = (byte)(b | 1);
			}

			if (entityArgument.playersOnly) {
				b = (byte)(b | 2);
			}

			friendlyByteBuf.writeByte(b);
		}

		public EntityArgument deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			byte b = friendlyByteBuf.readByte();
			return new EntityArgument((b & 1) != 0, (b & 2) != 0);
		}

		public void serializeToJson(EntityArgument entityArgument, JsonObject jsonObject) {
			jsonObject.addProperty("amount", entityArgument.single ? "single" : "multiple");
			jsonObject.addProperty("type", entityArgument.playersOnly ? "players" : "entities");
		}
	}
}
