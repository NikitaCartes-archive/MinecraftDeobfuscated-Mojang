package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;

public class CopyNbtFunction extends LootItemConditionalFunction {
	public static final Codec<CopyNbtFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<NbtProvider, List<CopyNbtFunction.CopyOperation>>and(
					instance.group(
						NbtProviders.CODEC.fieldOf("source").forGetter(copyNbtFunction -> copyNbtFunction.source),
						CopyNbtFunction.CopyOperation.CODEC.listOf().fieldOf("ops").forGetter(copyNbtFunction -> copyNbtFunction.operations)
					)
				)
				.apply(instance, CopyNbtFunction::new)
	);
	private final NbtProvider source;
	private final List<CopyNbtFunction.CopyOperation> operations;

	CopyNbtFunction(List<LootItemCondition> list, NbtProvider nbtProvider, List<CopyNbtFunction.CopyOperation> list2) {
		super(list);
		this.source = nbtProvider;
		this.operations = List.copyOf(list2);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.COPY_NBT;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.source.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Tag tag = this.source.get(lootContext);
		if (tag != null) {
			this.operations.forEach(copyOperation -> copyOperation.apply(itemStack::getOrCreateTag, tag));
		}

		return itemStack;
	}

	public static CopyNbtFunction.Builder copyData(NbtProvider nbtProvider) {
		return new CopyNbtFunction.Builder(nbtProvider);
	}

	public static CopyNbtFunction.Builder copyData(LootContext.EntityTarget entityTarget) {
		return new CopyNbtFunction.Builder(ContextNbtProvider.forContextEntity(entityTarget));
	}

	public static class Builder extends LootItemConditionalFunction.Builder<CopyNbtFunction.Builder> {
		private final NbtProvider source;
		private final List<CopyNbtFunction.CopyOperation> ops = Lists.<CopyNbtFunction.CopyOperation>newArrayList();

		Builder(NbtProvider nbtProvider) {
			this.source = nbtProvider;
		}

		public CopyNbtFunction.Builder copy(String string, String string2, CopyNbtFunction.MergeStrategy mergeStrategy) {
			try {
				this.ops.add(new CopyNbtFunction.CopyOperation(CopyNbtFunction.Path.of(string), CopyNbtFunction.Path.of(string2), mergeStrategy));
				return this;
			} catch (CommandSyntaxException var5) {
				throw new IllegalArgumentException(var5);
			}
		}

		public CopyNbtFunction.Builder copy(String string, String string2) {
			return this.copy(string, string2, CopyNbtFunction.MergeStrategy.REPLACE);
		}

		protected CopyNbtFunction.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new CopyNbtFunction(this.getConditions(), this.source, this.ops);
		}
	}

	static record CopyOperation(CopyNbtFunction.Path sourcePath, CopyNbtFunction.Path targetPath, CopyNbtFunction.MergeStrategy op) {
		public static final Codec<CopyNbtFunction.CopyOperation> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						CopyNbtFunction.Path.CODEC.fieldOf("source").forGetter(CopyNbtFunction.CopyOperation::sourcePath),
						CopyNbtFunction.Path.CODEC.fieldOf("target").forGetter(CopyNbtFunction.CopyOperation::targetPath),
						CopyNbtFunction.MergeStrategy.CODEC.fieldOf("op").forGetter(CopyNbtFunction.CopyOperation::op)
					)
					.apply(instance, CopyNbtFunction.CopyOperation::new)
		);

		public void apply(Supplier<Tag> supplier, Tag tag) {
			try {
				List<Tag> list = this.sourcePath.path().get(tag);
				if (!list.isEmpty()) {
					this.op.merge((Tag)supplier.get(), this.targetPath.path(), list);
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

		public static final Codec<CopyNbtFunction.MergeStrategy> CODEC = StringRepresentable.fromEnum(CopyNbtFunction.MergeStrategy::values);
		private final String name;

		public abstract void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException;

		MergeStrategy(String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	static record Path(String string, NbtPathArgument.NbtPath path) {
		public static final Codec<CopyNbtFunction.Path> CODEC = Codec.STRING.comapFlatMap(string -> {
			try {
				return DataResult.success(of(string));
			} catch (CommandSyntaxException var2) {
				return DataResult.error(() -> "Failed to parse path " + string + ": " + var2.getMessage());
			}
		}, CopyNbtFunction.Path::string);

		public static CopyNbtFunction.Path of(String string) throws CommandSyntaxException {
			NbtPathArgument.NbtPath nbtPath = new NbtPathArgument().parse(new StringReader(string));
			return new CopyNbtFunction.Path(string, nbtPath);
		}
	}
}
