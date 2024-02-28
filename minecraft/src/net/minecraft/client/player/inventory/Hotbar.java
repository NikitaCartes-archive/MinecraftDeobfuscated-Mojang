package net.minecraft.client.player.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Hotbar {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int SIZE = Inventory.getSelectionSize();
	public static final Codec<Hotbar> CODEC = ExtraCodecs.<List<Dynamic<?>>>validate(Codec.PASSTHROUGH.listOf(), list -> Util.fixedSize(list, SIZE))
		.xmap(Hotbar::new, hotbar -> hotbar.items);
	private static final DynamicOps<Tag> DEFAULT_OPS = NbtOps.INSTANCE;
	private static final Dynamic<?> EMPTY_STACK = new Dynamic<>(
		DEFAULT_OPS, Util.getOrThrow(ItemStack.OPTIONAL_CODEC.encodeStart(DEFAULT_OPS, ItemStack.EMPTY), IllegalStateException::new)
	);
	private List<Dynamic<?>> items;

	private Hotbar(List<Dynamic<?>> list) {
		this.items = list;
	}

	public Hotbar() {
		this(Collections.nCopies(SIZE, EMPTY_STACK));
	}

	public List<ItemStack> load(HolderLookup.Provider provider) {
		return this.items
			.stream()
			.map(
				dynamic -> (ItemStack)ItemStack.OPTIONAL_CODEC
						.parse(RegistryOps.injectRegistryContext(dynamic, provider))
						.resultOrPartial(string -> LOGGER.warn("Could not parse hotbar item: {}", string))
						.orElse(ItemStack.EMPTY)
			)
			.toList();
	}

	public void storeFrom(Inventory inventory, RegistryAccess registryAccess) {
		RegistryOps<Tag> registryOps = registryAccess.createSerializationContext(DEFAULT_OPS);
		Builder<Dynamic<?>> builder = ImmutableList.builderWithExpectedSize(SIZE);

		for (int i = 0; i < SIZE; i++) {
			ItemStack itemStack = inventory.getItem(i);
			Optional<Dynamic<?>> optional = ItemStack.OPTIONAL_CODEC
				.encodeStart(registryOps, itemStack)
				.resultOrPartial(string -> LOGGER.warn("Could not encode hotbar item: {}", string))
				.map(tag -> new Dynamic<>(DEFAULT_OPS, tag));
			builder.add((Dynamic<?>)optional.orElse(EMPTY_STACK));
		}

		this.items = builder.build();
	}

	public boolean isEmpty() {
		for (Dynamic<?> dynamic : this.items) {
			if (!isEmpty(dynamic)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isEmpty(Dynamic<?> dynamic) {
		return EMPTY_STACK.equals(dynamic);
	}
}
