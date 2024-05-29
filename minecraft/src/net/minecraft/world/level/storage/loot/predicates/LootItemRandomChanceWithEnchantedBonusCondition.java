package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record LootItemRandomChanceWithEnchantedBonusCondition(float unenchantedChance, LevelBasedValue enchantedChance, Holder<Enchantment> enchantment)
	implements LootItemCondition {
	public static final MapCodec<LootItemRandomChanceWithEnchantedBonusCondition> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("unenchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::unenchantedChance),
					LevelBasedValue.CODEC.fieldOf("enchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantedChance),
					Enchantment.CODEC.fieldOf("enchantment").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantment)
				)
				.apply(instance, LootItemRandomChanceWithEnchantedBonusCondition::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.RANDOM_CHANCE_WITH_ENCHANTED_BONUS;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.ATTACKING_ENTITY);
	}

	public boolean test(LootContext lootContext) {
		Entity entity = lootContext.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
		int i = entity instanceof LivingEntity livingEntity ? EnchantmentHelper.getEnchantmentLevel(this.enchantment, livingEntity) : 0;
		float f = i > 0 ? this.enchantedChance.calculate(i) : this.unenchantedChance;
		return lootContext.getRandom().nextFloat() < f;
	}

	public static LootItemCondition.Builder randomChanceAndLootingBoost(HolderLookup.Provider provider, float f, float g) {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = provider.lookupOrThrow(Registries.ENCHANTMENT);
		return () -> new LootItemRandomChanceWithEnchantedBonusCondition(f, new LevelBasedValue.Linear(f + g, g), registryLookup.getOrThrow(Enchantments.LOOTING));
	}
}
