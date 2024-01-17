package net.minecraft.world.item.enchantment;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class DamageEnchantment extends Enchantment {
	private final int minCost;
	private final int levelCost;
	private final int levelCostSpan;
	private final Optional<TagKey<EntityType<?>>> targets;

	public DamageEnchantment(Enchantment.Rarity rarity, int i, int j, int k, Optional<TagKey<EntityType<?>>> optional, EquipmentSlot... equipmentSlots) {
		super(rarity, ItemTags.WEAPON_ENCHANTABLE, equipmentSlots);
		this.minCost = i;
		this.levelCost = j;
		this.levelCostSpan = k;
		this.targets = optional;
	}

	@Override
	public int getMinCost(int i) {
		return this.minCost + (i - 1) * this.levelCost;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + this.levelCostSpan;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public float getDamageBonus(int i, @Nullable EntityType<?> entityType) {
		if (this.targets.isEmpty()) {
			return 1.0F + (float)Math.max(0, i - 1) * 0.5F;
		} else {
			return entityType != null && entityType.is((TagKey<EntityType<?>>)this.targets.get()) ? (float)i * 2.5F : 0.0F;
		}
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return !(enchantment instanceof DamageEnchantment);
	}

	@Override
	public void doPostAttack(LivingEntity livingEntity, Entity entity, int i) {
		if (this.targets.isPresent()
			&& entity instanceof LivingEntity livingEntity2
			&& this.targets.get() == EntityTypeTags.ARTHROPOD
			&& i > 0
			&& livingEntity2.getType().is((TagKey<EntityType<?>>)this.targets.get())) {
			int j = 20 + livingEntity.getRandom().nextInt(10 * i);
			livingEntity2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, j, 3));
		}
	}
}
