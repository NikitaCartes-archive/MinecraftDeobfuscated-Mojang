package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.level.GameRules;

public class BinaryGameRuleRule extends OneShotRule.Simple {
	private final List<GameRules.Key<GameRules.BooleanValue>> allowedRules = List.of(
		GameRules.RULE_DOFIRETICK,
		GameRules.RULE_MOBGRIEFING,
		GameRules.RULE_KEEPINVENTORY,
		GameRules.RULE_DOMOBSPAWNING,
		GameRules.RULE_DOMOBLOOT,
		GameRules.RULE_DOBLOCKDROPS,
		GameRules.RULE_DOENTITYDROPS,
		GameRules.RULE_NATURAL_REGENERATION,
		GameRules.RULE_DAYLIGHT,
		GameRules.RULE_SHOWDEATHMESSAGES,
		GameRules.RULE_REDUCEDDEBUGINFO,
		GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK,
		GameRules.RULE_WEATHER_CYCLE,
		GameRules.RULE_LIMITED_CRAFTING,
		GameRules.RULE_ANNOUNCE_ADVANCEMENTS,
		GameRules.RULE_DISABLE_RAIDS,
		GameRules.RULE_DOINSOMNIA,
		GameRules.RULE_DO_IMMEDIATE_RESPAWN,
		GameRules.RULE_DROWNING_DAMAGE,
		GameRules.RULE_FALL_DAMAGE,
		GameRules.RULE_FIRE_DAMAGE,
		GameRules.RULE_FREEZE_DAMAGE,
		GameRules.RULE_DO_PATROL_SPAWNING,
		GameRules.RULE_DO_TRADER_SPAWNING,
		GameRules.RULE_DO_WARDEN_SPAWNING,
		GameRules.RULE_FORGIVE_DEAD_PLAYERS,
		GameRules.RULE_UNIVERSAL_ANGER,
		GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY,
		GameRules.RULE_MOB_EXPLOSION_DROP_DECAY,
		GameRules.RULE_TNT_EXPLOSION_DROP_DECAY,
		GameRules.RULE_WATER_SOURCE_CONVERSION,
		GameRules.RULE_LAVA_SOURCE_CONVERSION,
		GameRules.RULE_GLOBAL_SOUND_EVENTS,
		GameRules.RULE_DO_VINES_SPREAD
	);
	private final Map<String, GameRules.Key<GameRules.BooleanValue>> keys = (Map<String, GameRules.Key<GameRules.BooleanValue>>)this.allowedRules
		.stream()
		.collect(Collectors.toMap(GameRules.Key::getId, key -> key));
	private final Codec<BinaryGameRuleRule.RuleRuleChange> CODEC = ExtraCodecs.stringResolverCodec(GameRules.Key::getId, this.keys::get)
		.xmap(key -> new BinaryGameRuleRule.RuleRuleChange(key), ruleRuleChange -> ruleRuleChange.key);

	@Override
	public Optional<RuleChange> randomApprovableChange(MinecraftServer minecraftServer, RandomSource randomSource) {
		return Util.getRandomSafe(this.allowedRules, randomSource).map(key -> new BinaryGameRuleRule.RuleRuleChange(key));
	}

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.CODEC);
	}

	class RuleRuleChange extends OneShotRule.OneShotRuleChange {
		final GameRules.Key<GameRules.BooleanValue> key;
		private final Component displayName;

		RuleRuleChange(GameRules.Key<GameRules.BooleanValue> key) {
			this.key = key;
			this.displayName = Component.translatable("rule.flip_binary_gamerule", Component.translatable(key.getDescriptionId()));
		}

		@Override
		protected Component description() {
			return this.displayName;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			GameRules.BooleanValue booleanValue = minecraftServer.getWorldData().getGameRules().getRule(this.key);
			booleanValue.set(!booleanValue.get(), minecraftServer);
		}
	}
}
