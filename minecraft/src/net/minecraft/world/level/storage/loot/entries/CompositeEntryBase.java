package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
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
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);
		if (this.children.length == 0) {
			validationContext.reportProblem("Empty children list");
		}

		for (int i = 0; i < this.children.length; i++) {
			this.children[i].validate(validationContext.forChild(".entry[" + i + "]"));
		}
	}

	protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainers);

	@Override
	public final boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		return !this.canRun(lootContext) ? false : this.composedChildren.expand(lootContext, consumer);
	}

	public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> createSerializer(
		CompositeEntryBase.CompositeEntryConstructor<T> compositeEntryConstructor
	) {
		return new LootPoolEntryContainer.Serializer<T>() {
			public void serializeCustom(JsonObject jsonObject, T compositeEntryBase, JsonSerializationContext jsonSerializationContext) {
				jsonObject.add("children", jsonSerializationContext.serialize(compositeEntryBase.children));
			}

			public final T deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
				LootPoolEntryContainer[] lootPoolEntryContainers = GsonHelper.getAsObject(
					jsonObject, "children", jsonDeserializationContext, LootPoolEntryContainer[].class
				);
				return compositeEntryConstructor.create(lootPoolEntryContainers, lootItemConditions);
			}
		};
	}

	@FunctionalInterface
	public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
		T create(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions);
	}
}
