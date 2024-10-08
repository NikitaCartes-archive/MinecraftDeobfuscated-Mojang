package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ExplosionCondition implements LootItemCondition {
	private static final ExplosionCondition INSTANCE = new ExplosionCondition();
	public static final MapCodec<ExplosionCondition> CODEC = MapCodec.unit(INSTANCE);

	private ExplosionCondition() {
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.SURVIVES_EXPLOSION;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.EXPLOSION_RADIUS);
	}

	public boolean test(LootContext lootContext) {
		Float float_ = lootContext.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
		if (float_ != null) {
			RandomSource randomSource = lootContext.getRandom();
			float f = 1.0F / float_;
			return randomSource.nextFloat() <= f;
		} else {
			return true;
		}
	}

	public static LootItemCondition.Builder survivesExplosion() {
		return () -> INSTANCE;
	}
}
