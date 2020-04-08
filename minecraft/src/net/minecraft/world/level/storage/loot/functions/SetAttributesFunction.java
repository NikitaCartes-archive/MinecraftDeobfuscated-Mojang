package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetAttributesFunction extends LootItemConditionalFunction {
	private final List<SetAttributesFunction.Modifier> modifiers;

	private SetAttributesFunction(LootItemCondition[] lootItemConditions, List<SetAttributesFunction.Modifier> list) {
		super(lootItemConditions);
		this.modifiers = ImmutableList.copyOf(list);
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Random random = lootContext.getRandom();

		for (SetAttributesFunction.Modifier modifier : this.modifiers) {
			UUID uUID = modifier.id;
			if (uUID == null) {
				uUID = UUID.randomUUID();
			}

			EquipmentSlot equipmentSlot = Util.getRandom(modifier.slots, random);
			itemStack.addAttributeModifier(
				modifier.attribute, new AttributeModifier(uUID, modifier.name, (double)modifier.amount.getFloat(random), modifier.operation), equipmentSlot
			);
		}

		return itemStack;
	}

	static class Modifier {
		private final String name;
		private final Attribute attribute;
		private final AttributeModifier.Operation operation;
		private final RandomValueBounds amount;
		@Nullable
		private final UUID id;
		private final EquipmentSlot[] slots;

		private Modifier(
			String string,
			Attribute attribute,
			AttributeModifier.Operation operation,
			RandomValueBounds randomValueBounds,
			EquipmentSlot[] equipmentSlots,
			@Nullable UUID uUID
		) {
			this.name = string;
			this.attribute = attribute;
			this.operation = operation;
			this.amount = randomValueBounds;
			this.id = uUID;
			this.slots = equipmentSlots;
		}

		public JsonObject serialize(JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name", this.name);
			jsonObject.addProperty("attribute", Registry.ATTRIBUTES.getKey(this.attribute).toString());
			jsonObject.addProperty("operation", operationToString(this.operation));
			jsonObject.add("amount", jsonSerializationContext.serialize(this.amount));
			if (this.id != null) {
				jsonObject.addProperty("id", this.id.toString());
			}

			if (this.slots.length == 1) {
				jsonObject.addProperty("slot", this.slots[0].getName());
			} else {
				JsonArray jsonArray = new JsonArray();

				for (EquipmentSlot equipmentSlot : this.slots) {
					jsonArray.add(new JsonPrimitive(equipmentSlot.getName()));
				}

				jsonObject.add("slot", jsonArray);
			}

			return jsonObject;
		}

		public static SetAttributesFunction.Modifier deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			String string = GsonHelper.getAsString(jsonObject, "name");
			Attribute attribute = Registry.ATTRIBUTES.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "attribute")));
			AttributeModifier.Operation operation = operationFromString(GsonHelper.getAsString(jsonObject, "operation"));
			RandomValueBounds randomValueBounds = GsonHelper.getAsObject(jsonObject, "amount", jsonDeserializationContext, RandomValueBounds.class);
			UUID uUID = null;
			EquipmentSlot[] equipmentSlots;
			if (GsonHelper.isStringValue(jsonObject, "slot")) {
				equipmentSlots = new EquipmentSlot[]{EquipmentSlot.byName(GsonHelper.getAsString(jsonObject, "slot"))};
			} else {
				if (!GsonHelper.isArrayNode(jsonObject, "slot")) {
					throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
				}

				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "slot");
				equipmentSlots = new EquipmentSlot[jsonArray.size()];
				int i = 0;

				for (JsonElement jsonElement : jsonArray) {
					equipmentSlots[i++] = EquipmentSlot.byName(GsonHelper.convertToString(jsonElement, "slot"));
				}

				if (equipmentSlots.length == 0) {
					throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
				}
			}

			if (jsonObject.has("id")) {
				String string2 = GsonHelper.getAsString(jsonObject, "id");

				try {
					uUID = UUID.fromString(string2);
				} catch (IllegalArgumentException var12) {
					throw new JsonSyntaxException("Invalid attribute modifier id '" + string2 + "' (must be UUID format, with dashes)");
				}
			}

			return new SetAttributesFunction.Modifier(string, attribute, operation, randomValueBounds, equipmentSlots, uUID);
		}

		private static String operationToString(AttributeModifier.Operation operation) {
			switch (operation) {
				case ADDITION:
					return "addition";
				case MULTIPLY_BASE:
					return "multiply_base";
				case MULTIPLY_TOTAL:
					return "multiply_total";
				default:
					throw new IllegalArgumentException("Unknown operation " + operation);
			}
		}

		private static AttributeModifier.Operation operationFromString(String string) {
			switch (string) {
				case "addition":
					return AttributeModifier.Operation.ADDITION;
				case "multiply_base":
					return AttributeModifier.Operation.MULTIPLY_BASE;
				case "multiply_total":
					return AttributeModifier.Operation.MULTIPLY_TOTAL;
				default:
					throw new JsonSyntaxException("Unknown attribute modifier operation " + string);
			}
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetAttributesFunction> {
		public Serializer() {
			super(new ResourceLocation("set_attributes"), SetAttributesFunction.class);
		}

		public void serialize(JsonObject jsonObject, SetAttributesFunction setAttributesFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setAttributesFunction, jsonSerializationContext);
			JsonArray jsonArray = new JsonArray();

			for (SetAttributesFunction.Modifier modifier : setAttributesFunction.modifiers) {
				jsonArray.add(modifier.serialize(jsonSerializationContext));
			}

			jsonObject.add("modifiers", jsonArray);
		}

		public SetAttributesFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "modifiers");
			List<SetAttributesFunction.Modifier> list = Lists.<SetAttributesFunction.Modifier>newArrayListWithExpectedSize(jsonArray.size());

			for (JsonElement jsonElement : jsonArray) {
				list.add(SetAttributesFunction.Modifier.deserialize(GsonHelper.convertToJsonObject(jsonElement, "modifier"), jsonDeserializationContext));
			}

			if (list.isEmpty()) {
				throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
			} else {
				return new SetAttributesFunction(lootItemConditions, list);
			}
		}
	}
}
