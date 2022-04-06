package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetAttributesFunction extends LootItemConditionalFunction {
	final List<SetAttributesFunction.Modifier> modifiers;

	SetAttributesFunction(LootItemCondition[] lootItemConditions, List<SetAttributesFunction.Modifier> list) {
		super(lootItemConditions);
		this.modifiers = ImmutableList.copyOf(list);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_ATTRIBUTES;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return (Set<LootContextParam<?>>)this.modifiers
			.stream()
			.flatMap(modifier -> modifier.amount.getReferencedContextParams().stream())
			.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		RandomSource randomSource = lootContext.getRandom();

		for (SetAttributesFunction.Modifier modifier : this.modifiers) {
			UUID uUID = modifier.id;
			if (uUID == null) {
				uUID = UUID.randomUUID();
			}

			EquipmentSlot equipmentSlot = Util.getRandom(modifier.slots, randomSource);
			itemStack.addAttributeModifier(
				modifier.attribute, new AttributeModifier(uUID, modifier.name, (double)modifier.amount.getFloat(lootContext), modifier.operation), equipmentSlot
			);
		}

		return itemStack;
	}

	public static SetAttributesFunction.ModifierBuilder modifier(
		String string, Attribute attribute, AttributeModifier.Operation operation, NumberProvider numberProvider
	) {
		return new SetAttributesFunction.ModifierBuilder(string, attribute, operation, numberProvider);
	}

	public static SetAttributesFunction.Builder setAttributes() {
		return new SetAttributesFunction.Builder();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetAttributesFunction.Builder> {
		private final List<SetAttributesFunction.Modifier> modifiers = Lists.<SetAttributesFunction.Modifier>newArrayList();

		protected SetAttributesFunction.Builder getThis() {
			return this;
		}

		public SetAttributesFunction.Builder withModifier(SetAttributesFunction.ModifierBuilder modifierBuilder) {
			this.modifiers.add(modifierBuilder.build());
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetAttributesFunction(this.getConditions(), this.modifiers);
		}
	}

	static class Modifier {
		final String name;
		final Attribute attribute;
		final AttributeModifier.Operation operation;
		final NumberProvider amount;
		@Nullable
		final UUID id;
		final EquipmentSlot[] slots;

		Modifier(
			String string,
			Attribute attribute,
			AttributeModifier.Operation operation,
			NumberProvider numberProvider,
			EquipmentSlot[] equipmentSlots,
			@Nullable UUID uUID
		) {
			this.name = string;
			this.attribute = attribute;
			this.operation = operation;
			this.amount = numberProvider;
			this.id = uUID;
			this.slots = equipmentSlots;
		}

		public JsonObject serialize(JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name", this.name);
			jsonObject.addProperty("attribute", Registry.ATTRIBUTE.getKey(this.attribute).toString());
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
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "attribute"));
			Attribute attribute = Registry.ATTRIBUTE.get(resourceLocation);
			if (attribute == null) {
				throw new JsonSyntaxException("Unknown attribute: " + resourceLocation);
			} else {
				AttributeModifier.Operation operation = operationFromString(GsonHelper.getAsString(jsonObject, "operation"));
				NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "amount", jsonDeserializationContext, NumberProvider.class);
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
					} catch (IllegalArgumentException var13) {
						throw new JsonSyntaxException("Invalid attribute modifier id '" + string2 + "' (must be UUID format, with dashes)");
					}
				}

				return new SetAttributesFunction.Modifier(string, attribute, operation, numberProvider, equipmentSlots, uUID);
			}
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

	public static class ModifierBuilder {
		private final String name;
		private final Attribute attribute;
		private final AttributeModifier.Operation operation;
		private final NumberProvider amount;
		@Nullable
		private UUID id;
		private final Set<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

		public ModifierBuilder(String string, Attribute attribute, AttributeModifier.Operation operation, NumberProvider numberProvider) {
			this.name = string;
			this.attribute = attribute;
			this.operation = operation;
			this.amount = numberProvider;
		}

		public SetAttributesFunction.ModifierBuilder forSlot(EquipmentSlot equipmentSlot) {
			this.slots.add(equipmentSlot);
			return this;
		}

		public SetAttributesFunction.ModifierBuilder withUuid(UUID uUID) {
			this.id = uUID;
			return this;
		}

		public SetAttributesFunction.Modifier build() {
			return new SetAttributesFunction.Modifier(
				this.name, this.attribute, this.operation, this.amount, (EquipmentSlot[])this.slots.toArray(new EquipmentSlot[0]), this.id
			);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetAttributesFunction> {
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
