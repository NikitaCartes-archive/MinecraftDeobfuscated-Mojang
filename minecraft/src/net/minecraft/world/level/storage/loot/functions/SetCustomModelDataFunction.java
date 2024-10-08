package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetCustomModelDataFunction extends LootItemConditionalFunction {
	static final MapCodec<SetCustomModelDataFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.and(NumberProviders.CODEC.fieldOf("value").forGetter(setCustomModelDataFunction -> setCustomModelDataFunction.valueProvider))
				.apply(instance, SetCustomModelDataFunction::new)
	);
	private final NumberProvider valueProvider;

	private SetCustomModelDataFunction(List<LootItemCondition> list, NumberProvider numberProvider) {
		super(list);
		this.valueProvider = numberProvider;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return this.valueProvider.getReferencedContextParams();
	}

	@Override
	public LootItemFunctionType<SetCustomModelDataFunction> getType() {
		return LootItemFunctions.SET_CUSTOM_MODEL_DATA;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(this.valueProvider.getInt(lootContext)));
		return itemStack;
	}
}
