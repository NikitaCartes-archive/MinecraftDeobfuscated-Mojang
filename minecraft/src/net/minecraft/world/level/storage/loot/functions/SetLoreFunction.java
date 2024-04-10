package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetLoreFunction extends LootItemConditionalFunction {
	public static final MapCodec<SetLoreFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<List<Component>, ListOperation, Optional<LootContext.EntityTarget>>and(
					instance.group(
						ComponentSerialization.CODEC.sizeLimitedListOf(256).fieldOf("lore").forGetter(setLoreFunction -> setLoreFunction.lore),
						ListOperation.codec(256).forGetter(setLoreFunction -> setLoreFunction.mode),
						LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(setLoreFunction -> setLoreFunction.resolutionContext)
					)
				)
				.apply(instance, SetLoreFunction::new)
	);
	private final List<Component> lore;
	private final ListOperation mode;
	private final Optional<LootContext.EntityTarget> resolutionContext;

	public SetLoreFunction(List<LootItemCondition> list, List<Component> list2, ListOperation listOperation, Optional<LootContext.EntityTarget> optional) {
		super(list);
		this.lore = List.copyOf(list2);
		this.mode = listOperation;
		this.resolutionContext = optional;
	}

	@Override
	public LootItemFunctionType<SetLoreFunction> getType() {
		return LootItemFunctions.SET_LORE;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return (Set<LootContextParam<?>>)this.resolutionContext.map(entityTarget -> Set.of(entityTarget.getParam())).orElseGet(Set::of);
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.update(DataComponents.LORE, ItemLore.EMPTY, itemLore -> new ItemLore(this.updateLore(itemLore, lootContext)));
		return itemStack;
	}

	private List<Component> updateLore(@Nullable ItemLore itemLore, LootContext lootContext) {
		if (itemLore == null && this.lore.isEmpty()) {
			return List.of();
		} else {
			UnaryOperator<Component> unaryOperator = SetNameFunction.createResolver(lootContext, (LootContext.EntityTarget)this.resolutionContext.orElse(null));
			List<Component> list = this.lore.stream().map(unaryOperator).toList();
			return this.mode.apply(itemLore.lines(), list, 256);
		}
	}

	public static SetLoreFunction.Builder setLore() {
		return new SetLoreFunction.Builder();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetLoreFunction.Builder> {
		private Optional<LootContext.EntityTarget> resolutionContext = Optional.empty();
		private final ImmutableList.Builder<Component> lore = ImmutableList.builder();
		private ListOperation mode = ListOperation.Append.INSTANCE;

		public SetLoreFunction.Builder setMode(ListOperation listOperation) {
			this.mode = listOperation;
			return this;
		}

		public SetLoreFunction.Builder setResolutionContext(LootContext.EntityTarget entityTarget) {
			this.resolutionContext = Optional.of(entityTarget);
			return this;
		}

		public SetLoreFunction.Builder addLine(Component component) {
			this.lore.add(component);
			return this;
		}

		protected SetLoreFunction.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetLoreFunction(this.getConditions(), this.lore.build(), this.mode, this.resolutionContext);
		}
	}
}
