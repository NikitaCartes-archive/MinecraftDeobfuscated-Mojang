package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

public class CopyNbtFunction extends LootItemConditionalFunction {
	final NbtProvider source;
	final List<CopyNbtFunction.CopyOperation> operations;

	CopyNbtFunction(LootItemCondition[] lootItemConditions, NbtProvider nbtProvider, List<CopyNbtFunction.CopyOperation> list) {
		super(lootItemConditions);
		this.source = nbtProvider;
		this.operations = ImmutableList.copyOf(list);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.COPY_NBT;
	}

	static NbtPathArgument.NbtPath compileNbtPath(String string) {
		try {
			return new NbtPathArgument().parse(new StringReader(string));
		} catch (CommandSyntaxException var2) {
			throw new IllegalArgumentException("Failed to parse path " + string, var2);
		}
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
			this.ops.add(new CopyNbtFunction.CopyOperation(string, string2, mergeStrategy));
			return this;
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

	static class CopyOperation {
		private final String sourcePathText;
		private final NbtPathArgument.NbtPath sourcePath;
		private final String targetPathText;
		private final NbtPathArgument.NbtPath targetPath;
		private final CopyNbtFunction.MergeStrategy op;

		CopyOperation(String string, String string2, CopyNbtFunction.MergeStrategy mergeStrategy) {
			this.sourcePathText = string;
			this.sourcePath = CopyNbtFunction.compileNbtPath(string);
			this.targetPathText = string2;
			this.targetPath = CopyNbtFunction.compileNbtPath(string2);
			this.op = mergeStrategy;
		}

		public void apply(Supplier<Tag> supplier, Tag tag) {
			try {
				List<Tag> list = this.sourcePath.get(tag);
				if (!list.isEmpty()) {
					this.op.merge((Tag)supplier.get(), this.targetPath, list);
				}
			} catch (CommandSyntaxException var4) {
			}
		}

		public JsonObject toJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("source", this.sourcePathText);
			jsonObject.addProperty("target", this.targetPathText);
			jsonObject.addProperty("op", this.op.name);
			return jsonObject;
		}

		public static CopyNbtFunction.CopyOperation fromJson(JsonObject jsonObject) {
			String string = GsonHelper.getAsString(jsonObject, "source");
			String string2 = GsonHelper.getAsString(jsonObject, "target");
			CopyNbtFunction.MergeStrategy mergeStrategy = CopyNbtFunction.MergeStrategy.getByName(GsonHelper.getAsString(jsonObject, "op"));
			return new CopyNbtFunction.CopyOperation(string, string2, mergeStrategy);
		}
	}

	public static enum MergeStrategy {
		REPLACE("replace") {
			@Override
			public void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
				nbtPath.set(tag, Iterables.getLast(list)::copy);
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

		final String name;

		public abstract void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException;

		MergeStrategy(String string2) {
			this.name = string2;
		}

		public static CopyNbtFunction.MergeStrategy getByName(String string) {
			for (CopyNbtFunction.MergeStrategy mergeStrategy : values()) {
				if (mergeStrategy.name.equals(string)) {
					return mergeStrategy;
				}
			}

			throw new IllegalArgumentException("Invalid merge strategy" + string);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNbtFunction> {
		public void serialize(JsonObject jsonObject, CopyNbtFunction copyNbtFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, copyNbtFunction, jsonSerializationContext);
			jsonObject.add("source", jsonSerializationContext.serialize(copyNbtFunction.source));
			JsonArray jsonArray = new JsonArray();
			copyNbtFunction.operations.stream().map(CopyNbtFunction.CopyOperation::toJson).forEach(jsonArray::add);
			jsonObject.add("ops", jsonArray);
		}

		public CopyNbtFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			NbtProvider nbtProvider = GsonHelper.getAsObject(jsonObject, "source", jsonDeserializationContext, NbtProvider.class);
			List<CopyNbtFunction.CopyOperation> list = Lists.<CopyNbtFunction.CopyOperation>newArrayList();

			for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "ops")) {
				JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonElement, "op");
				list.add(CopyNbtFunction.CopyOperation.fromJson(jsonObject2));
			}

			return new CopyNbtFunction(lootItemConditions, nbtProvider, list);
		}
	}
}
