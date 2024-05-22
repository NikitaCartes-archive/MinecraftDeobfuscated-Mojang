package net.minecraft.world.item.enchantment.effects;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record EnchantmentAttributeEffect(ResourceLocation id, Holder<Attribute> attribute, LevelBasedValue amount, AttributeModifier.Operation operation)
	implements EnchantmentLocationBasedEffect {
	public static final MapCodec<EnchantmentAttributeEffect> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("id").forGetter(EnchantmentAttributeEffect::id),
					Attribute.CODEC.fieldOf("attribute").forGetter(EnchantmentAttributeEffect::attribute),
					LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentAttributeEffect::amount),
					AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(EnchantmentAttributeEffect::operation)
				)
				.apply(instance, EnchantmentAttributeEffect::new)
	);

	private ResourceLocation idForSlot(StringRepresentable stringRepresentable) {
		return this.id.withSuffix("/" + stringRepresentable.getSerializedName());
	}

	public AttributeModifier getModifier(int i, StringRepresentable stringRepresentable) {
		return new AttributeModifier(this.idForSlot(stringRepresentable), (double)this.amount().calculate(i), this.operation());
	}

	@Override
	public void onChangedBlock(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, boolean bl) {
		if (bl && entity instanceof LivingEntity livingEntity) {
			livingEntity.getAttributes().addTransientAttributeModifiers(this.makeAttributeMap(i, enchantedItemInUse.inSlot()));
		}
	}

	@Override
	public void onDeactivated(EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, int i) {
		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.getAttributes().removeAttributeModifiers(this.makeAttributeMap(i, enchantedItemInUse.inSlot()));
		}
	}

	private HashMultimap<Holder<Attribute>, AttributeModifier> makeAttributeMap(int i, EquipmentSlot equipmentSlot) {
		HashMultimap<Holder<Attribute>, AttributeModifier> hashMultimap = HashMultimap.create();
		hashMultimap.put(this.attribute, this.getModifier(i, equipmentSlot));
		return hashMultimap;
	}

	@Override
	public MapCodec<EnchantmentAttributeEffect> codec() {
		return CODEC;
	}
}
