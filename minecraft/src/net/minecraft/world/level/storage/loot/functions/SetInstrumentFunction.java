package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemConditionalFunction {
	public static final Codec<SetInstrumentFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.and(TagKey.hashedCodec(Registries.INSTRUMENT).fieldOf("options").forGetter(setInstrumentFunction -> setInstrumentFunction.options))
				.apply(instance, SetInstrumentFunction::new)
	);
	private final TagKey<Instrument> options;

	private SetInstrumentFunction(List<LootItemCondition> list, TagKey<Instrument> tagKey) {
		super(list);
		this.options = tagKey;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_INSTRUMENT;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		InstrumentItem.setRandom(itemStack, this.options, lootContext.getRandom());
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> tagKey) {
		return simpleBuilder(list -> new SetInstrumentFunction(list, tagKey));
	}
}
