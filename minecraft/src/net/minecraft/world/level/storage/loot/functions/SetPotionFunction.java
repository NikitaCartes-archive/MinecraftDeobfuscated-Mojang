package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemConditionalFunction {
	public static final Codec<SetPotionFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.and(BuiltInRegistries.POTION.holderByNameCodec().fieldOf("id").forGetter(setPotionFunction -> setPotionFunction.potion))
				.apply(instance, SetPotionFunction::new)
	);
	private final Holder<Potion> potion;

	private SetPotionFunction(List<LootItemCondition> list, Holder<Potion> holder) {
		super(list);
		this.potion = holder;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_POTION;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		PotionUtils.setPotion(itemStack, this.potion.value());
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setPotion(Potion potion) {
		return simpleBuilder(list -> new SetPotionFunction(list, potion.builtInRegistryHolder()));
	}
}
