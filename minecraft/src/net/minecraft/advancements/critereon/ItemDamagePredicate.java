package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public record ItemDamagePredicate(MinMaxBounds.Ints durability, MinMaxBounds.Ints damage) implements SingleComponentItemPredicate<Integer> {
	public static final Codec<ItemDamagePredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "durability", MinMaxBounds.Ints.ANY).forGetter(ItemDamagePredicate::durability),
					ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "damage", MinMaxBounds.Ints.ANY).forGetter(ItemDamagePredicate::damage)
				)
				.apply(instance, ItemDamagePredicate::new)
	);

	@Override
	public DataComponentType<Integer> componentType() {
		return DataComponents.DAMAGE;
	}

	public boolean matches(ItemStack itemStack, Integer integer) {
		return !this.durability.matches(itemStack.getMaxDamage() - integer) ? false : this.damage.matches(integer);
	}

	public static ItemDamagePredicate durability(MinMaxBounds.Ints ints) {
		return new ItemDamagePredicate(ints, MinMaxBounds.Ints.ANY);
	}
}
