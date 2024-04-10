package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<FunctionReference> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.and(ResourceKey.codec(Registries.ITEM_MODIFIER).fieldOf("name").forGetter(functionReference -> functionReference.name))
				.apply(instance, FunctionReference::new)
	);
	private final ResourceKey<LootItemFunction> name;

	private FunctionReference(List<LootItemCondition> list, ResourceKey<LootItemFunction> resourceKey) {
		super(list);
		this.name = resourceKey;
	}

	@Override
	public LootItemFunctionType<FunctionReference> getType() {
		return LootItemFunctions.REFERENCE;
	}

	@Override
	public void validate(ValidationContext validationContext) {
		if (validationContext.hasVisitedElement(this.name)) {
			validationContext.reportProblem("Function " + this.name.location() + " is recursively called");
		} else {
			super.validate(validationContext);
			validationContext.resolver()
				.get(Registries.ITEM_MODIFIER, this.name)
				.ifPresentOrElse(
					reference -> ((LootItemFunction)reference.value()).validate(validationContext.enterElement(".{" + this.name.location() + "}", this.name)),
					() -> validationContext.reportProblem("Unknown function table called " + this.name.location())
				);
		}
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		LootItemFunction lootItemFunction = (LootItemFunction)lootContext.getResolver().get(Registries.ITEM_MODIFIER, this.name).map(Holder::value).orElse(null);
		if (lootItemFunction == null) {
			LOGGER.warn("Unknown function: {}", this.name.location());
			return itemStack;
		} else {
			LootContext.VisitedEntry<?> visitedEntry = LootContext.createVisitedEntry(lootItemFunction);
			if (lootContext.pushVisitedElement(visitedEntry)) {
				ItemStack var5;
				try {
					var5 = (ItemStack)lootItemFunction.apply(itemStack, lootContext);
				} finally {
					lootContext.popVisitedElement(visitedEntry);
				}

				return var5;
			} else {
				LOGGER.warn("Detected infinite loop in loot tables");
				return itemStack;
			}
		}
	}

	public static LootItemConditionalFunction.Builder<?> functionReference(ResourceKey<LootItemFunction> resourceKey) {
		return simpleBuilder(list -> new FunctionReference(list, resourceKey));
	}
}
