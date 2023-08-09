package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
	public static final Codec<LimitCount> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance).and(IntRange.CODEC.fieldOf("limit").forGetter(limitCount -> limitCount.limiter)).apply(instance, LimitCount::new)
	);
	private final IntRange limiter;

	private LimitCount(List<LootItemCondition> list, IntRange intRange) {
		super(list);
		this.limiter = intRange;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.LIMIT_COUNT;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.limiter.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		int i = this.limiter.clamp(lootContext, itemStack.getCount());
		itemStack.setCount(i);
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> limitCount(IntRange intRange) {
		return simpleBuilder(list -> new LimitCount(list, intRange));
	}
}
