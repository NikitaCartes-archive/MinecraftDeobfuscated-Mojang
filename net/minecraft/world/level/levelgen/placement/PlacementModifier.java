/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public abstract class PlacementModifier {
    public static final Codec<PlacementModifier> CODEC = Registry.PLACEMENT_MODIFIERS.byNameCodec().dispatch(PlacementModifier::type, PlacementModifierType::codec);

    public abstract Stream<BlockPos> getPositions(PlacementContext var1, Random var2, BlockPos var3);

    public abstract PlacementModifierType<?> type();
}

