package net.minecraft.world.item.component;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public final class CustomData {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final CustomData EMPTY = new CustomData(new CompoundTag());
	public static final Codec<CustomData> CODEC = CompoundTag.CODEC.xmap(CustomData::new, customData -> customData.tag);
	public static final Codec<CustomData> CODEC_WITH_ID = CODEC.validate(
		customData -> customData.getUnsafe().contains("id", 8) ? DataResult.success(customData) : DataResult.error(() -> "Missing id for entity in: " + customData)
	);
	@Deprecated
	public static final StreamCodec<ByteBuf, CustomData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CustomData::new, customData -> customData.tag);
	private final CompoundTag tag;

	private CustomData(CompoundTag compoundTag) {
		this.tag = compoundTag;
	}

	public static CustomData of(CompoundTag compoundTag) {
		return new CustomData(compoundTag.copy());
	}

	public static Predicate<ItemStack> itemMatcher(DataComponentType<CustomData> dataComponentType, CompoundTag compoundTag) {
		return itemStack -> {
			CustomData customData = itemStack.getOrDefault(dataComponentType, EMPTY);
			return customData.matchedBy(compoundTag);
		};
	}

	public boolean matchedBy(CompoundTag compoundTag) {
		return NbtUtils.compareNbt(compoundTag, this.tag, true);
	}

	public static void update(DataComponentType<CustomData> dataComponentType, ItemStack itemStack, Consumer<CompoundTag> consumer) {
		CustomData customData = itemStack.getOrDefault(dataComponentType, EMPTY).update(consumer);
		if (customData.tag.isEmpty()) {
			itemStack.remove(dataComponentType);
		} else {
			itemStack.set(dataComponentType, customData);
		}
	}

	public static void set(DataComponentType<CustomData> dataComponentType, ItemStack itemStack, CompoundTag compoundTag) {
		if (!compoundTag.isEmpty()) {
			itemStack.set(dataComponentType, of(compoundTag));
		} else {
			itemStack.remove(dataComponentType);
		}
	}

	public CustomData update(Consumer<CompoundTag> consumer) {
		CompoundTag compoundTag = this.tag.copy();
		consumer.accept(compoundTag);
		return new CustomData(compoundTag);
	}

	public void loadInto(Entity entity) {
		CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
		UUID uUID = entity.getUUID();
		compoundTag.merge(this.tag);
		entity.load(compoundTag);
		entity.setUUID(uUID);
	}

	public boolean loadInto(BlockEntity blockEntity, HolderLookup.Provider provider) {
		CompoundTag compoundTag = blockEntity.saveCustomOnly(provider);
		CompoundTag compoundTag2 = compoundTag.copy();
		compoundTag.merge(this.tag);
		if (!compoundTag.equals(compoundTag2)) {
			try {
				blockEntity.loadCustomOnly(compoundTag, provider);
				blockEntity.setChanged();
				return true;
			} catch (Exception var8) {
				LOGGER.warn("Failed to apply custom data to block entity at {}", blockEntity.getBlockPos(), var8);

				try {
					blockEntity.loadCustomOnly(compoundTag2, provider);
				} catch (Exception var7) {
					LOGGER.warn("Failed to rollback block entity at {} after failure", blockEntity.getBlockPos(), var7);
				}
			}
		}

		return false;
	}

	public <T> DataResult<CustomData> update(DynamicOps<Tag> dynamicOps, MapEncoder<T> mapEncoder, T object) {
		return mapEncoder.encode(object, dynamicOps, dynamicOps.mapBuilder()).build(this.tag).map(tag -> new CustomData((CompoundTag)tag));
	}

	public <T> DataResult<T> read(MapDecoder<T> mapDecoder) {
		return this.read(NbtOps.INSTANCE, mapDecoder);
	}

	public <T> DataResult<T> read(DynamicOps<Tag> dynamicOps, MapDecoder<T> mapDecoder) {
		MapLike<Tag> mapLike = dynamicOps.getMap(this.tag).getOrThrow();
		return mapDecoder.decode(dynamicOps, mapLike);
	}

	public int size() {
		return this.tag.size();
	}

	public boolean isEmpty() {
		return this.tag.isEmpty();
	}

	public CompoundTag copyTag() {
		return this.tag.copy();
	}

	public boolean contains(String string) {
		return this.tag.contains(string);
	}

	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else {
			return object instanceof CustomData customData ? this.tag.equals(customData.tag) : false;
		}
	}

	public int hashCode() {
		return this.tag.hashCode();
	}

	public String toString() {
		return this.tag.toString();
	}

	@Deprecated
	public CompoundTag getUnsafe() {
		return this.tag;
	}
}
