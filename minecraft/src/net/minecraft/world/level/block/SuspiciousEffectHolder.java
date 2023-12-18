package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface SuspiciousEffectHolder {
	List<SuspiciousEffectHolder.EffectEntry> getSuspiciousEffects();

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

	public static record EffectEntry(Holder<MobEffect> effect, int duration) {
		public static final Codec<SuspiciousEffectHolder.EffectEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("id").forGetter(SuspiciousEffectHolder.EffectEntry::effect),
						Codec.INT.optionalFieldOf("duration", Integer.valueOf(160)).forGetter(SuspiciousEffectHolder.EffectEntry::duration)
					)
					.apply(instance, SuspiciousEffectHolder.EffectEntry::new)
		);
		public static final Codec<List<SuspiciousEffectHolder.EffectEntry>> LIST_CODEC = CODEC.listOf();

		public MobEffectInstance createEffectInstance() {
			return new MobEffectInstance(this.effect, this.duration);
		}
	}
}
