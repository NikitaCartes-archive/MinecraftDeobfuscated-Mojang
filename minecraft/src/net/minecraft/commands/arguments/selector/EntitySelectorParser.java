package net.minecraft.commands.arguments.selector;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntitySelectorParser {
	public static final char SYNTAX_SELECTOR_START = '@';
	private static final char SYNTAX_OPTIONS_START = '[';
	private static final char SYNTAX_OPTIONS_END = ']';
	public static final char SYNTAX_OPTIONS_KEY_VALUE_SEPARATOR = '=';
	private static final char SYNTAX_OPTIONS_SEPARATOR = ',';
	public static final char SYNTAX_NOT = '!';
	public static final char SYNTAX_TAG = '#';
	private static final char SELECTOR_NEAREST_PLAYER = 'p';
	private static final char SELECTOR_ALL_PLAYERS = 'a';
	private static final char SELECTOR_RANDOM_PLAYERS = 'r';
	private static final char SELECTOR_CURRENT_ENTITY = 's';
	private static final char SELECTOR_ALL_ENTITIES = 'e';
	private static final char SELECTOR_NEAREST_ENTITY = 'n';
	public static final SimpleCommandExceptionType ERROR_INVALID_NAME_OR_UUID = new SimpleCommandExceptionType(Component.translatable("argument.entity.invalid"));
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_SELECTOR_TYPE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.entity.selector.unknown", object)
	);
	public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(
		Component.translatable("argument.entity.selector.not_allowed")
	);
	public static final SimpleCommandExceptionType ERROR_MISSING_SELECTOR_TYPE = new SimpleCommandExceptionType(
		Component.translatable("argument.entity.selector.missing")
	);
	public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_OPTIONS = new SimpleCommandExceptionType(
		Component.translatable("argument.entity.options.unterminated")
	);
	public static final DynamicCommandExceptionType ERROR_EXPECTED_OPTION_VALUE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.entity.options.valueless", object)
	);
	public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_NEAREST = (vec3, list) -> list.sort(
			(entity, entity2) -> Doubles.compare(entity.distanceToSqr(vec3), entity2.distanceToSqr(vec3))
		);
	public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_FURTHEST = (vec3, list) -> list.sort(
			(entity, entity2) -> Doubles.compare(entity2.distanceToSqr(vec3), entity.distanceToSqr(vec3))
		);
	public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_RANDOM = (vec3, list) -> Collections.shuffle(list);
	public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = (suggestionsBuilder, consumer) -> suggestionsBuilder.buildFuture();
	private final StringReader reader;
	private final boolean allowSelectors;
	private int maxResults;
	private boolean includesEntities;
	private boolean worldLimited;
	private MinMaxBounds.Doubles distance = MinMaxBounds.Doubles.ANY;
	private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
	@Nullable
	private Double x;
	@Nullable
	private Double y;
	@Nullable
	private Double z;
	@Nullable
	private Double deltaX;
	@Nullable
	private Double deltaY;
	@Nullable
	private Double deltaZ;
	private WrappedMinMaxBounds rotX = WrappedMinMaxBounds.ANY;
	private WrappedMinMaxBounds rotY = WrappedMinMaxBounds.ANY;
	private Predicate<Entity> predicate = entity -> true;
	private BiConsumer<Vec3, List<? extends Entity>> order = EntitySelector.ORDER_ARBITRARY;
	private boolean currentEntity;
	@Nullable
	private String playerName;
	private int startPosition;
	@Nullable
	private UUID entityUUID;
	private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;
	private boolean hasNameEquals;
	private boolean hasNameNotEquals;
	private boolean isLimited;
	private boolean isSorted;
	private boolean hasGamemodeEquals;
	private boolean hasGamemodeNotEquals;
	private boolean hasTeamEquals;
	private boolean hasTeamNotEquals;
	@Nullable
	private EntityType<?> type;
	private boolean typeInverse;
	private boolean hasScores;
	private boolean hasAdvancements;
	private boolean usesSelectors;

	public EntitySelectorParser(StringReader stringReader) {
		this(stringReader, true);
	}

	public EntitySelectorParser(StringReader stringReader, boolean bl) {
		this.reader = stringReader;
		this.allowSelectors = bl;
	}

	public EntitySelector getSelector() {
		AABB aABB;
		if (this.deltaX == null && this.deltaY == null && this.deltaZ == null) {
			if (this.distance.max().isPresent()) {
				double d = (Double)this.distance.max().get();
				aABB = new AABB(-d, -d, -d, d + 1.0, d + 1.0, d + 1.0);
			} else {
				aABB = null;
			}
		} else {
			aABB = this.createAabb(this.deltaX == null ? 0.0 : this.deltaX, this.deltaY == null ? 0.0 : this.deltaY, this.deltaZ == null ? 0.0 : this.deltaZ);
		}

		Function<Vec3, Vec3> function;
		if (this.x == null && this.y == null && this.z == null) {
			function = vec3 -> vec3;
		} else {
			function = vec3 -> new Vec3(this.x == null ? vec3.x : this.x, this.y == null ? vec3.y : this.y, this.z == null ? vec3.z : this.z);
		}

		return new EntitySelector(
			this.maxResults,
			this.includesEntities,
			this.worldLimited,
			this.predicate,
			this.distance,
			function,
			aABB,
			this.order,
			this.currentEntity,
			this.playerName,
			this.entityUUID,
			this.type,
			this.usesSelectors
		);
	}

	private AABB createAabb(double d, double e, double f) {
		boolean bl = d < 0.0;
		boolean bl2 = e < 0.0;
		boolean bl3 = f < 0.0;
		double g = bl ? d : 0.0;
		double h = bl2 ? e : 0.0;
		double i = bl3 ? f : 0.0;
		double j = (bl ? 0.0 : d) + 1.0;
		double k = (bl2 ? 0.0 : e) + 1.0;
		double l = (bl3 ? 0.0 : f) + 1.0;
		return new AABB(g, h, i, j, k, l);
	}

	private void finalizePredicates() {
		if (this.rotX != WrappedMinMaxBounds.ANY) {
			this.predicate = this.predicate.and(this.createRotationPredicate(this.rotX, Entity::getXRot));
		}

		if (this.rotY != WrappedMinMaxBounds.ANY) {
			this.predicate = this.predicate.and(this.createRotationPredicate(this.rotY, Entity::getYRot));
		}

		if (!this.level.isAny()) {
			this.predicate = this.predicate.and(entity -> !(entity instanceof ServerPlayer) ? false : this.level.matches(((ServerPlayer)entity).experienceLevel));
		}
	}

	private Predicate<Entity> createRotationPredicate(WrappedMinMaxBounds wrappedMinMaxBounds, ToDoubleFunction<Entity> toDoubleFunction) {
		double d = (double)Mth.wrapDegrees(wrappedMinMaxBounds.min() == null ? 0.0F : wrappedMinMaxBounds.min());
		double e = (double)Mth.wrapDegrees(wrappedMinMaxBounds.max() == null ? 359.0F : wrappedMinMaxBounds.max());
		return entity -> {
			double f = Mth.wrapDegrees(toDoubleFunction.applyAsDouble(entity));
			return d > e ? f >= d || f <= e : f >= d && f <= e;
		};
	}

	protected void parseSelector() throws CommandSyntaxException {
		this.usesSelectors = true;
		this.suggestions = this::suggestSelector;
		if (!this.reader.canRead()) {
			throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(this.reader);
		} else {
			int i = this.reader.getCursor();
			char c = this.reader.read();
			if (c == 'p') {
				this.maxResults = 1;
				this.includesEntities = false;
				this.order = ORDER_NEAREST;
				this.limitToType(EntityType.PLAYER);
			} else if (c == 'a') {
				this.maxResults = Integer.MAX_VALUE;
				this.includesEntities = false;
				this.order = EntitySelector.ORDER_ARBITRARY;
				this.limitToType(EntityType.PLAYER);
			} else if (c == 'r') {
				this.maxResults = 1;
				this.includesEntities = false;
				this.order = ORDER_RANDOM;
				this.limitToType(EntityType.PLAYER);
			} else if (c == 's') {
				this.maxResults = 1;
				this.includesEntities = true;
				this.currentEntity = true;
			} else if (c == 'e') {
				this.maxResults = Integer.MAX_VALUE;
				this.includesEntities = true;
				this.order = EntitySelector.ORDER_ARBITRARY;
				this.predicate = Entity::isAlive;
			} else {
				if (c != 'n') {
					this.reader.setCursor(i);
					throw ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(this.reader, "@" + c);
				}

				this.maxResults = 1;
				this.includesEntities = true;
				this.order = ORDER_NEAREST;
			}

			this.suggestions = this::suggestOpenOptions;
			if (this.reader.canRead() && this.reader.peek() == '[') {
				this.reader.skip();
				this.suggestions = this::suggestOptionsKeyOrClose;
				this.parseOptions();
			}
		}
	}

	protected void parseNameOrUUID() throws CommandSyntaxException {
		if (this.reader.canRead()) {
			this.suggestions = this::suggestName;
		}

		int i = this.reader.getCursor();
		String string = this.reader.readString();

		try {
			this.entityUUID = UUID.fromString(string);
			this.includesEntities = true;
		} catch (IllegalArgumentException var4) {
			if (string.isEmpty() || string.length() > 16) {
				this.reader.setCursor(i);
				throw ERROR_INVALID_NAME_OR_UUID.createWithContext(this.reader);
			}

			this.includesEntities = false;
			this.playerName = string;
		}

		this.maxResults = 1;
	}

	protected void parseOptions() throws CommandSyntaxException {
		this.suggestions = this::suggestOptionsKey;
		this.reader.skipWhitespace();

		while (this.reader.canRead() && this.reader.peek() != ']') {
			this.reader.skipWhitespace();
			int i = this.reader.getCursor();
			String string = this.reader.readString();
			EntitySelectorOptions.Modifier modifier = EntitySelectorOptions.get(this, string, i);
			this.reader.skipWhitespace();
			if (!this.reader.canRead() || this.reader.peek() != '=') {
				this.reader.setCursor(i);
				throw ERROR_EXPECTED_OPTION_VALUE.createWithContext(this.reader, string);
			}

			this.reader.skip();
			this.reader.skipWhitespace();
			this.suggestions = SUGGEST_NOTHING;
			modifier.handle(this);
			this.reader.skipWhitespace();
			this.suggestions = this::suggestOptionsNextOrClose;
			if (this.reader.canRead()) {
				if (this.reader.peek() != ',') {
					if (this.reader.peek() != ']') {
						throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
					}
					break;
				}

				this.reader.skip();
				this.suggestions = this::suggestOptionsKey;
			}
		}

		if (this.reader.canRead()) {
			this.reader.skip();
			this.suggestions = SUGGEST_NOTHING;
		} else {
			throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
		}
	}

	public boolean shouldInvertValue() {
		this.reader.skipWhitespace();
		if (this.reader.canRead() && this.reader.peek() == '!') {
			this.reader.skip();
			this.reader.skipWhitespace();
			return true;
		} else {
			return false;
		}
	}

	public boolean isTag() {
		this.reader.skipWhitespace();
		if (this.reader.canRead() && this.reader.peek() == '#') {
			this.reader.skip();
			this.reader.skipWhitespace();
			return true;
		} else {
			return false;
		}
	}

	public StringReader getReader() {
		return this.reader;
	}

	public void addPredicate(Predicate<Entity> predicate) {
		this.predicate = this.predicate.and(predicate);
	}

	public void setWorldLimited() {
		this.worldLimited = true;
	}

	public MinMaxBounds.Doubles getDistance() {
		return this.distance;
	}

	public void setDistance(MinMaxBounds.Doubles doubles) {
		this.distance = doubles;
	}

	public MinMaxBounds.Ints getLevel() {
		return this.level;
	}

	public void setLevel(MinMaxBounds.Ints ints) {
		this.level = ints;
	}

	public WrappedMinMaxBounds getRotX() {
		return this.rotX;
	}

	public void setRotX(WrappedMinMaxBounds wrappedMinMaxBounds) {
		this.rotX = wrappedMinMaxBounds;
	}

	public WrappedMinMaxBounds getRotY() {
		return this.rotY;
	}

	public void setRotY(WrappedMinMaxBounds wrappedMinMaxBounds) {
		this.rotY = wrappedMinMaxBounds;
	}

	@Nullable
	public Double getX() {
		return this.x;
	}

	@Nullable
	public Double getY() {
		return this.y;
	}

	@Nullable
	public Double getZ() {
		return this.z;
	}

	public void setX(double d) {
		this.x = d;
	}

	public void setY(double d) {
		this.y = d;
	}

	public void setZ(double d) {
		this.z = d;
	}

	public void setDeltaX(double d) {
		this.deltaX = d;
	}

	public void setDeltaY(double d) {
		this.deltaY = d;
	}

	public void setDeltaZ(double d) {
		this.deltaZ = d;
	}

	@Nullable
	public Double getDeltaX() {
		return this.deltaX;
	}

	@Nullable
	public Double getDeltaY() {
		return this.deltaY;
	}

	@Nullable
	public Double getDeltaZ() {
		return this.deltaZ;
	}

	public void setMaxResults(int i) {
		this.maxResults = i;
	}

	public void setIncludesEntities(boolean bl) {
		this.includesEntities = bl;
	}

	public BiConsumer<Vec3, List<? extends Entity>> getOrder() {
		return this.order;
	}

	public void setOrder(BiConsumer<Vec3, List<? extends Entity>> biConsumer) {
		this.order = biConsumer;
	}

	public EntitySelector parse() throws CommandSyntaxException {
		this.startPosition = this.reader.getCursor();
		this.suggestions = this::suggestNameOrSelector;
		if (this.reader.canRead() && this.reader.peek() == '@') {
			if (!this.allowSelectors) {
				throw ERROR_SELECTORS_NOT_ALLOWED.createWithContext(this.reader);
			}

			this.reader.skip();
			this.parseSelector();
		} else {
			this.parseNameOrUUID();
		}

		this.finalizePredicates();
		return this.getSelector();
	}

	private static void fillSelectorSuggestions(SuggestionsBuilder suggestionsBuilder) {
		suggestionsBuilder.suggest("@p", Component.translatable("argument.entity.selector.nearestPlayer"));
		suggestionsBuilder.suggest("@a", Component.translatable("argument.entity.selector.allPlayers"));
		suggestionsBuilder.suggest("@r", Component.translatable("argument.entity.selector.randomPlayer"));
		suggestionsBuilder.suggest("@s", Component.translatable("argument.entity.selector.self"));
		suggestionsBuilder.suggest("@e", Component.translatable("argument.entity.selector.allEntities"));
		suggestionsBuilder.suggest("@n", Component.translatable("argument.entity.selector.nearestEntity"));
	}

	private CompletableFuture<Suggestions> suggestNameOrSelector(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		consumer.accept(suggestionsBuilder);
		if (this.allowSelectors) {
			fillSelectorSuggestions(suggestionsBuilder);
		}

		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestName(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(this.startPosition);
		consumer.accept(suggestionsBuilder2);
		return suggestionsBuilder.add(suggestionsBuilder2).buildFuture();
	}

	private CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(suggestionsBuilder.getStart() - 1);
		fillSelectorSuggestions(suggestionsBuilder2);
		suggestionsBuilder.add(suggestionsBuilder2);
		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		suggestionsBuilder.suggest(String.valueOf('['));
		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		suggestionsBuilder.suggest(String.valueOf(']'));
		EntitySelectorOptions.suggestNames(this, suggestionsBuilder);
		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOptionsKey(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		EntitySelectorOptions.suggestNames(this, suggestionsBuilder);
		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOptionsNextOrClose(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		suggestionsBuilder.suggest(String.valueOf(','));
		suggestionsBuilder.suggest(String.valueOf(']'));
		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		suggestionsBuilder.suggest(String.valueOf('='));
		return suggestionsBuilder.buildFuture();
	}

	public boolean isCurrentEntity() {
		return this.currentEntity;
	}

	public void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> biFunction) {
		this.suggestions = biFunction;
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
		return (CompletableFuture<Suggestions>)this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), consumer);
	}

	public boolean hasNameEquals() {
		return this.hasNameEquals;
	}

	public void setHasNameEquals(boolean bl) {
		this.hasNameEquals = bl;
	}

	public boolean hasNameNotEquals() {
		return this.hasNameNotEquals;
	}

	public void setHasNameNotEquals(boolean bl) {
		this.hasNameNotEquals = bl;
	}

	public boolean isLimited() {
		return this.isLimited;
	}

	public void setLimited(boolean bl) {
		this.isLimited = bl;
	}

	public boolean isSorted() {
		return this.isSorted;
	}

	public void setSorted(boolean bl) {
		this.isSorted = bl;
	}

	public boolean hasGamemodeEquals() {
		return this.hasGamemodeEquals;
	}

	public void setHasGamemodeEquals(boolean bl) {
		this.hasGamemodeEquals = bl;
	}

	public boolean hasGamemodeNotEquals() {
		return this.hasGamemodeNotEquals;
	}

	public void setHasGamemodeNotEquals(boolean bl) {
		this.hasGamemodeNotEquals = bl;
	}

	public boolean hasTeamEquals() {
		return this.hasTeamEquals;
	}

	public void setHasTeamEquals(boolean bl) {
		this.hasTeamEquals = bl;
	}

	public boolean hasTeamNotEquals() {
		return this.hasTeamNotEquals;
	}

	public void setHasTeamNotEquals(boolean bl) {
		this.hasTeamNotEquals = bl;
	}

	public void limitToType(EntityType<?> entityType) {
		this.type = entityType;
	}

	public void setTypeLimitedInversely() {
		this.typeInverse = true;
	}

	public boolean isTypeLimited() {
		return this.type != null;
	}

	public boolean isTypeLimitedInversely() {
		return this.typeInverse;
	}

	public boolean hasScores() {
		return this.hasScores;
	}

	public void setHasScores(boolean bl) {
		this.hasScores = bl;
	}

	public boolean hasAdvancements() {
		return this.hasAdvancements;
	}

	public void setHasAdvancements(boolean bl) {
		this.hasAdvancements = bl;
	}
}
