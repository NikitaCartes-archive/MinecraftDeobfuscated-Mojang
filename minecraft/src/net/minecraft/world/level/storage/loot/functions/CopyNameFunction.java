package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction extends LootItemConditionalFunction {
	private final CopyNameFunction.NameSource source;

	private CopyNameFunction(LootItemCondition[] lootItemConditions, CopyNameFunction.NameSource nameSource) {
		super(lootItemConditions);
		this.source = nameSource;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(this.source.param);
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Object object = lootContext.getParamOrNull(this.source.param);
		if (object instanceof Nameable) {
			Nameable nameable = (Nameable)object;
			if (nameable.hasCustomName()) {
				itemStack.setHoverName(nameable.getDisplayName());
			}
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> copyName(CopyNameFunction.NameSource nameSource) {
		return simpleBuilder(lootItemConditions -> new CopyNameFunction(lootItemConditions, nameSource));
	}

	public static enum NameSource {
		THIS("this", LootContextParams.THIS_ENTITY),
		KILLER("killer", LootContextParams.KILLER_ENTITY),
		KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER),
		BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

		public final String name;
		public final LootContextParam<?> param;

		private NameSource(String string2, LootContextParam<?> lootContextParam) {
			this.name = string2;
			this.param = lootContextParam;
		}

		public static CopyNameFunction.NameSource getByName(String string) {
			for (CopyNameFunction.NameSource nameSource : values()) {
				if (nameSource.name.equals(string)) {
					return nameSource;
				}
			}

			throw new IllegalArgumentException("Invalid name source " + string);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNameFunction> {
		public Serializer() {
			super(new ResourceLocation("copy_name"), CopyNameFunction.class);
		}

		public void serialize(JsonObject jsonObject, CopyNameFunction copyNameFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, copyNameFunction, jsonSerializationContext);
			jsonObject.addProperty("source", copyNameFunction.source.name);
		}

		public CopyNameFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			CopyNameFunction.NameSource nameSource = CopyNameFunction.NameSource.getByName(GsonHelper.getAsString(jsonObject, "source"));
			return new CopyNameFunction(lootItemConditions, nameSource);
		}
	}
}
