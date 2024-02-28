package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetComponentsFunction extends LootItemConditionalFunction {
	public static final Codec<SetComponentsFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.and(DataComponentPatch.CODEC.fieldOf("components").forGetter(setComponentsFunction -> setComponentsFunction.components))
				.apply(instance, SetComponentsFunction::new)
	);
	private final DataComponentPatch components;

	private SetComponentsFunction(List<LootItemCondition> list, DataComponentPatch dataComponentPatch) {
		super(list);
		this.components = dataComponentPatch;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_COMPONENTS;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.applyComponents(this.components);
		return itemStack;
	}

	public static <T> LootItemConditionalFunction.Builder<?> setComponent(DataComponentType<T> dataComponentType, T object) {
		return simpleBuilder(list -> new SetComponentsFunction(list, DataComponentPatch.builder().set(dataComponentType, object).build()));
	}
}
