package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record PotDecorations(Optional<Item> back, Optional<Item> left, Optional<Item> right, Optional<Item> front) {
	public static final PotDecorations EMPTY = new PotDecorations(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	public static final Codec<PotDecorations> CODEC = BuiltInRegistries.ITEM.byNameCodec().sizeLimitedListOf(4).xmap(PotDecorations::new, PotDecorations::ordered);
	public static final StreamCodec<RegistryFriendlyByteBuf, PotDecorations> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM)
		.apply(ByteBufCodecs.list(4))
		.map(PotDecorations::new, PotDecorations::ordered);

	private PotDecorations(List<Item> list) {
		this(getItem(list, 0), getItem(list, 1), getItem(list, 2), getItem(list, 3));
	}

	public PotDecorations(Item item, Item item2, Item item3, Item item4) {
		this(List.of(item, item2, item3, item4));
	}

	private static Optional<Item> getItem(List<Item> list, int i) {
		if (i >= list.size()) {
			return Optional.empty();
		} else {
			Item item = (Item)list.get(i);
			return item == Items.BRICK ? Optional.empty() : Optional.of(item);
		}
	}

	public CompoundTag save(CompoundTag compoundTag) {
		if (this.equals(EMPTY)) {
			return compoundTag;
		} else {
			compoundTag.put("sherds", CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow());
			return compoundTag;
		}
	}

	public List<Item> ordered() {
		return Stream.of(this.back, this.left, this.right, this.front).map(optional -> (Item)optional.orElse(Items.BRICK)).toList();
	}

	public static PotDecorations load(@Nullable CompoundTag compoundTag) {
		return compoundTag != null && compoundTag.contains("sherds")
			? (PotDecorations)CODEC.parse(NbtOps.INSTANCE, compoundTag.get("sherds")).result().orElse(EMPTY)
			: EMPTY;
	}
}
