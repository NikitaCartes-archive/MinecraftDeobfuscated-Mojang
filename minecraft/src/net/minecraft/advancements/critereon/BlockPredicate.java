package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicate {
	public static final BlockPredicate ANY = new BlockPredicate(null, null, StatePropertiesPredicate.ANY, NbtPredicate.ANY);
	@Nullable
	private final Tag<Block> tag;
	@Nullable
	private final Block block;
	private final StatePropertiesPredicate properties;
	private final NbtPredicate nbt;

	public BlockPredicate(@Nullable Tag<Block> tag, @Nullable Block block, StatePropertiesPredicate statePropertiesPredicate, NbtPredicate nbtPredicate) {
		this.tag = tag;
		this.block = block;
		this.properties = statePropertiesPredicate;
		this.nbt = nbtPredicate;
	}

	public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
		if (this == ANY) {
			return true;
		} else if (!serverLevel.isLoaded(blockPos)) {
			return false;
		} else {
			BlockState blockState = serverLevel.getBlockState(blockPos);
			if (this.tag != null && !blockState.is(this.tag)) {
				return false;
			} else if (this.block != null && !blockState.is(this.block)) {
				return false;
			} else if (!this.properties.matches(blockState)) {
				return false;
			} else {
				if (this.nbt != NbtPredicate.ANY) {
					BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
					if (blockEntity == null || !this.nbt.matches(blockEntity.save(new CompoundTag()))) {
						return false;
					}
				}

				return true;
			}
		}
	}

	public static BlockPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "block");
			NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
			Block block = null;
			if (jsonObject.has("block")) {
				ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
				block = Registry.BLOCK.get(resourceLocation);
			}

			Tag<Block> tag = null;
			if (jsonObject.has("tag")) {
				ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
				tag = SerializationTags.getInstance()
					.getTagOrThrow(Registry.BLOCK_REGISTRY, resourceLocation2, resourceLocation -> new JsonSyntaxException("Unknown block tag '" + resourceLocation + "'"));
			}

			StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
			return new BlockPredicate(tag, block, statePropertiesPredicate, nbtPredicate);
		} else {
			return ANY;
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			if (this.block != null) {
				jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
			}

			if (this.tag != null) {
				jsonObject.addProperty(
					"tag", SerializationTags.getInstance().getIdOrThrow(Registry.BLOCK_REGISTRY, this.tag, () -> new IllegalStateException("Unknown block tag")).toString()
				);
			}

			jsonObject.add("nbt", this.nbt.serializeToJson());
			jsonObject.add("state", this.properties.serializeToJson());
			return jsonObject;
		}
	}

	public static class Builder {
		@Nullable
		private Block block;
		@Nullable
		private Tag<Block> blocks;
		private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;
		private NbtPredicate nbt = NbtPredicate.ANY;

		private Builder() {
		}

		public static BlockPredicate.Builder block() {
			return new BlockPredicate.Builder();
		}

		public BlockPredicate.Builder of(Block block) {
			this.block = block;
			return this;
		}

		public BlockPredicate.Builder of(Tag<Block> tag) {
			this.blocks = tag;
			return this;
		}

		public BlockPredicate.Builder setProperties(StatePropertiesPredicate statePropertiesPredicate) {
			this.properties = statePropertiesPredicate;
			return this;
		}

		public BlockPredicate build() {
			return new BlockPredicate(this.blocks, this.block, this.properties, this.nbt);
		}
	}
}
