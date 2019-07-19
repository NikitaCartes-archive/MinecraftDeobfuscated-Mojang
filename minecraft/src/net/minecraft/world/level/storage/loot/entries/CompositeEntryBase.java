package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase extends LootPoolEntryContainer {
	protected final LootPoolEntryContainer[] children;
	private final ComposableEntryContainer composedChildren;

	protected CompositeEntryBase(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions) {
		super(lootItemConditions);
		this.children = lootPoolEntryContainers;
		this.composedChildren = this.compose(lootPoolEntryContainers);
	}

	@Override
	public void validate(
		LootTableProblemCollector lootTableProblemCollector,
		Function<ResourceLocation, LootTable> function,
		Set<ResourceLocation> set,
		LootContextParamSet lootContextParamSet
	) {
		super.validate(lootTableProblemCollector, function, set, lootContextParamSet);
		if (this.children.length == 0) {
			lootTableProblemCollector.reportProblem("Empty children list");
		}

		for (int i = 0; i < this.children.length; i++) {
			this.children[i].validate(lootTableProblemCollector.forChild(".entry[" + i + "]"), function, set, lootContextParamSet);
		}
	}

	protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainers);

	@Override
	public final boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		return !this.canRun(lootContext) ? false : this.composedChildren.expand(lootContext, consumer);
	}

	public static <T extends CompositeEntryBase> CompositeEntryBase.Serializer<T> createSerializer(
		ResourceLocation resourceLocation, Class<T> class_, CompositeEntryBase.CompositeEntryConstructor<T> compositeEntryConstructor
	) {
		return new CompositeEntryBase.Serializer<T>(resourceLocation, class_) {
			@Override
			protected T deserialize(
				JsonObject jsonObject,
				JsonDeserializationContext jsonDeserializationContext,
				LootPoolEntryContainer[] lootPoolEntryContainers,
				LootItemCondition[] lootItemConditions
			) {
				return compositeEntryConstructor.create(lootPoolEntryContainers, lootItemConditions);
			}
		};
	}

	@FunctionalInterface
	public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
		T create(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions);
	}

	public abstract static class Serializer<T extends CompositeEntryBase> extends LootPoolEntryContainer.Serializer<T> {
		public Serializer(ResourceLocation resourceLocation, Class<T> class_) {
			super(resourceLocation, class_);
		}

		public void serialize(JsonObject jsonObject, T compositeEntryBase, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("children", jsonSerializationContext.serialize(compositeEntryBase.children));
		}

		public final T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			LootPoolEntryContainer[] lootPoolEntryContainers = GsonHelper.getAsObject(jsonObject, "children", jsonDeserializationContext, LootPoolEntryContainer[].class);
			return this.deserialize(jsonObject, jsonDeserializationContext, lootPoolEntryContainers, lootItemConditions);
		}

		protected abstract T deserialize(
			JsonObject jsonObject,
			JsonDeserializationContext jsonDeserializationContext,
			LootPoolEntryContainer[] lootPoolEntryContainers,
			LootItemCondition[] lootItemConditions
		);
	}
}
