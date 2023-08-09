package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public record ScoreboardValue(ScoreboardNameProvider target, String score, float scale) implements NumberProvider {
	public static final Codec<ScoreboardValue> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ScoreboardNameProviders.CODEC.fieldOf("target").forGetter(ScoreboardValue::target),
					Codec.STRING.fieldOf("score").forGetter(ScoreboardValue::score),
					Codec.FLOAT.fieldOf("scale").orElse(1.0F).forGetter(ScoreboardValue::scale)
				)
				.apply(instance, ScoreboardValue::new)
	);

	@Override
	public LootNumberProviderType getType() {
		return NumberProviders.SCORE;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.target.getReferencedContextParams();
	}

	public static ScoreboardValue fromScoreboard(LootContext.EntityTarget entityTarget, String string) {
		return fromScoreboard(entityTarget, string, 1.0F);
	}

	public static ScoreboardValue fromScoreboard(LootContext.EntityTarget entityTarget, String string, float f) {
		return new ScoreboardValue(ContextScoreboardNameProvider.forTarget(entityTarget), string, f);
	}

	@Override
	public float getFloat(LootContext lootContext) {
		String string = this.target.getScoreboardName(lootContext);
		if (string == null) {
			return 0.0F;
		} else {
			Scoreboard scoreboard = lootContext.getLevel().getScoreboard();
			Objective objective = scoreboard.getObjective(this.score);
			if (objective == null) {
				return 0.0F;
			} else {
				return !scoreboard.hasPlayerScore(string, objective) ? 0.0F : (float)scoreboard.getOrCreatePlayerScore(string, objective).getScore() * this.scale;
			}
		}
	}
}
