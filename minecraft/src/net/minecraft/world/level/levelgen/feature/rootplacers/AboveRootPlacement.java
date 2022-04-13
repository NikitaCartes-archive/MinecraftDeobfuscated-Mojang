package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record AboveRootPlacement(BlockStateProvider aboveRootProvider, float aboveRootPlacementChance) {
	public static final Codec<AboveRootPlacement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("above_root_provider").forGetter(aboveRootPlacement -> aboveRootPlacement.aboveRootProvider),
					Codec.floatRange(0.0F, 1.0F).fieldOf("above_root_placement_chance").forGetter(aboveRootPlacement -> aboveRootPlacement.aboveRootPlacementChance)
				)
				.apply(instance, AboveRootPlacement::new)
	);
}
