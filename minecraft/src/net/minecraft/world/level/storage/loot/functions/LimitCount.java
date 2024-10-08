package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
	public static final MapCodec<LimitCount> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance).and(IntRange.CODEC.fieldOf("limit").forGetter(limitCount -> limitCount.limiter)).apply(instance, LimitCount::new)
	);
	private final IntRange limiter;

	private LimitCount(List<LootItemCondition> list, IntRange intRange) {
		super(list);
		this.limiter = intRange;
	}

	@Override
	public LootItemFunctionType<LimitCount> getType() {
		return LootItemFunctions.LIMIT_COUNT;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
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
