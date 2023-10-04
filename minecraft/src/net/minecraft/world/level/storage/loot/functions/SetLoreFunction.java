package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
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
		ListTag listTag = this.getLoreTag(itemStack, !this.lore.isEmpty());
		if (listTag != null) {
			if (this.replace) {
				listTag.clear();
			}

			UnaryOperator<Component> unaryOperator = SetNameFunction.createResolver(lootContext, (LootContext.EntityTarget)this.resolutionContext.orElse(null));
			this.lore.stream().map(unaryOperator).map(Component.Serializer::toJson).map(StringTag::valueOf).forEach(listTag::add);
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
