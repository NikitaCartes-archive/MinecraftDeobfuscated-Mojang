package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetLoreFunction extends LootItemConditionalFunction {
	public static final Codec<SetLoreFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<boolean, List<Component>, Optional<LootContext.EntityTarget>>and(
					instance.group(
						Codec.BOOL.fieldOf("replace").orElse(false).forGetter(setLoreFunction -> setLoreFunction.replace),
						ComponentSerialization.CODEC.listOf().fieldOf("lore").forGetter(setLoreFunction -> setLoreFunction.lore),
						ExtraCodecs.strictOptionalField(LootContext.EntityTarget.CODEC, "entity").forGetter(setLoreFunction -> setLoreFunction.resolutionContext)
					)
				)
				.apply(instance, SetLoreFunction::new)
	);
	private final boolean replace;
	private final List<Component> lore;
	private final Optional<LootContext.EntityTarget> resolutionContext;

	public SetLoreFunction(List<LootItemCondition> list, boolean bl, List<Component> list2, Optional<LootContext.EntityTarget> optional) {
		super(list);
		this.replace = bl;
		this.lore = List.copyOf(list2);
		this.resolutionContext = optional;
	}

	@Override
	public LootItemFunctionType getType() {
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
			Stream<Component> stream = this.lore.stream().map(unaryOperator);
			return !this.replace && itemLore != null ? Stream.concat(itemLore.lines().stream(), stream).toList() : stream.toList();
		}
	}

	public static SetLoreFunction.Builder setLore() {
		return new SetLoreFunction.Builder();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetLoreFunction.Builder> {
		private boolean replace;
		private Optional<LootContext.EntityTarget> resolutionContext = Optional.empty();
		private final ImmutableList.Builder<Component> lore = ImmutableList.builder();

		public SetLoreFunction.Builder setReplace(boolean bl) {
			this.replace = bl;
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
			return new SetLoreFunction(this.getConditions(), this.replace, this.lore.build(), this.resolutionContext);
		}
	}
}
