package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetLoreFunction extends LootItemConditionalFunction {
	private final boolean replace;
	private final List<Component> lore;
	@Nullable
	private final LootContext.EntityTarget resolutionContext;

	public SetLoreFunction(LootItemCondition[] lootItemConditions, boolean bl, List<Component> list, @Nullable LootContext.EntityTarget entityTarget) {
		super(lootItemConditions);
		this.replace = bl;
		this.lore = ImmutableList.copyOf(list);
		this.resolutionContext = entityTarget;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		ListTag listTag = this.getLoreTag(itemStack, !this.lore.isEmpty());
		if (listTag != null) {
			if (this.replace) {
				listTag.clear();
			}

			UnaryOperator<Component> unaryOperator = SetNameFunction.createResolver(lootContext, this.resolutionContext);
			this.lore.stream().map(unaryOperator).map(Component.Serializer::toJson).map(StringTag::new).forEach(listTag::add);
		}

		return itemStack;
	}

	@Nullable
	private ListTag getLoreTag(ItemStack itemStack, boolean bl) {
		CompoundTag compoundTag;
		if (itemStack.hasTag()) {
			compoundTag = itemStack.getTag();
		} else {
			if (!bl) {
				return null;
			}

			compoundTag = new CompoundTag();
			itemStack.setTag(compoundTag);
		}

		CompoundTag compoundTag2;
		if (compoundTag.contains("display", 10)) {
			compoundTag2 = compoundTag.getCompound("display");
		} else {
			if (!bl) {
				return null;
			}

			compoundTag2 = new CompoundTag();
			compoundTag.put("display", compoundTag2);
		}

		if (compoundTag2.contains("Lore", 9)) {
			return compoundTag2.getList("Lore", 8);
		} else if (bl) {
			ListTag listTag = new ListTag();
			compoundTag2.put("Lore", listTag);
			return listTag;
		} else {
			return null;
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetLoreFunction> {
		public Serializer() {
			super(new ResourceLocation("set_lore"), SetLoreFunction.class);
		}

		public void serialize(JsonObject jsonObject, SetLoreFunction setLoreFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setLoreFunction, jsonSerializationContext);
			jsonObject.addProperty("replace", setLoreFunction.replace);
			JsonArray jsonArray = new JsonArray();

			for (Component component : setLoreFunction.lore) {
				jsonArray.add(Component.Serializer.toJsonTree(component));
			}

			jsonObject.add("lore", jsonArray);
			if (setLoreFunction.resolutionContext != null) {
				jsonObject.add("entity", jsonSerializationContext.serialize(setLoreFunction.resolutionContext));
			}
		}

		public SetLoreFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "replace", false);
			List<Component> list = (List<Component>)Streams.stream(GsonHelper.getAsJsonArray(jsonObject, "lore"))
				.map(Component.Serializer::fromJson)
				.collect(ImmutableList.toImmutableList());
			LootContext.EntityTarget entityTarget = GsonHelper.getAsObject(jsonObject, "entity", null, jsonDeserializationContext, LootContext.EntityTarget.class);
			return new SetLoreFunction(lootItemConditions, bl, list, entityTarget);
		}
	}
}
