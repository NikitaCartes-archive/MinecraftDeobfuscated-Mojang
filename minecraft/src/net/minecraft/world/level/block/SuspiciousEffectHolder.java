package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;

public interface SuspiciousEffectHolder {
	SuspiciousStewEffects getSuspiciousEffects();

	static List<SuspiciousEffectHolder> getAllEffectHolders() {
		return (List<SuspiciousEffectHolder>)BuiltInRegistries.ITEM
			.stream()
			.map(SuspiciousEffectHolder::tryGet)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Nullable
	static SuspiciousEffectHolder tryGet(ItemLike itemLike) {
		if (itemLike.asItem() instanceof BlockItem blockItem) {
			Block var6 = blockItem.getBlock();
			if (var6 instanceof SuspiciousEffectHolder) {
				return (SuspiciousEffectHolder)var6;
			}
		}

		Item suspiciousEffectHolder = itemLike.asItem();
		return suspiciousEffectHolder instanceof SuspiciousEffectHolder ? (SuspiciousEffectHolder)suspiciousEffectHolder : null;
	}
}
