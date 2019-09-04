/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class GameRules {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Key<?>, Type<?>> GAME_RULE_TYPES = Maps.newTreeMap(Comparator.comparing(key -> Key.method_20772(key)));
    public static final Key<BooleanValue> RULE_DOFIRETICK = GameRules.register("doFireTick", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_MOBGRIEFING = GameRules.register("mobGriefing", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_KEEPINVENTORY = GameRules.register("keepInventory", BooleanValue.method_20755(false));
    public static final Key<BooleanValue> RULE_DOMOBSPAWNING = GameRules.register("doMobSpawning", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_DOMOBLOOT = GameRules.register("doMobLoot", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_DOBLOCKDROPS = GameRules.register("doTileDrops", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_DOENTITYDROPS = GameRules.register("doEntityDrops", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_COMMANDBLOCKOUTPUT = GameRules.register("commandBlockOutput", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_NATURAL_REGENERATION = GameRules.register("naturalRegeneration", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_DAYLIGHT = GameRules.register("doDaylightCycle", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_LOGADMINCOMMANDS = GameRules.register("logAdminCommands", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_SHOWDEATHMESSAGES = GameRules.register("showDeathMessages", BooleanValue.method_20755(true));
    public static final Key<IntegerValue> RULE_RANDOMTICKING = GameRules.register("randomTickSpeed", IntegerValue.method_20764(3));
    public static final Key<BooleanValue> RULE_SENDCOMMANDFEEDBACK = GameRules.register("sendCommandFeedback", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_REDUCEDDEBUGINFO = GameRules.register("reducedDebugInfo", BooleanValue.method_20757(false, (minecraftServer, booleanValue) -> {
        byte b = booleanValue.get() ? (byte)22 : (byte)23;
        for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
            serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, b));
        }
    }));
    public static final Key<BooleanValue> RULE_SPECTATORSGENERATECHUNKS = GameRules.register("spectatorsGenerateChunks", BooleanValue.method_20755(true));
    public static final Key<IntegerValue> RULE_SPAWN_RADIUS = GameRules.register("spawnRadius", IntegerValue.method_20764(10));
    public static final Key<BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = GameRules.register("disableElytraMovementCheck", BooleanValue.method_20755(false));
    public static final Key<IntegerValue> RULE_MAX_ENTITY_CRAMMING = GameRules.register("maxEntityCramming", IntegerValue.method_20764(24));
    public static final Key<BooleanValue> RULE_WEATHER_CYCLE = GameRules.register("doWeatherCycle", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_LIMITED_CRAFTING = GameRules.register("doLimitedCrafting", BooleanValue.method_20755(false));
    public static final Key<IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH = GameRules.register("maxCommandChainLength", IntegerValue.method_20764(65536));
    public static final Key<BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS = GameRules.register("announceAdvancements", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_DISABLE_RAIDS = GameRules.register("disableRaids", BooleanValue.method_20755(false));
    public static final Key<BooleanValue> RULE_DOINSOMNIA = GameRules.register("doInsomnia", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_DO_IMMEDIATE_RESPAWN = GameRules.register("doImmediateRespawn", BooleanValue.method_20757(false, (minecraftServer, booleanValue) -> {
        for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
            serverPlayer.connection.send(new ClientboundGameEventPacket(11, booleanValue.get() ? 1.0f : 0.0f));
        }
    }));
    public static final Key<BooleanValue> RULE_DROWNING_DAMAGE = GameRules.register("drowningDamage", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_FALL_DAMAGE = GameRules.register("fallDamage", BooleanValue.method_20755(true));
    public static final Key<BooleanValue> RULE_FIRE_DAMAGE = GameRules.register("fireDamage", BooleanValue.method_20755(true));
    private final Map<Key<?>, Value<?>> rules = GAME_RULE_TYPES.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> ((Type)entry.getValue()).createRule()));

    private static <T extends Value<T>> Key<T> register(String string, Type<T> type) {
        Key key = new Key(string);
        Type<T> type2 = GAME_RULE_TYPES.put(key, type);
        if (type2 != null) {
            throw new IllegalStateException("Duplicate game rule registration for " + string);
        }
        return key;
    }

    public <T extends Value<T>> T getRule(Key<T> key) {
        return (T)this.rules.get(key);
    }

    public CompoundTag createTag() {
        CompoundTag compoundTag = new CompoundTag();
        this.rules.forEach((key, value) -> compoundTag.putString(((Key)key).id, value.serialize()));
        return compoundTag;
    }

    public void loadFromTag(CompoundTag compoundTag) {
        this.rules.forEach((key, value) -> value.deserialize(compoundTag.getString(((Key)key).id)));
    }

    public static void visitGameRuleTypes(GameRuleTypeVisitor gameRuleTypeVisitor) {
        GAME_RULE_TYPES.forEach((key, type) -> GameRules.cap(gameRuleTypeVisitor, key, type));
    }

    private static <T extends Value<T>> void cap(GameRuleTypeVisitor gameRuleTypeVisitor, Key<?> key, Type<?> type) {
        Key<?> key2 = key;
        Type<?> type2 = type;
        gameRuleTypeVisitor.visit(key2, type2);
    }

    public boolean getBoolean(Key<BooleanValue> key) {
        return this.getRule(key).get();
    }

    public int getInt(Key<IntegerValue> key) {
        return this.getRule(key).get();
    }

    public static class BooleanValue
    extends Value<BooleanValue> {
        private boolean value;

        private static Type<BooleanValue> create(boolean bl, BiConsumer<MinecraftServer, BooleanValue> biConsumer) {
            return new Type<BooleanValue>(BoolArgumentType::bool, type -> new BooleanValue((Type<BooleanValue>)type, bl), biConsumer);
        }

        private static Type<BooleanValue> create(boolean bl) {
            return BooleanValue.create(bl, (minecraftServer, booleanValue) -> {});
        }

        public BooleanValue(Type<BooleanValue> type, boolean bl) {
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

        @Override
        protected BooleanValue getSelf() {
            return this;
        }

        @Override
        protected /* synthetic */ Value getSelf() {
            return this.getSelf();
        }

        static /* synthetic */ Type method_20755(boolean bl) {
            return BooleanValue.create(bl);
        }

        static /* synthetic */ Type method_20757(boolean bl, BiConsumer biConsumer) {
            return BooleanValue.create(bl, biConsumer);
        }
    }

    public static class IntegerValue
    extends Value<IntegerValue> {
        private int value;

        private static Type<IntegerValue> create(int i, BiConsumer<MinecraftServer, IntegerValue> biConsumer) {
            return new Type<IntegerValue>(IntegerArgumentType::integer, type -> new IntegerValue((Type<IntegerValue>)type, i), biConsumer);
        }

        private static Type<IntegerValue> create(int i) {
            return IntegerValue.create(i, (minecraftServer, integerValue) -> {});
        }

        public IntegerValue(Type<IntegerValue> type, int i) {
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
            this.value = IntegerValue.safeParse(string);
        }

        private static int safeParse(String string) {
            if (!string.isEmpty()) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException numberFormatException) {
                    LOGGER.warn("Failed to parse integer {}", (Object)string);
                }
            }
            return 0;
        }

        @Override
        public int getCommandResult() {
            return this.value;
        }

        @Override
        protected IntegerValue getSelf() {
            return this;
        }

        @Override
        protected /* synthetic */ Value getSelf() {
            return this.getSelf();
        }

        static /* synthetic */ Type method_20764(int i) {
            return IntegerValue.create(i);
        }
    }

    public static abstract class Value<T extends Value<T>> {
        private final Type<T> type;

        public Value(Type<T> type) {
            this.type = type;
        }

        protected abstract void updateFromArgument(CommandContext<CommandSourceStack> var1, String var2);

        public void setFromArgument(CommandContext<CommandSourceStack> commandContext, String string) {
            this.updateFromArgument(commandContext, string);
            this.onChanged(commandContext.getSource().getServer());
        }

        protected void onChanged(@Nullable MinecraftServer minecraftServer) {
            if (minecraftServer != null) {
                ((Type)this.type).callback.accept(minecraftServer, this.getSelf());
            }
        }

        protected abstract void deserialize(String var1);

        protected abstract String serialize();

        public String toString() {
            return this.serialize();
        }

        public abstract int getCommandResult();

        protected abstract T getSelf();
    }

    public static class Type<T extends Value<T>> {
        private final Supplier<ArgumentType<?>> argument;
        private final Function<Type<T>, T> constructor;
        private final BiConsumer<MinecraftServer, T> callback;

        private Type(Supplier<ArgumentType<?>> supplier, Function<Type<T>, T> function, BiConsumer<MinecraftServer, T> biConsumer) {
            this.argument = supplier;
            this.constructor = function;
            this.callback = biConsumer;
        }

        public RequiredArgumentBuilder<CommandSourceStack, ?> createArgument(String string) {
            return Commands.argument(string, this.argument.get());
        }

        public T createRule() {
            return (T)((Value)this.constructor.apply(this));
        }
    }

    public static final class Key<T extends Value<T>> {
        private final String id;

        public Key(String string) {
            this.id = string;
        }

        public String toString() {
            return this.id;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            return object instanceof Key && ((Key)object).id.equals(this.id);
        }

        public int hashCode() {
            return this.id.hashCode();
        }

        public String getId() {
            return this.id;
        }
    }

    @FunctionalInterface
    public static interface GameRuleTypeVisitor {
        public <T extends Value<T>> void visit(Key<T> var1, Type<T> var2);
    }
}

