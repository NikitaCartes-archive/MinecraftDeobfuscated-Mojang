package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record TargetedConditionalEffect<T>(EnchantmentTarget enchanted, EnchantmentTarget affected, T effect, Optional<LootItemCondition> requirements) {
	public static <S> Codec<TargetedConditionalEffect<S>> codec(Codec<S> codec, LootContextParamSet lootContextParamSet) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						EnchantmentTarget.CODEC.fieldOf("enchanted").forGetter(TargetedConditionalEffect::enchanted),
						EnchantmentTarget.CODEC.fieldOf("affected").forGetter(TargetedConditionalEffect::affected),
						codec.fieldOf("effect").forGetter(TargetedConditionalEffect::effect),
						ConditionalEffect.conditionCodec(lootContextParamSet).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)
					)
					.apply(instance, TargetedConditionalEffect::new)
		);
	}

	public static <S> Codec<TargetedConditionalEffect<S>> equipmentDropsCodec(Codec<S> codec, LootContextParamSet lootContextParamSet) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						EnchantmentTarget.CODEC
							.validate(
								enchantmentTarget -> enchantmentTarget != EnchantmentTarget.DAMAGING_ENTITY
										? DataResult.success(enchantmentTarget)
										: DataResult.error(() -> "enchanted must be attacker or victim")
							)
							.fieldOf("enchanted")
							.forGetter(TargetedConditionalEffect::enchanted),
						codec.fieldOf("effect").forGetter(TargetedConditionalEffect::effect),
						ConditionalEffect.conditionCodec(lootContextParamSet).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)
					)
					.apply(instance, (enchantmentTarget, object, optional) -> new TargetedConditionalEffect<>(enchantmentTarget, EnchantmentTarget.VICTIM, object, optional))
		);
	}

	public boolean matches(LootContext lootContext) {
		return this.requirements.isEmpty() ? true : ((LootItemCondition)this.requirements.get()).test(lootContext);
	}
}
