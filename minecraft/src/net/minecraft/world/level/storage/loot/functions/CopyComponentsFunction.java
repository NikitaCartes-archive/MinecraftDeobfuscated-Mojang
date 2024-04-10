package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.Util;
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
	public static final MapCodec<CopyComponentsFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<CopyComponentsFunction.Source, Optional<List<DataComponentType<?>>>, Optional<List<DataComponentType<?>>>>and(
					instance.group(
						CopyComponentsFunction.Source.CODEC.fieldOf("source").forGetter(copyComponentsFunction -> copyComponentsFunction.source),
						DataComponentType.CODEC.listOf().optionalFieldOf("include").forGetter(copyComponentsFunction -> copyComponentsFunction.include),
						DataComponentType.CODEC.listOf().optionalFieldOf("exclude").forGetter(copyComponentsFunction -> copyComponentsFunction.exclude)
					)
				)
				.apply(instance, CopyComponentsFunction::new)
	);
	private final CopyComponentsFunction.Source source;
	private final Optional<List<DataComponentType<?>>> include;
	private final Optional<List<DataComponentType<?>>> exclude;
	private final Predicate<DataComponentType<?>> bakedPredicate;

	CopyComponentsFunction(
		List<LootItemCondition> list,
		CopyComponentsFunction.Source source,
		Optional<List<DataComponentType<?>>> optional,
		Optional<List<DataComponentType<?>>> optional2
	) {
		super(list);
		this.source = source;
		this.include = optional.map(List::copyOf);
		this.exclude = optional2.map(List::copyOf);
		List<Predicate<DataComponentType<?>>> list2 = new ArrayList(2);
		optional2.ifPresent(list2x -> list2.add((Predicate)dataComponentType -> !list2x.contains(dataComponentType)));
		optional.ifPresent(list2x -> list2.add(list2x::contains));
		this.bakedPredicate = Util.allOf(list2);
	}

	@Override
	public LootItemFunctionType<CopyComponentsFunction> getType() {
		return LootItemFunctions.COPY_COMPONENTS;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.source.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		DataComponentMap dataComponentMap = this.source.get(lootContext);
		itemStack.applyComponents(dataComponentMap.filter(this.bakedPredicate));
		return itemStack;
	}

	public static CopyComponentsFunction.Builder copyComponents(CopyComponentsFunction.Source source) {
		return new CopyComponentsFunction.Builder(source);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<CopyComponentsFunction.Builder> {
		private final CopyComponentsFunction.Source source;
		private Optional<ImmutableList.Builder<DataComponentType<?>>> include = Optional.empty();
		private Optional<ImmutableList.Builder<DataComponentType<?>>> exclude = Optional.empty();

		Builder(CopyComponentsFunction.Source source) {
			this.source = source;
		}

		public CopyComponentsFunction.Builder include(DataComponentType<?> dataComponentType) {
			if (this.include.isEmpty()) {
				this.include = Optional.of(ImmutableList.builder());
			}

			((ImmutableList.Builder)this.include.get()).add(dataComponentType);
			return this;
		}

		public CopyComponentsFunction.Builder exclude(DataComponentType<?> dataComponentType) {
			if (this.exclude.isEmpty()) {
				this.exclude = Optional.of(ImmutableList.builder());
			}

			((ImmutableList.Builder)this.exclude.get()).add(dataComponentType);
			return this;
		}

		protected CopyComponentsFunction.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new CopyComponentsFunction(
				this.getConditions(), this.source, this.include.map(ImmutableList.Builder::build), this.exclude.map(ImmutableList.Builder::build)
			);
		}
	}

	public static enum Source implements StringRepresentable {
		BLOCK_ENTITY("block_entity");

		public static final Codec<CopyComponentsFunction.Source> CODEC = StringRepresentable.fromValues(CopyComponentsFunction.Source::values);
		private final String name;

		private Source(final String string2) {
			this.name = string2;
		}

		public DataComponentMap get(LootContext lootContext) {
			switch (this) {
				case BLOCK_ENTITY:
					BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
					return blockEntity != null ? blockEntity.collectComponents() : DataComponentMap.EMPTY;
				default:
					throw new MatchException(null, null);
			}
		}

		public Set<LootContextParam<?>> getReferencedContextParams() {
			switch (this) {
				case BLOCK_ENTITY:
					return Set.of(LootContextParams.BLOCK_ENTITY);
				default:
					throw new MatchException(null, null);
			}
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
