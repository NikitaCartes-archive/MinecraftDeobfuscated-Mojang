package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase extends LootPoolEntryContainer {
	protected final List<LootPoolEntryContainer> children;
	private final ComposableEntryContainer composedChildren;

	protected CompositeEntryBase(List<LootPoolEntryContainer> list, List<LootItemCondition> list2) {
		super(list2);
		this.children = list;
		this.composedChildren = this.compose(list);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);
		if (this.children.isEmpty()) {
			validationContext.reportProblem("Empty children list");
		}

		for (int i = 0; i < this.children.size(); i++) {
			((LootPoolEntryContainer)this.children.get(i)).validate(validationContext.forChild(".entry[" + i + "]"));
		}
	}

	protected abstract ComposableEntryContainer compose(List<? extends ComposableEntryContainer> list);

	@Override
	public final boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		return !this.canRun(lootContext) ? false : this.composedChildren.expand(lootContext, consumer);
	}

	public static <T extends CompositeEntryBase> MapCodec<T> createCodec(CompositeEntryBase.CompositeEntryConstructor<T> compositeEntryConstructor) {
		return RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						LootPoolEntries.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(compositeEntryBase -> compositeEntryBase.children)
					)
					.and(commonFields(instance).t1())
					.apply(instance, compositeEntryConstructor::create)
		);
	}

	@FunctionalInterface
	public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
		T create(List<LootPoolEntryContainer> list, List<LootItemCondition> list2);
	}
}
