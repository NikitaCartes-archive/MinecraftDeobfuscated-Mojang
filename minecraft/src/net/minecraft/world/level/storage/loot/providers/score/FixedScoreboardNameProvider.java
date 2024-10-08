package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.scores.ScoreHolder;

public record FixedScoreboardNameProvider(String name) implements ScoreboardNameProvider {
	public static final MapCodec<FixedScoreboardNameProvider> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.STRING.fieldOf("name").forGetter(FixedScoreboardNameProvider::name)).apply(instance, FixedScoreboardNameProvider::new)
	);

	public static ScoreboardNameProvider forName(String string) {
		return new FixedScoreboardNameProvider(string);
	}

	@Override
	public LootScoreProviderType getType() {
		return ScoreboardNameProviders.FIXED;
	}

	@Override
	public ScoreHolder getScoreHolder(LootContext lootContext) {
		return ScoreHolder.forNameOnly(this.name);
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Set.of();
	}
}
