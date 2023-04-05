package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogUtils.getLogger();
	final ResourceLocation name;

	FunctionReference(LootItemCondition[] lootItemConditions, ResourceLocation resourceLocation) {
		super(lootItemConditions);
		this.name = resourceLocation;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.REFERENCE;
	}

	@Override
	public void validate(ValidationContext validationContext) {
		LootDataId<LootItemFunction> lootDataId = new LootDataId<>(LootDataType.MODIFIER, this.name);
		if (validationContext.hasVisitedElement(lootDataId)) {
			validationContext.reportProblem("Function " + this.name + " is recursively called");
		} else {
			super.validate(validationContext);
			validationContext.resolver()
				.getElementOptional(lootDataId)
				.ifPresentOrElse(
					lootItemFunction -> lootItemFunction.validate(validationContext.enterElement(".{" + this.name + "}", lootDataId)),
					() -> validationContext.reportProblem("Unknown function table called " + this.name)
				);
		}
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		LootItemFunction lootItemFunction = lootContext.getResolver().getElement(LootDataType.MODIFIER, this.name);
		if (lootItemFunction == null) {
			LOGGER.warn("Unknown function: {}", this.name);
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

	public static LootItemConditionalFunction.Builder<?> functionReference(ResourceLocation resourceLocation) {
		return simpleBuilder(lootItemConditions -> new FunctionReference(lootItemConditions, resourceLocation));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<FunctionReference> {
		public void serialize(JsonObject jsonObject, FunctionReference functionReference, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("name", functionReference.name.toString());
		}

		public FunctionReference deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
			return new FunctionReference(lootItemConditions, resourceLocation);
		}
	}
}
