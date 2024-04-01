package net.minecraft.world.level;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicLike;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class GameRules {
	public static final int DEFAULT_RANDOM_TICK_SPEED = 3;
	static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES = Maps.newTreeMap(Comparator.comparing(key -> key.id));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOFIRETICK = register(
		"doFireTick", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_MOBGRIEFING = register(
		"mobGriefing", GameRules.Category.MOBS, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_KEEPINVENTORY = register(
		"keepInventory", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBSPAWNING = register(
		"doMobSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBLOOT = register("doMobLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_PROJECTILESCANBREAKBLOCKS = register(
		"projectilesCanBreakBlocks", GameRules.Category.DROPS, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOBLOCKDROPS = register(
		"doTileDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOENTITYDROPS = register(
		"doEntityDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_COMMANDBLOCKOUTPUT = register(
		"commandBlockOutput", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_NATURAL_REGENERATION = register(
		"naturalRegeneration", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DAYLIGHT = register(
		"doDaylightCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_LOGADMINCOMMANDS = register(
		"logAdminCommands", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_SHOWDEATHMESSAGES = register(
		"showDeathMessages", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_RANDOMTICKING = register(
		"randomTickSpeed", GameRules.Category.UPDATES, GameRules.IntegerValue.create(3)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_SENDCOMMANDFEEDBACK = register(
		"sendCommandFeedback", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_REDUCEDDEBUGINFO = register(
		"reducedDebugInfo", GameRules.Category.MISC, GameRules.BooleanValue.create(false, (minecraftServer, booleanValue) -> {
			byte b = (byte)(booleanValue.get() ? 22 : 23);

			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, b));
			}
		})
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_SPECTATORSGENERATECHUNKS = register(
		"spectatorsGenerateChunks", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_SPAWN_RADIUS = register(
		"spawnRadius", GameRules.Category.PLAYER, GameRules.IntegerValue.create(10)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = register(
		"disableElytraMovementCheck", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_ENTITY_CRAMMING = register(
		"maxEntityCramming", GameRules.Category.MOBS, GameRules.IntegerValue.create(24)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_WEATHER_CYCLE = register(
		"doWeatherCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_LIMITED_CRAFTING = register(
		"doLimitedCrafting", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (minecraftServer, booleanValue) -> {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.LIMITED_CRAFTING, booleanValue.get() ? 1.0F : 0.0F));
			}
		})
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH = register(
		"maxCommandChainLength", GameRules.Category.MISC, GameRules.IntegerValue.create(65536)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_COMMAND_FORK_COUNT = register(
		"maxCommandForkCount", GameRules.Category.MISC, GameRules.IntegerValue.create(65536)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_COMMAND_MODIFICATION_BLOCK_LIMIT = register(
		"commandModificationBlockLimit", GameRules.Category.MISC, GameRules.IntegerValue.create(32768)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_FLOATATER_SIZE_LIMIT = register(
		"floataterSizeLimit", GameRules.Category.MISC, GameRules.IntegerValue.create(32)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS = register(
		"announceAdvancements", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_RAIDS = register(
		"disableRaids", GameRules.Category.MOBS, GameRules.BooleanValue.create(false)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOINSOMNIA = register(
		"doInsomnia", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_IMMEDIATE_RESPAWN = register(
		"doImmediateRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (minecraftServer, booleanValue) -> {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, booleanValue.get() ? 1.0F : 0.0F));
			}
		})
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = register(
		"playersNetherPortalDefaultDelay", GameRules.Category.PLAYER, GameRules.IntegerValue.create(80)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = register(
		"playersNetherPortalCreativeDelay", GameRules.Category.PLAYER, GameRules.IntegerValue.create(1)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DROWNING_DAMAGE = register(
		"drowningDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_FALL_DAMAGE = register(
		"fallDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_FIRE_DAMAGE = register(
		"fireDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_FREEZE_DAMAGE = register(
		"freezeDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_PATROL_SPAWNING = register(
		"doPatrolSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_TRADER_SPAWNING = register(
		"doTraderSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_WARDEN_SPAWNING = register(
		"doWardenSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_FORGIVE_DEAD_PLAYERS = register(
		"forgiveDeadPlayers", GameRules.Category.MOBS, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_UNIVERSAL_ANGER = register(
		"universalAnger", GameRules.Category.MOBS, GameRules.BooleanValue.create(false)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_PLAYERS_SLEEPING_PERCENTAGE = register(
		"playersSleepingPercentage", GameRules.Category.PLAYER, GameRules.IntegerValue.create(100)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_BLOCK_EXPLOSION_DROP_DECAY = register(
		"blockExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_MOB_EXPLOSION_DROP_DECAY = register(
		"mobExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_TNT_EXPLOSION_DROP_DECAY = register(
		"tntExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(false)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_SNOW_ACCUMULATION_HEIGHT = register(
		"snowAccumulationHeight", GameRules.Category.UPDATES, GameRules.IntegerValue.create(1)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_WATER_SOURCE_CONVERSION = register(
		"waterSourceConversion", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_LAVA_SOURCE_CONVERSION = register(
		"lavaSourceConversion", GameRules.Category.UPDATES, GameRules.BooleanValue.create(false)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_GLOBAL_SOUND_EVENTS = register(
		"globalSoundEvents", GameRules.Category.MISC, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_VINES_SPREAD = register(
		"doVinesSpread", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_ENDER_PEARLS_VANISH_ON_DEATH = register(
		"enderPearlsVanishOnDeath", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_SPAWN_CHUNK_RADIUS = register(
		"spawnChunkRadius", GameRules.Category.MISC, GameRules.IntegerValue.create(2, 0, 32, (minecraftServer, integerValue) -> {
			ServerLevel serverLevel = minecraftServer.overworld();
			serverLevel.setDefaultSpawnPos(serverLevel.getSharedSpawnPos(), serverLevel.getSharedSpawnAngle());
		})
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_NEVER_EAT_ARMOR = register(
		"neverEatArmor", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)
	);
	private final Map<GameRules.Key<?>, GameRules.Value<?>> rules;

	private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String string, GameRules.Category category, GameRules.Type<T> type) {
		GameRules.Key<T> key = new GameRules.Key<>(string, category);
		GameRules.Type<?> type2 = (GameRules.Type<?>)GAME_RULE_TYPES.put(key, type);
		if (type2 != null) {
			throw new IllegalStateException("Duplicate game rule registration for " + string);
		} else {
			return key;
		}
	}

	public GameRules(DynamicLike<?> dynamicLike) {
		this();
		this.loadFromTag(dynamicLike);
	}

	public GameRules() {
		this.rules = (Map<GameRules.Key<?>, GameRules.Value<?>>)GAME_RULE_TYPES.entrySet()
			.stream()
			.collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> ((GameRules.Type)entry.getValue()).createRule()));
	}

	private GameRules(Map<GameRules.Key<?>, GameRules.Value<?>> map) {
		this.rules = map;
	}

	public <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> key) {
		return (T)this.rules.get(key);
	}

	public CompoundTag createTag() {
		CompoundTag compoundTag = new CompoundTag();
		this.rules.forEach((key, value) -> compoundTag.putString(key.id, value.serialize()));
		return compoundTag;
	}

	private void loadFromTag(DynamicLike<?> dynamicLike) {
		this.rules.forEach((key, value) -> dynamicLike.get(key.id).asString().result().ifPresent(value::deserialize));
	}

	public GameRules copy() {
		return new GameRules(
			(Map<GameRules.Key<?>, GameRules.Value<?>>)this.rules
				.entrySet()
				.stream()
				.collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> ((GameRules.Value)entry.getValue()).copy()))
		);
	}

	public static void visitGameRuleTypes(GameRules.GameRuleTypeVisitor gameRuleTypeVisitor) {
		GAME_RULE_TYPES.forEach((key, type) -> callVisitorCap(gameRuleTypeVisitor, key, type));
	}

	private static <T extends GameRules.Value<T>> void callVisitorCap(
		GameRules.GameRuleTypeVisitor gameRuleTypeVisitor, GameRules.Key<?> key, GameRules.Type<?> type
	) {
		gameRuleTypeVisitor.visit(key, type);
		type.callVisitor(gameRuleTypeVisitor, key);
	}

	public void assignFrom(GameRules gameRules, @Nullable MinecraftServer minecraftServer) {
		gameRules.rules.keySet().forEach(key -> this.assignCap(key, gameRules, minecraftServer));
	}

	private <T extends GameRules.Value<T>> void assignCap(GameRules.Key<T> key, GameRules gameRules, @Nullable MinecraftServer minecraftServer) {
		T value = gameRules.getRule(key);
		this.<T>getRule(key).setFrom(value, minecraftServer);
	}

	public boolean getBoolean(GameRules.Key<GameRules.BooleanValue> key) {
		return this.getRule(key).get();
	}

	public int getInt(GameRules.Key<GameRules.IntegerValue> key) {
		return this.getRule(key).get();
	}

	public static class BooleanValue extends GameRules.Value<GameRules.BooleanValue> {
		private boolean value;

		static GameRules.Type<GameRules.BooleanValue> create(boolean bl, BiConsumer<MinecraftServer, GameRules.BooleanValue> biConsumer) {
			return new GameRules.Type<>(BoolArgumentType::bool, type -> new GameRules.BooleanValue(type, bl), biConsumer, GameRules.GameRuleTypeVisitor::visitBoolean);
		}

		static GameRules.Type<GameRules.BooleanValue> create(boolean bl) {
			return create(bl, (minecraftServer, booleanValue) -> {
			});
		}

		public BooleanValue(GameRules.Type<GameRules.BooleanValue> type, boolean bl) {
			super(type);
			this.value = bl;
		}

		@Override
		protected void updateFromArgument(CommandContext<CommandSourceStack> commandContext, String string) {
			this.value = BoolArgumentType.getBool(commandContext, string);
		}

		public boolean get() {
			return this.value;
		}

		public void set(boolean bl, @Nullable MinecraftServer minecraftServer) {
			this.value = bl;
			this.onChanged(minecraftServer);
		}

		@Override
		public String serialize() {
			return Boolean.toString(this.value);
		}

		@Override
		protected void deserialize(String string) {
			this.value = Boolean.parseBoolean(string);
		}

		@Override
		public int getCommandResult() {
			return this.value ? 1 : 0;
		}

		protected GameRules.BooleanValue getSelf() {
			return this;
		}

		protected GameRules.BooleanValue copy() {
			return new GameRules.BooleanValue(this.type, this.value);
		}

		public void setFrom(GameRules.BooleanValue booleanValue, @Nullable MinecraftServer minecraftServer) {
			this.value = booleanValue.value;
			this.onChanged(minecraftServer);
		}
	}

	public static enum Category {
		PLAYER("gamerule.category.player"),
		MOBS("gamerule.category.mobs"),
		SPAWNING("gamerule.category.spawning"),
		DROPS("gamerule.category.drops"),
		UPDATES("gamerule.category.updates"),
		CHAT("gamerule.category.chat"),
		MISC("gamerule.category.misc");

		private final String descriptionId;

		private Category(String string2) {
			this.descriptionId = string2;
		}

		public String getDescriptionId() {
			return this.descriptionId;
		}
	}

	public interface GameRuleTypeVisitor {
		default <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
		}

		default void visitBoolean(GameRules.Key<GameRules.BooleanValue> key, GameRules.Type<GameRules.BooleanValue> type) {
		}

		default void visitInteger(GameRules.Key<GameRules.IntegerValue> key, GameRules.Type<GameRules.IntegerValue> type) {
		}
	}

	public static class IntegerValue extends GameRules.Value<GameRules.IntegerValue> {
		private int value;

		private static GameRules.Type<GameRules.IntegerValue> create(int i, BiConsumer<MinecraftServer, GameRules.IntegerValue> biConsumer) {
			return new GameRules.Type<>(
				IntegerArgumentType::integer, type -> new GameRules.IntegerValue(type, i), biConsumer, GameRules.GameRuleTypeVisitor::visitInteger
			);
		}

		static GameRules.Type<GameRules.IntegerValue> create(int i, int j, int k, BiConsumer<MinecraftServer, GameRules.IntegerValue> biConsumer) {
			return new GameRules.Type<>(
				() -> IntegerArgumentType.integer(j, k), type -> new GameRules.IntegerValue(type, i), biConsumer, GameRules.GameRuleTypeVisitor::visitInteger
			);
		}

		static GameRules.Type<GameRules.IntegerValue> create(int i) {
			return create(i, (minecraftServer, integerValue) -> {
			});
		}

		public IntegerValue(GameRules.Type<GameRules.IntegerValue> type, int i) {
			super(type);
			this.value = i;
		}

		@Override
		protected void updateFromArgument(CommandContext<CommandSourceStack> commandContext, String string) {
			this.value = IntegerArgumentType.getInteger(commandContext, string);
		}

		public int get() {
			return this.value;
		}

		public void set(int i, @Nullable MinecraftServer minecraftServer) {
			this.value = i;
			this.onChanged(minecraftServer);
		}

		@Override
		public String serialize() {
			return Integer.toString(this.value);
		}

		@Override
		protected void deserialize(String string) {
			this.value = safeParse(string);
		}

		public boolean tryDeserialize(String string) {
			try {
				StringReader stringReader = new StringReader(string);
				this.value = (Integer)((ArgumentType)this.type.argument.get()).parse(stringReader);
				return !stringReader.canRead();
			} catch (CommandSyntaxException var3) {
				return false;
			}
		}

		private static int safeParse(String string) {
			if (!string.isEmpty()) {
				try {
					return Integer.parseInt(string);
				} catch (NumberFormatException var2) {
					GameRules.LOGGER.warn("Failed to parse integer {}", string);
				}
			}

			return 0;
		}

		@Override
		public int getCommandResult() {
			return this.value;
		}

		protected GameRules.IntegerValue getSelf() {
			return this;
		}

		protected GameRules.IntegerValue copy() {
			return new GameRules.IntegerValue(this.type, this.value);
		}

		public void setFrom(GameRules.IntegerValue integerValue, @Nullable MinecraftServer minecraftServer) {
			this.value = integerValue.value;
			this.onChanged(minecraftServer);
		}
	}

	public static final class Key<T extends GameRules.Value<T>> {
		final String id;
		private final GameRules.Category category;

		public Key(String string, GameRules.Category category) {
			this.id = string;
			this.category = category;
		}

		public String toString() {
			return this.id;
		}

		public boolean equals(Object object) {
			return this == object ? true : object instanceof GameRules.Key && ((GameRules.Key)object).id.equals(this.id);
		}

		public int hashCode() {
			return this.id.hashCode();
		}

		public String getId() {
			return this.id;
		}

		public String getDescriptionId() {
			return "gamerule." + this.id;
		}

		public GameRules.Category getCategory() {
			return this.category;
		}
	}

	public static class Type<T extends GameRules.Value<T>> {
		final Supplier<ArgumentType<?>> argument;
		private final Function<GameRules.Type<T>, T> constructor;
		final BiConsumer<MinecraftServer, T> callback;
		private final GameRules.VisitorCaller<T> visitorCaller;

		Type(
			Supplier<ArgumentType<?>> supplier,
			Function<GameRules.Type<T>, T> function,
			BiConsumer<MinecraftServer, T> biConsumer,
			GameRules.VisitorCaller<T> visitorCaller
		) {
			this.argument = supplier;
			this.constructor = function;
			this.callback = biConsumer;
			this.visitorCaller = visitorCaller;
		}

		public RequiredArgumentBuilder<CommandSourceStack, ?> createArgument(String string) {
			return Commands.argument(string, (ArgumentType<T>)this.argument.get());
		}

		public T createRule() {
			return (T)this.constructor.apply(this);
		}

		public void callVisitor(GameRules.GameRuleTypeVisitor gameRuleTypeVisitor, GameRules.Key<T> key) {
			this.visitorCaller.call(gameRuleTypeVisitor, key, this);
		}
	}

	public abstract static class Value<T extends GameRules.Value<T>> {
		protected final GameRules.Type<T> type;

		public Value(GameRules.Type<T> type) {
			this.type = type;
		}

		protected abstract void updateFromArgument(CommandContext<CommandSourceStack> commandContext, String string);

		public void setFromArgument(CommandContext<CommandSourceStack> commandContext, String string) {
			this.updateFromArgument(commandContext, string);
			this.onChanged(commandContext.getSource().getServer());
		}

		protected void onChanged(@Nullable MinecraftServer minecraftServer) {
			if (minecraftServer != null) {
				this.type.callback.accept(minecraftServer, this.getSelf());
			}
		}

		protected abstract void deserialize(String string);

		public abstract String serialize();

		public String toString() {
			return this.serialize();
		}

		public abstract int getCommandResult();

		protected abstract T getSelf();

		protected abstract T copy();

		public abstract void setFrom(T value, @Nullable MinecraftServer minecraftServer);
	}

	interface VisitorCaller<T extends GameRules.Value<T>> {
		void call(GameRules.GameRuleTypeVisitor gameRuleTypeVisitor, GameRules.Key<T> key, GameRules.Type<T> type);
	}
}
