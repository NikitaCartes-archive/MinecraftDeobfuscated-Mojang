package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;

public record LootItemRandomChanceCondition(float probability) implements LootItemCondition {
	public static final Codec<LootItemRandomChanceCondition> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.FLOAT.fieldOf("chance").forGetter(LootItemRandomChanceCondition::probability))
				.apply(instance, LootItemRandomChanceCondition::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.RANDOM_CHANCE;
	}

	public boolean test(LootContext lootContext) {
		return lootContext.getRandom().nextFloat() < this.probability;
	}

	public static LootItemCondition.Builder randomChance(float f) {
		return () -> new LootItemRandomChanceCondition(f);
	}
}
