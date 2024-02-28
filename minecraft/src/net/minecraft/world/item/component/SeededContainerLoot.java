package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public record SeededContainerLoot(ResourceLocation lootTable, long seed) {
	public static final Codec<SeededContainerLoot> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("loot_table").forGetter(SeededContainerLoot::lootTable),
					ExtraCodecs.strictOptionalField(Codec.LONG, "seed", 0L).forGetter(SeededContainerLoot::seed)
				)
				.apply(instance, SeededContainerLoot::new)
	);
}
