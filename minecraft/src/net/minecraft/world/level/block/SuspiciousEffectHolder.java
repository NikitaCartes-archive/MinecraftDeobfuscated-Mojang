package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;

public interface SuspiciousEffectHolder {
	RandomSource random = RandomSource.create();
	List<Holder<MobEffect>> POTATO_EFFECTS = List.of(
		MobEffects.DIG_SLOWDOWN,
		MobEffects.DIG_SPEED,
		MobEffects.POTATO_OIL,
		MobEffects.LUCK,
		MobEffects.UNLUCK,
		MobEffects.SLOW_FALLING,
		MobEffects.HERO_OF_THE_VILLAGE,
		MobEffects.GLOWING
	);

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
		if (suspiciousEffectHolder instanceof SuspiciousEffectHolder) {
			return (SuspiciousEffectHolder)suspiciousEffectHolder;
		} else {
			return itemLike.asItem() == Items.POISONOUS_POTATO
				? () -> new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(Util.getRandom(POTATO_EFFECTS, random), random.nextInt(60))))
				: null;
		}
	}
}
