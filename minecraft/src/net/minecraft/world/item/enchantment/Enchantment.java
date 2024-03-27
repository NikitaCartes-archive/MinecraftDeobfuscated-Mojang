package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Enchantment implements FeatureElement {
	private final Enchantment.EnchantmentDefinition definition;
	@Nullable
	protected String descriptionId;
	private final Holder.Reference<Enchantment> builtInRegistryHolder = BuiltInRegistries.ENCHANTMENT.createIntrusiveHolder(this);

	public static Enchantment.Cost constantCost(int i) {
		return new Enchantment.Cost(i, 0);
	}

	public static Enchantment.Cost dynamicCost(int i, int j) {
		return new Enchantment.Cost(i, j);
	}

	public static Enchantment.EnchantmentDefinition definition(
		TagKey<Item> tagKey, TagKey<Item> tagKey2, int i, int j, Enchantment.Cost cost, Enchantment.Cost cost2, int k, EquipmentSlot... equipmentSlots
	) {
		return new Enchantment.EnchantmentDefinition(tagKey, Optional.of(tagKey2), i, j, cost, cost2, k, FeatureFlags.DEFAULT_FLAGS, equipmentSlots);
	}

	public static Enchantment.EnchantmentDefinition definition(
		TagKey<Item> tagKey, int i, int j, Enchantment.Cost cost, Enchantment.Cost cost2, int k, EquipmentSlot... equipmentSlots
	) {
		return new Enchantment.EnchantmentDefinition(tagKey, Optional.empty(), i, j, cost, cost2, k, FeatureFlags.DEFAULT_FLAGS, equipmentSlots);
	}

	public static Enchantment.EnchantmentDefinition definition(
		TagKey<Item> tagKey, int i, int j, Enchantment.Cost cost, Enchantment.Cost cost2, int k, FeatureFlagSet featureFlagSet, EquipmentSlot... equipmentSlots
	) {
		return new Enchantment.EnchantmentDefinition(tagKey, Optional.empty(), i, j, cost, cost2, k, featureFlagSet, equipmentSlots);
	}

	@Nullable
	public static Enchantment byId(int i) {
		return BuiltInRegistries.ENCHANTMENT.byId(i);
	}

	public Enchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		this.definition = enchantmentDefinition;
	}

	public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity livingEntity) {
		Map<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);

		for (EquipmentSlot equipmentSlot : this.definition.slots()) {
			ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
			if (!itemStack.isEmpty()) {
				map.put(equipmentSlot, itemStack);
			}
		}

		return map;
	}

	public final TagKey<Item> getSupportedItems() {
		return this.definition.supportedItems();
	}

	public final boolean isPrimaryItem(ItemStack itemStack) {
		return this.definition.primaryItems.isEmpty() || itemStack.is((TagKey<Item>)this.definition.primaryItems.get());
	}

	public final int getWeight() {
		return this.definition.weight();
	}

	public final int getAnvilCost() {
		return this.definition.anvilCost();
	}

	public final int getMinLevel() {
		return 1;
	}

	public final int getMaxLevel() {
		return this.definition.maxLevel();
	}

	public final int getMinCost(int i) {
		return this.definition.minCost().calculate(i);
	}

	public final int getMaxCost(int i) {
		return this.definition.maxCost().calculate(i);
	}

	public int getDamageProtection(int i, DamageSource damageSource) {
		return 0;
	}

	public float getDamageBonus(int i, @Nullable EntityType<?> entityType) {
		return 0.0F;
	}

	public final boolean isCompatibleWith(Enchantment enchantment) {
		return this.checkCompatibility(enchantment) && enchantment.checkCompatibility(this);
	}

	protected boolean checkCompatibility(Enchantment enchantment) {
		return this != enchantment;
	}

	protected String getOrCreateDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(this));
		}

		return this.descriptionId;
	}

	public String getDescriptionId() {
		return this.getOrCreateDescriptionId();
	}

	public Component getFullname(int i) {
		MutableComponent mutableComponent = Component.translatable(this.getDescriptionId());
		if (this.isCurse()) {
			mutableComponent.withStyle(ChatFormatting.RED);
		} else {
			mutableComponent.withStyle(ChatFormatting.GRAY);
		}

		if (i != 1 || this.getMaxLevel() != 1) {
			mutableComponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + i));
		}

		return mutableComponent;
	}

	public boolean canEnchant(ItemStack itemStack) {
		return itemStack.getItem().builtInRegistryHolder().is(this.definition.supportedItems());
	}

	public void doPostAttack(LivingEntity livingEntity, Entity entity, int i) {
	}

	public void doPostHurt(LivingEntity livingEntity, Entity entity, int i) {
	}

	public void doPostItemStackHurt(LivingEntity livingEntity, Entity entity, int i) {
	}

	public boolean isTreasureOnly() {
		return false;
	}

	public boolean isCurse() {
		return false;
	}

	public boolean isTradeable() {
		return true;
	}

	public boolean isDiscoverable() {
		return true;
	}

	@Deprecated
	public Holder.Reference<Enchantment> builtInRegistryHolder() {
		return this.builtInRegistryHolder;
	}

	@Override
	public FeatureFlagSet requiredFeatures() {
		return this.definition.requiredFeatures();
	}

	public static record Cost(int base, int perLevel) {
		public int calculate(int i) {
			return this.base + this.perLevel * (i - 1);
		}
	}

	public static record EnchantmentDefinition(
		TagKey<Item> supportedItems,
		Optional<TagKey<Item>> primaryItems,
		int weight,
		int maxLevel,
		Enchantment.Cost minCost,
		Enchantment.Cost maxCost,
		int anvilCost,
		FeatureFlagSet requiredFeatures,
		EquipmentSlot[] slots
	) {
	}
}
