package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetItemDamageFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogManager.getLogger();
	final NumberProvider damage;
	final boolean add;

	SetItemDamageFunction(LootItemCondition[] lootItemConditions, NumberProvider numberProvider, boolean bl) {
		super(lootItemConditions);
		this.damage = numberProvider;
		this.add = bl;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_DAMAGE;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.damage.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isDamageableItem()) {
			int i = itemStack.getMaxDamage();
			float f = this.add ? 1.0F - (float)itemStack.getDamageValue() / (float)i : 0.0F;
			float g = 1.0F - Mth.clamp(this.damage.getFloat(lootContext) + f, 0.0F, 1.0F);
			itemStack.setDamageValue(Mth.floor(g * (float)i));
		} else {
			LOGGER.warn("Couldn't set damage of loot item {}", itemStack);
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberProvider) {
		return simpleBuilder(lootItemConditions -> new SetItemDamageFunction(lootItemConditions, numberProvider, false));
	}

	public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberProvider, boolean bl) {
		return simpleBuilder(lootItemConditions -> new SetItemDamageFunction(lootItemConditions, numberProvider, bl));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemDamageFunction> {
		public void serialize(JsonObject jsonObject, SetItemDamageFunction setItemDamageFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setItemDamageFunction, jsonSerializationContext);
			jsonObject.add("damage", jsonSerializationContext.serialize(setItemDamageFunction.damage));
			jsonObject.addProperty("add", setItemDamageFunction.add);
		}

		public SetItemDamageFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "damage", jsonDeserializationContext, NumberProvider.class);
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "add", false);
			return new SetItemDamageFunction(lootItemConditions, numberProvider, bl);
		}
	}
}
