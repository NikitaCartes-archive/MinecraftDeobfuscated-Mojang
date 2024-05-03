package net.minecraft.world.item.enchantment.effects;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record EnchantmentAttributeEffect(String name, Holder<Attribute> attribute, LevelBasedValue amount, AttributeModifier.Operation operation, UUID uuid)
	implements EnchantmentLocationBasedEffect {
	public static final MapCodec<EnchantmentAttributeEffect> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.STRING.fieldOf("name").forGetter(EnchantmentAttributeEffect::name),
					Attribute.CODEC.fieldOf("attribute").forGetter(EnchantmentAttributeEffect::attribute),
					LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentAttributeEffect::amount),
					AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(EnchantmentAttributeEffect::operation),
					UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(EnchantmentAttributeEffect::uuid)
				)
				.apply(instance, EnchantmentAttributeEffect::new)
	);

	public AttributeModifier getModifier(int i) {
		return new AttributeModifier(this.uuid(), this.name(), (double)this.amount().calculate(i), this.operation());
	}

	@Override
	public void onChangedBlock(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, boolean bl) {
		if (bl && entity instanceof LivingEntity livingEntity) {
			livingEntity.getAttributes().addTransientAttributeModifiers(this.makeAttributeMap(i));
		}
	}

	@Override
	public void onDeactivated(EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, int i) {
		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.getAttributes().removeAttributeModifiers(this.makeAttributeMap(i));
		}
	}

	private HashMultimap<Holder<Attribute>, AttributeModifier> makeAttributeMap(int i) {
		HashMultimap<Holder<Attribute>, AttributeModifier> hashMultimap = HashMultimap.create();
		hashMultimap.put(this.attribute, this.getModifier(i));
		return hashMultimap;
	}

	@Override
	public MapCodec<EnchantmentAttributeEffect> codec() {
		return CODEC;
	}
}
