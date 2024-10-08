package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyExplosionDecay extends LootItemConditionalFunction {
	public static final MapCodec<ApplyExplosionDecay> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance).apply(instance, ApplyExplosionDecay::new)
	);

	private ApplyExplosionDecay(List<LootItemCondition> list) {
		super(list);
	}

	@Override
	public LootItemFunctionType<ApplyExplosionDecay> getType() {
		return LootItemFunctions.EXPLOSION_DECAY;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Float float_ = lootContext.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
		if (float_ != null) {
			RandomSource randomSource = lootContext.getRandom();
			float f = 1.0F / float_;
			int i = itemStack.getCount();
			int j = 0;

			for (int k = 0; k < i; k++) {
				if (randomSource.nextFloat() <= f) {
					j++;
				}
			}

			itemStack.setCount(j);
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> explosionDecay() {
		return simpleBuilder(ApplyExplosionDecay::new);
	}
}
