package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.ItemContainerContents;

public interface ContainerComponentManipulators {
	ContainerComponentManipulator<ItemContainerContents> CONTAINER = new ContainerComponentManipulator<ItemContainerContents>() {
		@Override
		public DataComponentType<ItemContainerContents> type() {
			return DataComponents.CONTAINER;
		}

		public Stream<ItemStack> getContents(ItemContainerContents itemContainerContents) {
			return itemContainerContents.stream();
		}

		public ItemContainerContents empty() {
			return ItemContainerContents.EMPTY;
		}

		public ItemContainerContents setContents(ItemContainerContents itemContainerContents, Stream<ItemStack> stream) {
			return ItemContainerContents.fromItems(stream.toList());
		}
	};
	ContainerComponentManipulator<BundleContents> BUNDLE_CONTENTS = new ContainerComponentManipulator<BundleContents>() {
		@Override
		public DataComponentType<BundleContents> type() {
			return DataComponents.BUNDLE_CONTENTS;
		}

		public BundleContents empty() {
			return BundleContents.EMPTY;
		}

		public Stream<ItemStack> getContents(BundleContents bundleContents) {
			return bundleContents.itemCopyStream();
		}

		public BundleContents setContents(BundleContents bundleContents, Stream<ItemStack> stream) {
			BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents).clearItems();
			stream.forEach(mutable::tryInsert);
			return mutable.toImmutable();
		}
	};
	ContainerComponentManipulator<ChargedProjectiles> CHARGED_PROJECTILES = new ContainerComponentManipulator<ChargedProjectiles>() {
		@Override
		public DataComponentType<ChargedProjectiles> type() {
			return DataComponents.CHARGED_PROJECTILES;
		}

		public ChargedProjectiles empty() {
			return ChargedProjectiles.EMPTY;
		}

		public Stream<ItemStack> getContents(ChargedProjectiles chargedProjectiles) {
			return chargedProjectiles.getItems().stream();
		}

		public ChargedProjectiles setContents(ChargedProjectiles chargedProjectiles, Stream<ItemStack> stream) {
			return ChargedProjectiles.of(stream.toList());
		}
	};
	Map<DataComponentType<?>, ContainerComponentManipulator<?>> ALL_MANIPULATORS = (Map<DataComponentType<?>, ContainerComponentManipulator<?>>)Stream.of(
			CONTAINER, BUNDLE_CONTENTS, CHARGED_PROJECTILES
		)
		.collect(Collectors.toMap(ContainerComponentManipulator::type, containerComponentManipulator -> containerComponentManipulator));
	Codec<ContainerComponentManipulator<?>> CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().comapFlatMap(dataComponentType -> {
		ContainerComponentManipulator<?> containerComponentManipulator = (ContainerComponentManipulator<?>)ALL_MANIPULATORS.get(dataComponentType);
		return containerComponentManipulator != null ? DataResult.success(containerComponentManipulator) : DataResult.error(() -> "No items in component");
	}, ContainerComponentManipulator::type);
}
