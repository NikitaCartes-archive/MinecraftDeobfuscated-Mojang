package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.scores.ScoreHolder;

public record ContextScoreboardNameProvider(LootContext.EntityTarget target) implements ScoreboardNameProvider {
	public static final MapCodec<ContextScoreboardNameProvider> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(LootContext.EntityTarget.CODEC.fieldOf("target").forGetter(ContextScoreboardNameProvider::target))
				.apply(instance, ContextScoreboardNameProvider::new)
	);
	public static final Codec<ContextScoreboardNameProvider> INLINE_CODEC = LootContext.EntityTarget.CODEC
		.xmap(ContextScoreboardNameProvider::new, ContextScoreboardNameProvider::target);

	public static ScoreboardNameProvider forTarget(LootContext.EntityTarget entityTarget) {
		return new ContextScoreboardNameProvider(entityTarget);
	}

	@Override
	public LootScoreProviderType getType() {
		return ScoreboardNameProviders.CONTEXT;
	}

	@Nullable
	@Override
	public ScoreHolder getScoreHolder(LootContext lootContext) {
		return lootContext.getOptionalParameter(this.target.getParam());
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Set.of(this.target.getParam());
	}
}
