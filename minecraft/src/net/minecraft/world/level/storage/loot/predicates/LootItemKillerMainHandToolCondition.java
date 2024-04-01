package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record LootItemKillerMainHandToolCondition(ItemPredicate predicate) implements LootItemCondition {
	public static final Codec<LootItemKillerMainHandToolCondition> CODEC = ItemPredicate.CODEC
		.xmap(LootItemKillerMainHandToolCondition::new, LootItemKillerMainHandToolCondition::predicate);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.KILLER_MAIN_HAND_TOOL;
	}

	public boolean test(LootContext lootContext) {
		DamageSource damageSource = lootContext.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
		if (damageSource != null) {
			Entity entity = damageSource.getEntity();
			return entity != null && entity instanceof LivingEntity livingEntity ? this.predicate.matches(livingEntity.getMainHandItem()) : false;
		} else {
			return false;
		}
	}

	public static LootItemCondition.Builder killedWithItemInHand(ItemLike itemLike) {
		return () -> new LootItemKillerMainHandToolCondition(ItemPredicate.Builder.item().of(itemLike).build());
	}
}
