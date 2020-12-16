/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;

public class EntitySelectorOptions {
    private static final Map<String, Option> OPTIONS = Maps.newHashMap();
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType(object -> new TranslatableComponent("argument.entity.options.unknown", object));
    public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType(object -> new TranslatableComponent("argument.entity.options.inapplicable", object));
    public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType(new TranslatableComponent("argument.entity.options.distance.negative"));
    public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType(new TranslatableComponent("argument.entity.options.level.negative"));
    public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(new TranslatableComponent("argument.entity.options.limit.toosmall"));
    public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType(object -> new TranslatableComponent("argument.entity.options.sort.irreversible", object));
    public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType(object -> new TranslatableComponent("argument.entity.options.mode.invalid", object));
    public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType(object -> new TranslatableComponent("argument.entity.options.type.invalid", object));

    private static void register(String string, Modifier modifier, Predicate<EntitySelectorParser> predicate, Component component) {
        OPTIONS.put(string, new Option(modifier, predicate, component));
    }

    public static void bootStrap() {
        if (!OPTIONS.isEmpty()) {
            return;
        }
        EntitySelectorOptions.register("name", entitySelectorParser -> {
            int i = entitySelectorParser.getReader().getCursor();
            boolean bl = entitySelectorParser.shouldInvertValue();
            String string = entitySelectorParser.getReader().readString();
            if (entitySelectorParser.hasNameNotEquals() && !bl) {
                entitySelectorParser.getReader().setCursor(i);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), "name");
            }
            if (bl) {
                entitySelectorParser.setHasNameNotEquals(true);
            } else {
                entitySelectorParser.setHasNameEquals(true);
            }
            entitySelectorParser.addPredicate(entity -> entity.getName().getString().equals(string) != bl);
        }, entitySelectorParser -> !entitySelectorParser.hasNameEquals(), new TranslatableComponent("argument.entity.options.name.description"));
        EntitySelectorOptions.register("distance", entitySelectorParser -> {
            int i = entitySelectorParser.getReader().getCursor();
            MinMaxBounds.Floats floats = MinMaxBounds.Floats.fromReader(entitySelectorParser.getReader());
            if (floats.getMin() != null && ((Float)floats.getMin()).floatValue() < 0.0f || floats.getMax() != null && ((Float)floats.getMax()).floatValue() < 0.0f) {
                entitySelectorParser.getReader().setCursor(i);
                throw ERROR_RANGE_NEGATIVE.createWithContext(entitySelectorParser.getReader());
            }
            entitySelectorParser.setDistance(floats);
            entitySelectorParser.setWorldLimited();
        }, entitySelectorParser -> entitySelectorParser.getDistance().isAny(), new TranslatableComponent("argument.entity.options.distance.description"));
        EntitySelectorOptions.register("level", entitySelectorParser -> {
            int i = entitySelectorParser.getReader().getCursor();
            MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromReader(entitySelectorParser.getReader());
            if (ints.getMin() != null && (Integer)ints.getMin() < 0 || ints.getMax() != null && (Integer)ints.getMax() < 0) {
                entitySelectorParser.getReader().setCursor(i);
                throw ERROR_LEVEL_NEGATIVE.createWithContext(entitySelectorParser.getReader());
            }
            entitySelectorParser.setLevel(ints);
            entitySelectorParser.setIncludesEntities(false);
        }, entitySelectorParser -> entitySelectorParser.getLevel().isAny(), new TranslatableComponent("argument.entity.options.level.description"));
        EntitySelectorOptions.register("x", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setX(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getX() == null, new TranslatableComponent("argument.entity.options.x.description"));
        EntitySelectorOptions.register("y", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setY(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getY() == null, new TranslatableComponent("argument.entity.options.y.description"));
        EntitySelectorOptions.register("z", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setZ(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getZ() == null, new TranslatableComponent("argument.entity.options.z.description"));
        EntitySelectorOptions.register("dx", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setDeltaX(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getDeltaX() == null, new TranslatableComponent("argument.entity.options.dx.description"));
        EntitySelectorOptions.register("dy", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setDeltaY(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getDeltaY() == null, new TranslatableComponent("argument.entity.options.dy.description"));
        EntitySelectorOptions.register("dz", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setDeltaZ(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getDeltaZ() == null, new TranslatableComponent("argument.entity.options.dz.description"));
        EntitySelectorOptions.register("x_rotation", entitySelectorParser -> entitySelectorParser.setRotX(WrappedMinMaxBounds.fromReader(entitySelectorParser.getReader(), true, Mth::wrapDegrees)), entitySelectorParser -> entitySelectorParser.getRotX() == WrappedMinMaxBounds.ANY, new TranslatableComponent("argument.entity.options.x_rotation.description"));
        EntitySelectorOptions.register("y_rotation", entitySelectorParser -> entitySelectorParser.setRotY(WrappedMinMaxBounds.fromReader(entitySelectorParser.getReader(), true, Mth::wrapDegrees)), entitySelectorParser -> entitySelectorParser.getRotY() == WrappedMinMaxBounds.ANY, new TranslatableComponent("argument.entity.options.y_rotation.description"));
        EntitySelectorOptions.register("limit", entitySelectorParser -> {
            int i = entitySelectorParser.getReader().getCursor();
            int j = entitySelectorParser.getReader().readInt();
            if (j < 1) {
                entitySelectorParser.getReader().setCursor(i);
                throw ERROR_LIMIT_TOO_SMALL.createWithContext(entitySelectorParser.getReader());
            }
            entitySelectorParser.setMaxResults(j);
            entitySelectorParser.setLimited(true);
        }, entitySelectorParser -> !entitySelectorParser.isCurrentEntity() && !entitySelectorParser.isLimited(), new TranslatableComponent("argument.entity.options.limit.description"));
        EntitySelectorOptions.register("sort", entitySelectorParser -> {
            BiConsumer<Vec3, List<? extends Entity>> biConsumer;
            int i = entitySelectorParser.getReader().getCursor();
            String string = entitySelectorParser.getReader().readUnquotedString();
            entitySelectorParser.setSuggestions((suggestionsBuilder, consumer) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), suggestionsBuilder));
            switch (string) {
                case "nearest": {
                    biConsumer = EntitySelectorParser.ORDER_NEAREST;
                    break;
                }
                case "furthest": {
                    biConsumer = EntitySelectorParser.ORDER_FURTHEST;
                    break;
                }
                case "random": {
                    biConsumer = EntitySelectorParser.ORDER_RANDOM;
                    break;
                }
                case "arbitrary": {
                    biConsumer = EntitySelectorParser.ORDER_ARBITRARY;
                    break;
                }
                default: {
                    entitySelectorParser.getReader().setCursor(i);
                    throw ERROR_SORT_UNKNOWN.createWithContext(entitySelectorParser.getReader(), string);
                }
            }
            entitySelectorParser.setOrder(biConsumer);
            entitySelectorParser.setSorted(true);
        }, entitySelectorParser -> !entitySelectorParser.isCurrentEntity() && !entitySelectorParser.isSorted(), new TranslatableComponent("argument.entity.options.sort.description"));
        EntitySelectorOptions.register("gamemode", entitySelectorParser -> {
            entitySelectorParser.setSuggestions((suggestionsBuilder, consumer) -> {
                String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
                boolean bl = !entitySelectorParser.hasGamemodeNotEquals();
                boolean bl2 = true;
                if (!string.isEmpty()) {
                    if (string.charAt(0) == '!') {
                        bl = false;
                        string = string.substring(1);
                    } else {
                        bl2 = false;
                    }
                }
                for (GameType gameType : GameType.values()) {
                    if (!gameType.getName().toLowerCase(Locale.ROOT).startsWith(string)) continue;
                    if (bl2) {
                        suggestionsBuilder.suggest('!' + gameType.getName());
                    }
                    if (!bl) continue;
                    suggestionsBuilder.suggest(gameType.getName());
                }
                return suggestionsBuilder.buildFuture();
            });
            int i = entitySelectorParser.getReader().getCursor();
            boolean bl = entitySelectorParser.shouldInvertValue();
            if (entitySelectorParser.hasGamemodeNotEquals() && !bl) {
                entitySelectorParser.getReader().setCursor(i);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), "gamemode");
            }
            String string = entitySelectorParser.getReader().readUnquotedString();
            GameType gameType = GameType.byName(string, null);
            if (gameType == null) {
                entitySelectorParser.getReader().setCursor(i);
                throw ERROR_GAME_MODE_INVALID.createWithContext(entitySelectorParser.getReader(), string);
            }
            entitySelectorParser.setIncludesEntities(false);
            entitySelectorParser.addPredicate(entity -> {
                if (!(entity instanceof ServerPlayer)) {
                    return false;
                }
                GameType gameType2 = ((ServerPlayer)entity).gameMode.getGameModeForPlayer();
                return bl ? gameType2 != gameType : gameType2 == gameType;
            });
            if (bl) {
                entitySelectorParser.setHasGamemodeNotEquals(true);
            } else {
                entitySelectorParser.setHasGamemodeEquals(true);
            }
        }, entitySelectorParser -> !entitySelectorParser.hasGamemodeEquals(), new TranslatableComponent("argument.entity.options.gamemode.description"));
        EntitySelectorOptions.register("team", entitySelectorParser -> {
            boolean bl = entitySelectorParser.shouldInvertValue();
            String string = entitySelectorParser.getReader().readUnquotedString();
            entitySelectorParser.addPredicate(entity -> {
                if (!(entity instanceof LivingEntity)) {
                    return false;
                }
                Team team = entity.getTeam();
                String string2 = team == null ? "" : team.getName();
                return string2.equals(string) != bl;
            });
            if (bl) {
                entitySelectorParser.setHasTeamNotEquals(true);
            } else {
                entitySelectorParser.setHasTeamEquals(true);
            }
        }, entitySelectorParser -> !entitySelectorParser.hasTeamEquals(), new TranslatableComponent("argument.entity.options.team.description"));
        EntitySelectorOptions.register("type", entitySelectorParser -> {
            entitySelectorParser.setSuggestions((suggestionsBuilder, consumer) -> {
                SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), suggestionsBuilder, String.valueOf('!'));
                SharedSuggestionProvider.suggestResource(EntityTypeTags.getAllTags().getAvailableTags(), suggestionsBuilder, "!#");
                if (!entitySelectorParser.isTypeLimitedInversely()) {
                    SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), suggestionsBuilder);
                    SharedSuggestionProvider.suggestResource(EntityTypeTags.getAllTags().getAvailableTags(), suggestionsBuilder, String.valueOf('#'));
                }
                return suggestionsBuilder.buildFuture();
            });
            int i = entitySelectorParser.getReader().getCursor();
            boolean bl = entitySelectorParser.shouldInvertValue();
            if (entitySelectorParser.isTypeLimitedInversely() && !bl) {
                entitySelectorParser.getReader().setCursor(i);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), "type");
            }
            if (bl) {
                entitySelectorParser.setTypeLimitedInversely();
            }
            if (entitySelectorParser.isTag()) {
                ResourceLocation resourceLocation = ResourceLocation.read(entitySelectorParser.getReader());
                entitySelectorParser.addPredicate(entity -> entity.getType().is(entity.getServer().getTags().getOrEmpty(Registry.ENTITY_TYPE_REGISTRY).getTagOrEmpty(resourceLocation)) != bl);
            } else {
                ResourceLocation resourceLocation = ResourceLocation.read(entitySelectorParser.getReader());
                EntityType<?> entityType = Registry.ENTITY_TYPE.getOptional(resourceLocation).orElseThrow(() -> {
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
        }, entitySelectorParser -> !entitySelectorParser.isTypeLimited(), new TranslatableComponent("argument.entity.options.type.description"));
        EntitySelectorOptions.register("tag", entitySelectorParser -> {
            boolean bl = entitySelectorParser.shouldInvertValue();
            String string = entitySelectorParser.getReader().readUnquotedString();
            entitySelectorParser.addPredicate(entity -> {
                if ("".equals(string)) {
                    return entity.getTags().isEmpty() != bl;
                }
                return entity.getTags().contains(string) != bl;
            });
        }, entitySelectorParser -> true, new TranslatableComponent("argument.entity.options.tag.description"));
        EntitySelectorOptions.register("nbt", entitySelectorParser -> {
            boolean bl = entitySelectorParser.shouldInvertValue();
            CompoundTag compoundTag = new TagParser(entitySelectorParser.getReader()).readStruct();
            entitySelectorParser.addPredicate(entity -> {
                ItemStack itemStack;
                CompoundTag compoundTag2 = entity.saveWithoutId(new CompoundTag());
                if (entity instanceof ServerPlayer && !(itemStack = ((ServerPlayer)entity).getInventory().getSelected()).isEmpty()) {
                    compoundTag2.put("SelectedItem", itemStack.save(new CompoundTag()));
                }
                return NbtUtils.compareNbt(compoundTag, compoundTag2, true) != bl;
            });
        }, entitySelectorParser -> true, new TranslatableComponent("argument.entity.options.nbt.description"));
        EntitySelectorOptions.register("scores", entitySelectorParser -> {
            StringReader stringReader = entitySelectorParser.getReader();
            HashMap<String, MinMaxBounds.Ints> map = Maps.newHashMap();
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
                if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                stringReader.skip();
            }
            stringReader.expect('}');
            if (!map.isEmpty()) {
                entitySelectorParser.addPredicate(entity -> {
                    ServerScoreboard scoreboard = entity.getServer().getScoreboard();
                    String string = entity.getScoreboardName();
                    for (Map.Entry entry : map.entrySet()) {
                        Objective objective = scoreboard.getObjective((String)entry.getKey());
                        if (objective == null) {
                            return false;
                        }
                        if (!scoreboard.hasPlayerScore(string, objective)) {
                            return false;
                        }
                        Score score = scoreboard.getOrCreatePlayerScore(string, objective);
                        int i = score.getScore();
                        if (((MinMaxBounds.Ints)entry.getValue()).matches(i)) continue;
                        return false;
                    }
                    return true;
                });
            }
            entitySelectorParser.setHasScores(true);
        }, entitySelectorParser -> !entitySelectorParser.hasScores(), new TranslatableComponent("argument.entity.options.scores.description"));
        EntitySelectorOptions.register("advancements", entitySelectorParser -> {
            StringReader stringReader = entitySelectorParser.getReader();
            HashMap<ResourceLocation, Predicate<AdvancementProgress>> map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != '}') {
                stringReader.skipWhitespace();
                ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                if (stringReader.canRead() && stringReader.peek() == '{') {
                    HashMap<String, Predicate<CriterionProgress>> map2 = Maps.newHashMap();
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
                        map2.put(string, criterionProgress -> criterionProgress.isDone() == bl);
                        stringReader.skipWhitespace();
                        if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                        stringReader.skip();
                    }
                    stringReader.skipWhitespace();
                    stringReader.expect('}');
                    stringReader.skipWhitespace();
                    map.put(resourceLocation, advancementProgress -> {
                        for (Map.Entry entry : map2.entrySet()) {
                            CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
                            if (criterionProgress != null && ((Predicate)entry.getValue()).test(criterionProgress)) continue;
                            return false;
                        }
                        return true;
                    });
                } else {
                    boolean bl2 = stringReader.readBoolean();
                    map.put(resourceLocation, advancementProgress -> advancementProgress.isDone() == bl2);
                }
                stringReader.skipWhitespace();
                if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                stringReader.skip();
            }
            stringReader.expect('}');
            if (!map.isEmpty()) {
                entitySelectorParser.addPredicate(entity -> {
                    if (!(entity instanceof ServerPlayer)) {
                        return false;
                    }
                    ServerPlayer serverPlayer = (ServerPlayer)entity;
                    PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
                    ServerAdvancementManager serverAdvancementManager = serverPlayer.getServer().getAdvancements();
                    for (Map.Entry entry : map.entrySet()) {
                        Advancement advancement = serverAdvancementManager.getAdvancement((ResourceLocation)entry.getKey());
                        if (advancement != null && ((Predicate)entry.getValue()).test(playerAdvancements.getOrStartProgress(advancement))) continue;
                        return false;
                    }
                    return true;
                });
                entitySelectorParser.setIncludesEntities(false);
            }
            entitySelectorParser.setHasAdvancements(true);
        }, entitySelectorParser -> !entitySelectorParser.hasAdvancements(), new TranslatableComponent("argument.entity.options.advancements.description"));
        EntitySelectorOptions.register("predicate", entitySelectorParser -> {
            boolean bl = entitySelectorParser.shouldInvertValue();
            ResourceLocation resourceLocation = ResourceLocation.read(entitySelectorParser.getReader());
            entitySelectorParser.addPredicate(entity -> {
                if (!(entity.level instanceof ServerLevel)) {
                    return false;
                }
                ServerLevel serverLevel = (ServerLevel)entity.level;
                LootItemCondition lootItemCondition = serverLevel.getServer().getPredicateManager().get(resourceLocation);
                if (lootItemCondition == null) {
                    return false;
                }
                LootContext lootContext = new LootContext.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position()).create(LootContextParamSets.SELECTOR);
                return bl ^ lootItemCondition.test(lootContext);
            });
        }, entitySelectorParser -> true, new TranslatableComponent("argument.entity.options.predicate.description"));
    }

    public static Modifier get(EntitySelectorParser entitySelectorParser, String string, int i) throws CommandSyntaxException {
        Option option = OPTIONS.get(string);
        if (option != null) {
            if (option.predicate.test(entitySelectorParser)) {
                return option.modifier;
            }
            throw ERROR_INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), string);
        }
        entitySelectorParser.getReader().setCursor(i);
        throw ERROR_UNKNOWN_OPTION.createWithContext(entitySelectorParser.getReader(), string);
    }

    public static void suggestNames(EntitySelectorParser entitySelectorParser, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Option> entry : OPTIONS.entrySet()) {
            if (!entry.getValue().predicate.test(entitySelectorParser) || !entry.getKey().toLowerCase(Locale.ROOT).startsWith(string)) continue;
            suggestionsBuilder.suggest(entry.getKey() + '=', (Message)entry.getValue().description);
        }
    }

    static class Option {
        public final Modifier modifier;
        public final Predicate<EntitySelectorParser> predicate;
        public final Component description;

        private Option(Modifier modifier, Predicate<EntitySelectorParser> predicate, Component component) {
            this.modifier = modifier;
            this.predicate = predicate;
            this.description = component;
        }
    }

    public static interface Modifier {
        public void handle(EntitySelectorParser var1) throws CommandSyntaxException;
    }
}

