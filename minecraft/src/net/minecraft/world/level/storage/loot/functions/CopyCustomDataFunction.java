package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import org.apache.commons.lang3.mutable.MutableObject;

public class CopyCustomDataFunction extends LootItemConditionalFunction {
	public static final MapCodec<CopyCustomDataFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<NbtProvider, List<CopyCustomDataFunction.CopyOperation>>and(
					instance.group(
						NbtProviders.CODEC.fieldOf("source").forGetter(copyCustomDataFunction -> copyCustomDataFunction.source),
						CopyCustomDataFunction.CopyOperation.CODEC.listOf().fieldOf("ops").forGetter(copyCustomDataFunction -> copyCustomDataFunction.operations)
					)
				)
				.apply(instance, CopyCustomDataFunction::new)
	);
	private final NbtProvider source;
	private final List<CopyCustomDataFunction.CopyOperation> operations;

	CopyCustomDataFunction(List<LootItemCondition> list, NbtProvider nbtProvider, List<CopyCustomDataFunction.CopyOperation> list2) {
		super(list);
		this.source = nbtProvider;
		this.operations = List.copyOf(list2);
	}

	@Override
	public LootItemFunctionType<CopyCustomDataFunction> getType() {
		return LootItemFunctions.COPY_CUSTOM_DATA;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return this.source.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Tag tag = this.source.get(lootContext);
		if (tag == null) {
			return itemStack;
		} else {
			MutableObject<CompoundTag> mutableObject = new MutableObject<>();
			Supplier<Tag> supplier = () -> {
				if (mutableObject.getValue() == null) {
					mutableObject.setValue(itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
				}

				return mutableObject.getValue();
			};
			this.operations.forEach(copyOperation -> copyOperation.apply(supplier, tag));
			CompoundTag compoundTag = mutableObject.getValue();
			if (compoundTag != null) {
				CustomData.set(DataComponents.CUSTOM_DATA, itemStack, compoundTag);
			}

			return itemStack;
		}
	}

	@Deprecated
	public static CopyCustomDataFunction.Builder copyData(NbtProvider nbtProvider) {
		return new CopyCustomDataFunction.Builder(nbtProvider);
	}

	public static CopyCustomDataFunction.Builder copyData(LootContext.EntityTarget entityTarget) {
		return new CopyCustomDataFunction.Builder(ContextNbtProvider.forContextEntity(entityTarget));
	}

	public static class Builder extends LootItemConditionalFunction.Builder<CopyCustomDataFunction.Builder> {
		private final NbtProvider source;
		private final List<CopyCustomDataFunction.CopyOperation> ops = Lists.<CopyCustomDataFunction.CopyOperation>newArrayList();

		Builder(NbtProvider nbtProvider) {
			this.source = nbtProvider;
		}

		public CopyCustomDataFunction.Builder copy(String string, String string2, CopyCustomDataFunction.MergeStrategy mergeStrategy) {
			try {
				this.ops.add(new CopyCustomDataFunction.CopyOperation(NbtPathArgument.NbtPath.of(string), NbtPathArgument.NbtPath.of(string2), mergeStrategy));
				return this;
			} catch (CommandSyntaxException var5) {
				throw new IllegalArgumentException(var5);
			}
		}

		public CopyCustomDataFunction.Builder copy(String string, String string2) {
			return this.copy(string, string2, CopyCustomDataFunction.MergeStrategy.REPLACE);
		}

		protected CopyCustomDataFunction.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new CopyCustomDataFunction(this.getConditions(), this.source, this.ops);
		}
	}

	static record CopyOperation(NbtPathArgument.NbtPath sourcePath, NbtPathArgument.NbtPath targetPath, CopyCustomDataFunction.MergeStrategy op) {
		public static final Codec<CopyCustomDataFunction.CopyOperation> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						NbtPathArgument.NbtPath.CODEC.fieldOf("source").forGetter(CopyCustomDataFunction.CopyOperation::sourcePath),
						NbtPathArgument.NbtPath.CODEC.fieldOf("target").forGetter(CopyCustomDataFunction.CopyOperation::targetPath),
						CopyCustomDataFunction.MergeStrategy.CODEC.fieldOf("op").forGetter(CopyCustomDataFunction.CopyOperation::op)
					)
					.apply(instance, CopyCustomDataFunction.CopyOperation::new)
		);

		public void apply(Supplier<Tag> supplier, Tag tag) {
			try {
				List<Tag> list = this.sourcePath.get(tag);
				if (!list.isEmpty()) {
					this.op.merge((Tag)supplier.get(), this.targetPath, list);
				}
			} catch (CommandSyntaxException var4) {
			}
		}
	}

	public static enum MergeStrategy implements StringRepresentable {
		REPLACE("replace") {
			@Override
			public void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
				nbtPath.set(tag, Iterables.getLast(list));
			}
		},
		APPEND("append") {
			@Override
			public void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
				List<Tag> list2 = nbtPath.getOrCreate(tag, ListTag::new);
				list2.forEach(tagx -> {
					if (tagx instanceof ListTag) {
						list.forEach(tag2 -> ((ListTag)tagx).add(tag2.copy()));
					}
				});
			}
		},
		MERGE("merge") {
			@Override
			public void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
				List<Tag> list2 = nbtPath.getOrCreate(tag, CompoundTag::new);
				list2.forEach(tagx -> {
					if (tagx instanceof CompoundTag) {
						list.forEach(tag2 -> {
							if (tag2 instanceof CompoundTag) {
								((CompoundTag)tagx).merge((CompoundTag)tag2);
							}
						});
					}
				});
			}
		};

		public static final Codec<CopyCustomDataFunction.MergeStrategy> CODEC = StringRepresentable.fromEnum(CopyCustomDataFunction.MergeStrategy::values);
		private final String name;

		public abstract void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException;

		MergeStrategy(final String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
