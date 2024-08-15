package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemConditionalFunction {
	public static final MapCodec<SetInstrumentFunction> CODEC = RecordCodecBuilder.mapCodec(
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
	public LootItemFunctionType<SetInstrumentFunction> getType() {
		return LootItemFunctions.SET_INSTRUMENT;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Registry<Instrument> registry = lootContext.getLevel().registryAccess().registryOrThrow(Registries.INSTRUMENT);
		Optional<Holder<Instrument>> optional = registry.getRandomElementOf(this.options, lootContext.getRandom());
		if (optional.isPresent()) {
			itemStack.set(DataComponents.INSTRUMENT, (Holder<Instrument>)optional.get());
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> tagKey) {
		return simpleBuilder(list -> new SetInstrumentFunction(list, tagKey));
	}
}
