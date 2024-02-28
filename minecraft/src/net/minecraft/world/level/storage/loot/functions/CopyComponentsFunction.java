package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyComponentsFunction extends LootItemConditionalFunction {
	public static final Codec<CopyComponentsFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<CopyComponentsFunction.Source, List<DataComponentType<?>>>and(
					instance.group(
						CopyComponentsFunction.Source.CODEC.fieldOf("source").forGetter(copyComponentsFunction -> copyComponentsFunction.source),
						DataComponentType.CODEC.listOf().fieldOf("components").forGetter(copyComponentsFunction -> copyComponentsFunction.components)
					)
				)
				.apply(instance, CopyComponentsFunction::new)
	);
	private final CopyComponentsFunction.Source source;
	private final List<DataComponentType<?>> components;

	CopyComponentsFunction(List<LootItemCondition> list, CopyComponentsFunction.Source source, List<DataComponentType<?>> list2) {
		super(list);
		this.source = source;
		this.components = List.copyOf(list2);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.COPY_COMPONENTS;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.source.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		DataComponentMap dataComponentMap = this.source.get(lootContext);
		itemStack.applyComponents(dataComponentMap.filter(this.components::contains));
		return itemStack;
	}

	public static CopyComponentsFunction.Builder copyComponents(CopyComponentsFunction.Source source) {
		return new CopyComponentsFunction.Builder(source);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<CopyComponentsFunction.Builder> {
		private final CopyComponentsFunction.Source source;
		private final ImmutableList.Builder<DataComponentType<?>> components = ImmutableList.builder();

		Builder(CopyComponentsFunction.Source source) {
			this.source = source;
		}

		public CopyComponentsFunction.Builder copy(DataComponentType<?> dataComponentType) {
			this.components.add(dataComponentType);
			return this;
		}

		protected CopyComponentsFunction.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new CopyComponentsFunction(this.getConditions(), this.source, this.components.build());
		}
	}

	public static enum Source implements StringRepresentable {
		BLOCK_ENTITY("block_entity");

		public static final Codec<CopyComponentsFunction.Source> CODEC = StringRepresentable.fromValues(CopyComponentsFunction.Source::values);
		private final String name;

		private Source(String string2) {
			this.name = string2;
		}

		public DataComponentMap get(LootContext lootContext) {
			switch (this) {
				case BLOCK_ENTITY:
					BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
					return blockEntity != null ? blockEntity.collectComponents() : DataComponentMap.EMPTY;
				default:
					throw new IncompatibleClassChangeError();
			}
		}

		public Set<LootContextParam<?>> getReferencedContextParams() {
			switch (this) {
				case BLOCK_ENTITY:
					return Set.of(LootContextParams.BLOCK_ENTITY);
				default:
					throw new IncompatibleClassChangeError();
			}
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
