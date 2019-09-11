package net.minecraft.world.level;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameRules {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES = Maps.newTreeMap(Comparator.comparing(key -> key.id));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOFIRETICK = register("doFireTick", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_MOBGRIEFING = register("mobGriefing", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_KEEPINVENTORY = register("keepInventory", GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBSPAWNING = register("doMobSpawning", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBLOOT = register("doMobLoot", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOBLOCKDROPS = register("doTileDrops", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOENTITYDROPS = register("doEntityDrops", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_COMMANDBLOCKOUTPUT = register("commandBlockOutput", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_NATURAL_REGENERATION = register("naturalRegeneration", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DAYLIGHT = register("doDaylightCycle", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_LOGADMINCOMMANDS = register("logAdminCommands", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_SHOWDEATHMESSAGES = register("showDeathMessages", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_RANDOMTICKING = register("randomTickSpeed", GameRules.IntegerValue.create(3));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_SENDCOMMANDFEEDBACK = register("sendCommandFeedback", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_REDUCEDDEBUGINFO = register(
		"reducedDebugInfo", GameRules.BooleanValue.create(false, (minecraftServer, booleanValue) -> {
			byte b = (byte)(booleanValue.get() ? 22 : 23);

			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, b));
			}
		})
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_SPECTATORSGENERATECHUNKS = register(
		"spectatorsGenerateChunks", GameRules.BooleanValue.create(true)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_SPAWN_RADIUS = register("spawnRadius", GameRules.IntegerValue.create(10));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = register(
		"disableElytraMovementCheck", GameRules.BooleanValue.create(false)
	);
	public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_ENTITY_CRAMMING = register("maxEntityCramming", GameRules.IntegerValue.create(24));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_WEATHER_CYCLE = register("doWeatherCycle", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_LIMITED_CRAFTING = register("doLimitedCrafting", GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH = register(
		"maxCommandChainLength", GameRules.IntegerValue.create(65536)
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS = register("announceAdvancements", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_RAIDS = register("disableRaids", GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DOINSOMNIA = register("doInsomnia", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_IMMEDIATE_RESPAWN = register(
		"doImmediateRespawn", GameRules.BooleanValue.create(false, (minecraftServer, booleanValue) -> {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				serverPlayer.connection.send(new ClientboundGameEventPacket(11, booleanValue.get() ? 1.0F : 0.0F));
			}
		})
	);
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DROWNING_DAMAGE = register("drowningDamage", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_FALL_DAMAGE = register("fallDamage", GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_FIRE_DAMAGE = register("fireDamage", GameRules.BooleanValue.create(true));
	private final Map<GameRules.Key<?>, GameRules.Value<?>> rules = (Map<GameRules.Key<?>, GameRules.Value<?>>)GAME_RULE_TYPES.entrySet()
		.stream()
		.collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> ((GameRules.Type)entry.getValue()).createRule()));

	private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String string, GameRules.Type<T> type) {
		GameRules.Key<T> key = new GameRules.Key<>(string);
		GameRules.Type<?> type2 = (GameRules.Type<?>)GAME_RULE_TYPES.put(key, type);
		if (type2 != null) {
			throw new IllegalStateException("Duplicate game rule registration for " + string);
		} else {
			return key;
		}
	}

	public <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> key) {
		return (T)this.rules.get(key);
	}

	public CompoundTag createTag() {
		CompoundTag compoundTag = new CompoundTag();
		this.rules.forEach((key, value) -> compoundTag.putString(key.id, value.serialize()));
		return compoundTag;
	}

	public void loadFromTag(CompoundTag compoundTag) {
		this.rules.forEach((key, value) -> {
			if (compoundTag.contains(key.id)) {
				value.deserialize(compoundTag.getString(key.id));
			}
		});
	}

	public static void visitGameRuleTypes(GameRules.GameRuleTypeVisitor gameRuleTypeVisitor) {
		GAME_RULE_TYPES.forEach((key, type) -> cap(gameRuleTypeVisitor, key, type));
	}

	private static <T extends GameRules.Value<T>> void cap(GameRules.GameRuleTypeVisitor gameRuleTypeVisitor, GameRules.Key<?> key, GameRules.Type<?> type) {
		gameRuleTypeVisitor.visit(key, type);
	}

	public boolean getBoolean(GameRules.Key<GameRules.BooleanValue> key) {
		return this.getRule(key).get();
	}

	public int getInt(GameRules.Key<GameRules.IntegerValue> key) {
		return this.getRule(key).get();
	}

	public static class BooleanValue extends GameRules.Value<GameRules.BooleanValue> {
		private boolean value;

		private static GameRules.Type<GameRules.BooleanValue> create(boolean bl, BiConsumer<MinecraftServer, GameRules.BooleanValue> biConsumer) {
			return new GameRules.Type<>(BoolArgumentType::bool, type -> new GameRules.BooleanValue(type, bl), biConsumer);
		}

		private static GameRules.Type<GameRules.BooleanValue> create(boolean bl) {
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
		protected String serialize() {
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
	}

	@FunctionalInterface
	public interface GameRuleTypeVisitor {
		<T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type);
	}

	public static class IntegerValue extends GameRules.Value<GameRules.IntegerValue> {
		private int value;

		private static GameRules.Type<GameRules.IntegerValue> create(int i, BiConsumer<MinecraftServer, GameRules.IntegerValue> biConsumer) {
			return new GameRules.Type<>(IntegerArgumentType::integer, type -> new GameRules.IntegerValue(type, i), biConsumer);
		}

		private static GameRules.Type<GameRules.IntegerValue> create(int i) {
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

		@Override
		protected String serialize() {
			return Integer.toString(this.value);
		}

		@Override
		protected void deserialize(String string) {
			this.value = safeParse(string);
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
	}

	public static final class Key<T extends GameRules.Value<T>> {
		private final String id;

		public Key(String string) {
			this.id = string;
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
	}

	public static class Type<T extends GameRules.Value<T>> {
		private final Supplier<ArgumentType<?>> argument;
		private final Function<GameRules.Type<T>, T> constructor;
		private final BiConsumer<MinecraftServer, T> callback;

		private Type(Supplier<ArgumentType<?>> supplier, Function<GameRules.Type<T>, T> function, BiConsumer<MinecraftServer, T> biConsumer) {
			this.argument = supplier;
			this.constructor = function;
			this.callback = biConsumer;
		}

		public RequiredArgumentBuilder<CommandSourceStack, ?> createArgument(String string) {
			return Commands.argument(string, (ArgumentType<T>)this.argument.get());
		}

		public T createRule() {
			return (T)this.constructor.apply(this);
		}
	}

	public abstract static class Value<T extends GameRules.Value<T>> {
		private final GameRules.Type<T> type;

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

		protected abstract String serialize();

		public String toString() {
			return this.serialize();
		}

		public abstract int getCommandResult();

		protected abstract T getSelf();
	}
}
