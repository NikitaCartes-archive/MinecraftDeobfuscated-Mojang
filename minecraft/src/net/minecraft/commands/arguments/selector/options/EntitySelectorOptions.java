package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class EntitySelectorOptions {
	private static final Map<String, EntitySelectorOptions.Option> OPTIONS = Maps.<String, EntitySelectorOptions.Option>newHashMap();
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.entity.options.unknown", object)
	);
	public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.entity.options.inapplicable", object)
	);
	public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType(
		Component.translatable("argument.entity.options.distance.negative")
	);
	public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType(
		Component.translatable("argument.entity.options.level.negative")
	);
	public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(
		Component.translatable("argument.entity.options.limit.toosmall")
	);
	public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.entity.options.sort.irreversible", object)
	);
	public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.entity.options.mode.invalid", object)
	);
	public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.entity.options.type.invalid", object)
	);

	private static void register(String string, EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> predicate, Component component) {
		OPTIONS.put(string, new EntitySelectorOptions.Option(modifier, predicate, component));
	}

	public static void bootStrap() {
		if (OPTIONS.isEmpty()) {
			register("name", entitySelectorParser -> {
				int i = entitySelectorParser.getReader().getCursor();
				boolean bl = entitySelectorParser.shouldInvertValue();
				String string = entitySelectorParser.getReader().readString();
				if (entitySelectorParser.hasNameNotEquals() && !bl) {
					entitySelectorParser.getReader().setCursor(i);
					throw ERROR_INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), "name");
				} else {
					if (bl) {
						entitySelectorParser.setHasNameNotEquals(true);
					} else {
						entitySelectorParser.setHasNameEquals(true);
					}

					entitySelectorParser.addPredicate(entity -> entity.getName().getString().equals(string) != bl);
				}
			}, entitySelectorParser -> !entitySelectorParser.hasNameEquals(), Component.translatable("argument.entity.options.name.description"));
			register("distance", entitySelectorParser -> {
				int i = entitySelectorParser.getReader().getCursor();
				MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromReader(entitySelectorParser.getReader());
				if ((!doubles.min().isPresent() || !((Double)doubles.min().get() < 0.0)) && (!doubles.max().isPresent() || !((Double)doubles.max().get() < 0.0))) {
					entitySelectorParser.setDistance(doubles);
					entitySelectorParser.setWorldLimited();
				} else {
					entitySelectorParser.getReader().setCursor(i);
					throw ERROR_RANGE_NEGATIVE.createWithContext(entitySelectorParser.getReader());
				}
			}, entitySelectorParser -> entitySelectorParser.getDistance().isAny(), Component.translatable("argument.entity.options.distance.description"));
			register("level", entitySelectorParser -> {
				int i = entitySelectorParser.getReader().getCursor();
				MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromReader(entitySelectorParser.getReader());
				if ((!ints.min().isPresent() || (Integer)ints.min().get() >= 0) && (!ints.max().isPresent() || (Integer)ints.max().get() >= 0)) {
					entitySelectorParser.setLevel(ints);
					entitySelectorParser.setIncludesEntities(false);
				} else {
					entitySelectorParser.getReader().setCursor(i);
					throw ERROR_LEVEL_NEGATIVE.createWithContext(entitySelectorParser.getReader());
				}
			}, entitySelectorParser -> entitySelectorParser.getLevel().isAny(), Component.translatable("argument.entity.options.level.description"));
			register("x", entitySelectorParser -> {
				entitySelectorParser.setWorldLimited();
				entitySelectorParser.setX(entitySelectorParser.getReader().readDouble());
			}, entitySelectorParser -> entitySelectorParser.getX() == null, Component.translatable("argument.entity.options.x.description"));
			register("y", entitySelectorParser -> {
				entitySelectorParser.setWorldLimited();
				entitySelectorParser.setY(entitySelectorParser.getReader().readDouble());
			}, entitySelectorParser -> entitySelectorParser.getY() == null, Component.translatable("argument.entity.options.y.description"));
			register("z", entitySelectorParser -> {
				entitySelectorParser.setWorldLimited();
				entitySelectorParser.setZ(entitySelectorParser.getReader().readDouble());
			}, entitySelectorParser -> entitySelectorParser.getZ() == null, Component.translatable("argument.entity.options.z.description"));
			register("dx", entitySelectorParser -> {
				entitySelectorParser.setWorldLimited();
				entitySelectorParser.setDeltaX(entitySelectorParser.getReader().readDouble());
			}, entitySelectorParser -> entitySelectorParser.getDeltaX() == null, Component.translatable("argument.entity.options.dx.description"));
			register("dy", entitySelectorParser -> {
				entitySelectorParser.setWorldLimited();
				entitySelectorParser.setDeltaY(entitySelectorParser.getReader().readDouble());
			}, entitySelectorParser -> entitySelectorParser.getDeltaY() == null, Component.translatable("argument.entity.options.dy.description"));
			register("dz", entitySelectorParser -> {
				entitySelectorParser.setWorldLimited();
				entitySelectorParser.setDeltaZ(entitySelectorParser.getReader().readDouble());
			}, entitySelectorParser -> entitySelectorParser.getDeltaZ() == null, Component.translatable("argument.entity.options.dz.description"));
			register(
				"x_rotation",
				entitySelectorParser -> entitySelectorParser.setRotX(WrappedMinMaxBounds.fromReader(entitySelectorParser.getReader(), true, Mth::wrapDegrees)),
				entitySelectorParser -> entitySelectorParser.getRotX() == WrappedMinMaxBounds.ANY,
				Component.translatable("argument.entity.options.x_rotation.description")
			);
			register(
				"y_rotation",
				entitySelectorParser -> entitySelectorParser.setRotY(WrappedMinMaxBounds.fromReader(entitySelectorParser.getReader(), true, Mth::wrapDegrees)),
				entitySelectorParser -> entitySelectorParser.getRotY() == WrappedMinMaxBounds.ANY,
				Component.translatable("argument.entity.options.y_rotation.description")
			);
			register(
				"limit",
				entitySelectorParser -> {
					int i = entitySelectorParser.getReader().getCursor();
					int j = entitySelectorParser.getReader().readInt();
					if (j < 1) {
						entitySelectorParser.getReader().setCursor(i);
						throw ERROR_LIMIT_TOO_SMALL.createWithContext(entitySelectorParser.getReader());
					} else {
						entitySelectorParser.setMaxResults(j);
						entitySelectorParser.setLimited(true);
					}
				},
				entitySelectorParser -> !entitySelectorParser.isCurrentEntity() && !entitySelectorParser.isLimited(),
				Component.translatable("argument.entity.options.limit.description")
			);
			register(
				"sort",
				entitySelectorParser -> {
					int i = entitySelectorParser.getReader().getCursor();
					String string = entitySelectorParser.getReader().readUnquotedString();
					entitySelectorParser.setSuggestions(
						(suggestionsBuilder, consumer) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), suggestionsBuilder)
					);

					entitySelectorParser.setOrder(switch (string) {
						case "nearest" -> EntitySelectorParser.ORDER_NEAREST;
						case "furthest" -> EntitySelectorParser.ORDER_FURTHEST;
						case "random" -> EntitySelectorParser.ORDER_RANDOM;
						case "arbitrary" -> EntitySelector.ORDER_ARBITRARY;
						default -> {
							entitySelectorParser.getReader().setCursor(i);
							throw ERROR_SORT_UNKNOWN.createWithContext(entitySelectorParser.getReader(), string);
						}
					});
					entitySelectorParser.setSorted(true);
				},
				entitySelectorParser -> !entitySelectorParser.isCurrentEntity() && !entitySelectorParser.isSorted(),
				Component.translatable("argument.entity.options.sort.description")
			);
			register("gamemode", entitySelectorParser -> {
				entitySelectorParser.setSuggestions((suggestionsBuilder, consumer) -> {
					String stringx = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
					boolean blx = !entitySelectorParser.hasGamemodeNotEquals();
					boolean bl2 = true;
					if (!stringx.isEmpty()) {
						if (stringx.charAt(0) == '!') {
							blx = false;
							stringx = stringx.substring(1);
						} else {
							bl2 = false;
						}
					}

					for (GameType gameTypex : GameType.values()) {
						if (gameTypex.getName().toLowerCase(Locale.ROOT).startsWith(stringx)) {
							if (bl2) {
								suggestionsBuilder.suggest("!" + gameTypex.getName());
							}

							if (blx) {
								suggestionsBuilder.suggest(gameTypex.getName());
							}
						}
					}

					return suggestionsBuilder.buildFuture();
				});
				int i = entitySelectorParser.getReader().getCursor();
				boolean bl = entitySelectorParser.shouldInvertValue();
				if (entitySelectorParser.hasGamemodeNotEquals() && !bl) {
					entitySelectorParser.getReader().setCursor(i);
					throw ERROR_INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), "gamemode");
				} else {
					String string = entitySelectorParser.getReader().readUnquotedString();
					GameType gameType = GameType.byName(string, null);
					if (gameType == null) {
						entitySelectorParser.getReader().setCursor(i);
						throw ERROR_GAME_MODE_INVALID.createWithContext(entitySelectorParser.getReader(), string);
					} else {
						entitySelectorParser.setIncludesEntities(false);
						entitySelectorParser.addPredicate(entity -> {
							if (!(entity instanceof ServerPlayer)) {
								return false;
							} else {
								GameType gameType2 = ((ServerPlayer)entity).gameMode.getGameModeForPlayer();
								return bl ? gameType2 != gameType : gameType2 == gameType;
							}
						});
						if (bl) {
							entitySelectorParser.setHasGamemodeNotEquals(true);
						} else {
							entitySelectorParser.setHasGamemodeEquals(true);
						}
					}
				}
			}, entitySelectorParser -> !entitySelectorParser.hasGamemodeEquals(), Component.translatable("argument.entity.options.gamemode.description"));
			register("team", entitySelectorParser -> {
				boolean bl = entitySelectorParser.shouldInvertValue();
				String string = entitySelectorParser.getReader().readUnquotedString();
				entitySelectorParser.addPredicate(entity -> {
					if (!(entity instanceof LivingEntity)) {
						return false;
					} else {
						Team team = entity.getTeam();
						String string2 = team == null ? "" : team.getName();
						return string2.equals(string) != bl;
					}
				});
				if (bl) {
					entitySelectorParser.setHasTeamNotEquals(true);
				} else {
					entitySelectorParser.setHasTeamEquals(true);
				}
			}, entitySelectorParser -> !entitySelectorParser.hasTeamEquals(), Component.translatable("argument.entity.options.team.description"));
			register("type", entitySelectorParser -> {
				entitySelectorParser.setSuggestions((suggestionsBuilder, consumer) -> {
					SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), suggestionsBuilder, String.valueOf('!'));
					SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTagNames().map(TagKey::location), suggestionsBuilder, "!#");
					if (!entitySelectorParser.isTypeLimitedInversely()) {
						SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), suggestionsBuilder);
						SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTagNames().map(TagKey::location), suggestionsBuilder, String.valueOf('#'));
					}

					return suggestionsBuilder.buildFuture();
				});
				int i = entitySelectorParser.getReader().getCursor();
				boolean bl = entitySelectorParser.shouldInvertValue();
				if (entitySelectorParser.isTypeLimitedInversely() && !bl) {
					entitySelectorParser.getReader().setCursor(i);
					throw ERROR_INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), "type");
				} else {
					if (bl) {
						entitySelectorParser.setTypeLimitedInversely();
					}

					if (entitySelectorParser.isTag()) {
						TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.read(entitySelectorParser.getReader()));
						entitySelectorParser.addPredicate(entity -> entity.getType().is(tagKey) != bl);
					} else {
						ResourceLocation resourceLocation = ResourceLocation.read(entitySelectorParser.getReader());
						EntityType<?> entityType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getOptional(resourceLocation).orElseThrow(() -> {
							entitySelectorParser.getReader().setCursor(i);
							return ERROR_ENTITY_TYPE_INVALID.createWithContext(entitySelectorParser.getReader(), resourceLocation.toString());
						});
						if (Objects.equals(EntityType.PLAYER, entityType) && !bl) {
							entitySelectorParser.setIncludesEntities(false);
						}

						entitySelectorParser.addPredicate(entity -> Objects.equals(entityType, entity.getType()) != bl);
						if (!bl) {
							entitySelectorParser.limitToType(entityType);
						}
					}
				}
			}, entitySelectorParser -> !entitySelectorParser.isTypeLimited(), Component.translatable("argument.entity.options.type.description"));
			register("tag", entitySelectorParser -> {
				boolean bl = entitySelectorParser.shouldInvertValue();
				String string = entitySelectorParser.getReader().readUnquotedString();
				entitySelectorParser.addPredicate(entity -> "".equals(string) ? entity.getTags().isEmpty() != bl : entity.getTags().contains(string) != bl);
			}, entitySelectorParser -> true, Component.translatable("argument.entity.options.tag.description"));
			register("nbt", entitySelectorParser -> {
				boolean bl = entitySelectorParser.shouldInvertValue();
				CompoundTag compoundTag = new TagParser(entitySelectorParser.getReader()).readStruct();
				entitySelectorParser.addPredicate(entity -> {
					CompoundTag compoundTag2 = entity.saveWithoutId(new CompoundTag());
					if (entity instanceof ServerPlayer serverPlayer) {
						ItemStack itemStack = serverPlayer.getInventory().getSelected();
						if (!itemStack.isEmpty()) {
							compoundTag2.put("SelectedItem", itemStack.save(serverPlayer.registryAccess()));
						}
					}

					return NbtUtils.compareNbt(compoundTag, compoundTag2, true) != bl;
				});
			}, entitySelectorParser -> true, Component.translatable("argument.entity.options.nbt.description"));
			register("scores", entitySelectorParser -> {
				StringReader stringReader = entitySelectorParser.getReader();
				Map<String, MinMaxBounds.Ints> map = Maps.<String, MinMaxBounds.Ints>newHashMap();
				stringReader.expect('{');
				stringReader.skipWhitespace();

				while (stringReader.canRead() && stringReader.peek() != '}') {
					stringReader.skipWhitespace();
					String string = stringReader.readUnquotedString();
					stringReader.skipWhitespace();
					stringReader.expect('=');
					stringReader.skipWhitespace();
					MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromReader(stringReader);
					map.put(string, ints);
					stringReader.skipWhitespace();
					if (stringReader.canRead() && stringReader.peek() == ',') {
						stringReader.skip();
					}
				}

				stringReader.expect('}');
				if (!map.isEmpty()) {
					entitySelectorParser.addPredicate(entity -> {
						Scoreboard scoreboard = entity.getServer().getScoreboard();

						for (Entry<String, MinMaxBounds.Ints> entry : map.entrySet()) {
							Objective objective = scoreboard.getObjective((String)entry.getKey());
							if (objective == null) {
								return false;
							}

							ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(entity, objective);
							if (readOnlyScoreInfo == null) {
								return false;
							}

							if (!((MinMaxBounds.Ints)entry.getValue()).matches(readOnlyScoreInfo.value())) {
								return false;
							}
						}

						return true;
					});
				}

				entitySelectorParser.setHasScores(true);
			}, entitySelectorParser -> !entitySelectorParser.hasScores(), Component.translatable("argument.entity.options.scores.description"));
			register("advancements", entitySelectorParser -> {
				StringReader stringReader = entitySelectorParser.getReader();
				Map<ResourceLocation, Predicate<AdvancementProgress>> map = Maps.<ResourceLocation, Predicate<AdvancementProgress>>newHashMap();
				stringReader.expect('{');
				stringReader.skipWhitespace();

				while (stringReader.canRead() && stringReader.peek() != '}') {
					stringReader.skipWhitespace();
					ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
					stringReader.skipWhitespace();
					stringReader.expect('=');
					stringReader.skipWhitespace();
					if (stringReader.canRead() && stringReader.peek() == '{') {
						Map<String, Predicate<CriterionProgress>> map2 = Maps.<String, Predicate<CriterionProgress>>newHashMap();
						stringReader.skipWhitespace();
						stringReader.expect('{');
						stringReader.skipWhitespace();

						while (stringReader.canRead() && stringReader.peek() != '}') {
							stringReader.skipWhitespace();
							String string = stringReader.readUnquotedString();
							stringReader.skipWhitespace();
							stringReader.expect('=');
							stringReader.skipWhitespace();
							boolean bl = stringReader.readBoolean();
							map2.put(string, (Predicate)criterionProgress -> criterionProgress.isDone() == bl);
							stringReader.skipWhitespace();
							if (stringReader.canRead() && stringReader.peek() == ',') {
								stringReader.skip();
							}
						}

						stringReader.skipWhitespace();
						stringReader.expect('}');
						stringReader.skipWhitespace();
						map.put(resourceLocation, (Predicate)advancementProgress -> {
							for (Entry<String, Predicate<CriterionProgress>> entry : map2.entrySet()) {
								CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
								if (criterionProgress == null || !((Predicate)entry.getValue()).test(criterionProgress)) {
									return false;
								}
							}

							return true;
						});
					} else {
						boolean bl2 = stringReader.readBoolean();
						map.put(resourceLocation, (Predicate)advancementProgress -> advancementProgress.isDone() == bl2);
					}

					stringReader.skipWhitespace();
					if (stringReader.canRead() && stringReader.peek() == ',') {
						stringReader.skip();
					}
				}

				stringReader.expect('}');
				if (!map.isEmpty()) {
					entitySelectorParser.addPredicate(entity -> {
						if (!(entity instanceof ServerPlayer serverPlayer)) {
							return false;
						} else {
							PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
							ServerAdvancementManager serverAdvancementManager = serverPlayer.getServer().getAdvancements();

							for (Entry<ResourceLocation, Predicate<AdvancementProgress>> entry : map.entrySet()) {
								AdvancementHolder advancementHolder = serverAdvancementManager.get((ResourceLocation)entry.getKey());
								if (advancementHolder == null || !((Predicate)entry.getValue()).test(playerAdvancements.getOrStartProgress(advancementHolder))) {
									return false;
								}
							}

							return true;
						}
					});
					entitySelectorParser.setIncludesEntities(false);
				}

				entitySelectorParser.setHasAdvancements(true);
			}, entitySelectorParser -> !entitySelectorParser.hasAdvancements(), Component.translatable("argument.entity.options.advancements.description"));
			register(
				"predicate",
				entitySelectorParser -> {
					boolean bl = entitySelectorParser.shouldInvertValue();
					ResourceKey<LootItemCondition> resourceKey = ResourceKey.create(Registries.PREDICATE, ResourceLocation.read(entitySelectorParser.getReader()));
					entitySelectorParser.addPredicate(
						entity -> {
							if (!(entity.level() instanceof ServerLevel)) {
								return false;
							} else {
								ServerLevel serverLevel = (ServerLevel)entity.level();
								Optional<LootItemCondition> optional = serverLevel.getServer()
									.reloadableRegistries()
									.lookup()
									.get(Registries.PREDICATE, resourceKey)
									.map(Holder::value);
								if (optional.isEmpty()) {
									return false;
								} else {
									LootParams lootParams = new LootParams.Builder(serverLevel)
										.withParameter(LootContextParams.THIS_ENTITY, entity)
										.withParameter(LootContextParams.ORIGIN, entity.position())
										.create(LootContextParamSets.SELECTOR);
									LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
									lootContext.pushVisitedElement(LootContext.createVisitedEntry((LootItemCondition)optional.get()));
									return bl ^ ((LootItemCondition)optional.get()).test(lootContext);
								}
							}
						}
					);
				},
				entitySelectorParser -> true,
				Component.translatable("argument.entity.options.predicate.description")
			);
		}
	}

	public static EntitySelectorOptions.Modifier get(EntitySelectorParser entitySelectorParser, String string, int i) throws CommandSyntaxException {
		EntitySelectorOptions.Option option = (EntitySelectorOptions.Option)OPTIONS.get(string);
		if (option != null) {
			if (option.canUse.test(entitySelectorParser)) {
				return option.modifier;
			} else {
				throw ERROR_INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), string);
			}
		} else {
			entitySelectorParser.getReader().setCursor(i);
			throw ERROR_UNKNOWN_OPTION.createWithContext(entitySelectorParser.getReader(), string);
		}
	}

	public static void suggestNames(EntitySelectorParser entitySelectorParser, SuggestionsBuilder suggestionsBuilder) {
		String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);

		for (Entry<String, EntitySelectorOptions.Option> entry : OPTIONS.entrySet()) {
			if (((EntitySelectorOptions.Option)entry.getValue()).canUse.test(entitySelectorParser)
				&& ((String)entry.getKey()).toLowerCase(Locale.ROOT).startsWith(string)) {
				suggestionsBuilder.suggest((String)entry.getKey() + "=", ((EntitySelectorOptions.Option)entry.getValue()).description);
			}
		}
	}

	public interface Modifier {
		void handle(EntitySelectorParser entitySelectorParser) throws CommandSyntaxException;
	}

	static record Option(EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> canUse, Component description) {
	}
}
