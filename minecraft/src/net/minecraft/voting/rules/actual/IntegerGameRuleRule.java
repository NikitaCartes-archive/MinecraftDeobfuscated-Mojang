package net.minecraft.voting.rules.actual;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.level.GameRules;

public class IntegerGameRuleRule extends OneShotRule {
	private final List<Pair<GameRules.Key<GameRules.IntegerValue>, IntProvider>> allowedRules = List.of(
		Pair.of(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT, UniformInt.of(0, 8)),
		Pair.of(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE, ClampedInt.of(UniformInt.of(-20, 120), 0, 100)),
		Pair.of(GameRules.RULE_MAX_ENTITY_CRAMMING, UniformInt.of(0, 100)),
		Pair.of(GameRules.RULE_SPAWN_RADIUS, UniformInt.of(1, 100)),
		Pair.of(GameRules.RULE_RANDOMTICKING, UniformInt.of(0, 20))
	);
	private final Map<String, GameRules.Key<GameRules.IntegerValue>> keys = (Map<String, GameRules.Key<GameRules.IntegerValue>>)this.allowedRules
		.stream()
		.collect(Collectors.toMap(pair -> ((GameRules.Key)pair.getFirst()).getId(), Pair::getFirst));
	private final Codec<GameRules.Key<GameRules.IntegerValue>> gameRuleIdCodec = ExtraCodecs.stringResolverCodec(GameRules.Key::getId, this.keys::get);
	private final Codec<IntegerGameRuleRule.RuleRuleChange> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					this.gameRuleIdCodec.fieldOf("game_rule_id").forGetter(ruleRuleChange -> ruleRuleChange.key),
					Codec.INT.fieldOf("value").forGetter(ruleRuleChange -> ruleRuleChange.value)
				)
				.apply(instance, (key, i) -> new IntegerGameRuleRule.RuleRuleChange(key, i))
	);

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return Util.getRandomSafe(this.allowedRules, randomSource)
			.stream()
			.flatMap(
				pair -> {
					GameRules.Key<GameRules.IntegerValue> key = (GameRules.Key<GameRules.IntegerValue>)pair.getFirst();
					int j = minecraftServer.getWorldData().getGameRules().getRule(key).get();
					IntProvider intProvider = (IntProvider)pair.getSecond();
					return Stream.generate(() -> intProvider.sample(randomSource))
						.filter(integer -> integer != j)
						.limit((long)i)
						.map(integer -> new IntegerGameRuleRule.RuleRuleChange(key, integer));
				}
			);
	}

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.CODEC);
	}

	class RuleRuleChange extends OneShotRule.OneShotRuleChange {
		final GameRules.Key<GameRules.IntegerValue> key;
		final int value;
		private final Component displayName;

		RuleRuleChange(GameRules.Key<GameRules.IntegerValue> key, int i) {
			this.key = key;
			this.value = i;
			this.displayName = Component.translatable("rule.change_integer_gamerule", Component.translatable(key.getDescriptionId()), i);
		}

		@Override
		protected Component description() {
			return this.displayName;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			minecraftServer.getWorldData().getGameRules().getRule(this.key).set(this.value, minecraftServer);
		}
	}
}
