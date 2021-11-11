package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

public abstract class PlacementModifier {
	public static final Codec<PlacementModifier> CODEC = Registry.PLACEMENT_MODIFIERS
		.byNameCodec()
		.dispatch(PlacementModifier::type, PlacementModifierType::codec);

	public abstract Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos);

	public abstract PlacementModifierType<?> type();
}
