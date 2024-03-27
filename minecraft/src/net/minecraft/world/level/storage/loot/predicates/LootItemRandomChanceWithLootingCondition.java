package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record LootItemRandomChanceWithLootingCondition(float percent, float lootingMultiplier) implements LootItemCondition {
	public static final MapCodec<LootItemRandomChanceWithLootingCondition> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.FLOAT.fieldOf("chance").forGetter(LootItemRandomChanceWithLootingCondition::percent),
					Codec.FLOAT.fieldOf("looting_multiplier").forGetter(LootItemRandomChanceWithLootingCondition::lootingMultiplier)
				)
				.apply(instance, LootItemRandomChanceWithLootingCondition::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.RANDOM_CHANCE_WITH_LOOTING;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
	}

	public boolean test(LootContext lootContext) {
		Entity entity = lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
		int i = 0;
		if (entity instanceof LivingEntity) {
			i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
		}

		return lootContext.getRandom().nextFloat() < this.percent + (float)i * this.lootingMultiplier;
	}

	public static LootItemCondition.Builder randomChanceAndLootingBoost(float f, float g) {
		return () -> new LootItemRandomChanceWithLootingCondition(f, g);
	}
}
