package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetItemCountFunction extends LootItemConditionalFunction {
	public static final MapCodec<SetItemCountFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<NumberProvider, boolean>and(
					instance.group(
						NumberProviders.CODEC.fieldOf("count").forGetter(setItemCountFunction -> setItemCountFunction.value),
						Codec.BOOL.fieldOf("add").orElse(false).forGetter(setItemCountFunction -> setItemCountFunction.add)
					)
				)
				.apply(instance, SetItemCountFunction::new)
	);
	private final NumberProvider value;
	private final boolean add;

	private SetItemCountFunction(List<LootItemCondition> list, NumberProvider numberProvider, boolean bl) {
		super(list);
		this.value = numberProvider;
		this.add = bl;
	}

	@Override
	public LootItemFunctionType<SetItemCountFunction> getType() {
		return LootItemFunctions.SET_COUNT;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return this.value.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		int i = this.add ? itemStack.getCount() : 0;
		itemStack.setCount(i + this.value.getInt(lootContext));
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberProvider) {
		return simpleBuilder(list -> new SetItemCountFunction(list, numberProvider, false));
	}

	public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberProvider, boolean bl) {
		return simpleBuilder(list -> new SetItemCountFunction(list, numberProvider, bl));
	}
}
