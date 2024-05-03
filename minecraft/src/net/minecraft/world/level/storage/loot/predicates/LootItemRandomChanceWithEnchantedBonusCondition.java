package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
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

public record LootItemRandomChanceWithEnchantedBonusCondition(LevelBasedValue chance, Holder<Enchantment> enchantment) implements LootItemCondition {
	public static final MapCodec<LootItemRandomChanceWithEnchantedBonusCondition> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					LevelBasedValue.CODEC.fieldOf("chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::chance),
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
		int i;
		if (entity instanceof LivingEntity livingEntity) {
			i = EnchantmentHelper.getEnchantmentLevel(this.enchantment, livingEntity);
		} else {
			i = 0;
		}

		return lootContext.getRandom().nextFloat() < this.chance.calculate(i);
	}

	public static LootItemCondition.Builder randomChanceAndLootingBoost(HolderLookup.Provider provider, float f, float g) {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = provider.lookupOrThrow(Registries.ENCHANTMENT);
		return () -> new LootItemRandomChanceWithEnchantedBonusCondition(new LevelBasedValue.Linear(f, g), registryLookup.getOrThrow(Enchantments.LOOTING));
	}
}
