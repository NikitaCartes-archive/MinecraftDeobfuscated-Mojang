package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetOminousBottleAmplifierFunction extends LootItemConditionalFunction {
	static final MapCodec<SetOminousBottleAmplifierFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.and(NumberProviders.CODEC.fieldOf("amplifier").forGetter(setOminousBottleAmplifierFunction -> setOminousBottleAmplifierFunction.amplifierGenerator))
				.apply(instance, SetOminousBottleAmplifierFunction::new)
	);
	private final NumberProvider amplifierGenerator;

	private SetOminousBottleAmplifierFunction(List<LootItemCondition> list, NumberProvider numberProvider) {
		super(list);
		this.amplifierGenerator = numberProvider;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_OMINOUS_BOTTLE_AMPLIFIER;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, this.amplifierGenerator.getInt(lootContext));
		return itemStack;
	}

	public NumberProvider amplifier() {
		return this.amplifierGenerator;
	}

	public static LootItemConditionalFunction.Builder<?> setAmplifier(NumberProvider numberProvider) {
		return simpleBuilder(list -> new SetOminousBottleAmplifierFunction(list, numberProvider));
	}
}
