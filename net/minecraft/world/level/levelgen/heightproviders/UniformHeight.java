/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import org.slf4j.Logger;

public class UniformHeight
extends HeightProvider {
    public static final Codec<UniformHeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)VerticalAnchor.CODEC.fieldOf("min_inclusive")).forGetter(uniformHeight -> uniformHeight.minInclusive), ((MapCodec)VerticalAnchor.CODEC.fieldOf("max_inclusive")).forGetter(uniformHeight -> uniformHeight.maxInclusive)).apply((Applicative<UniformHeight, ?>)instance, UniformHeight::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final LongSet warnedFor = new LongOpenHashSet();

    private UniformHeight(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
        this.minInclusive = verticalAnchor;
        this.maxInclusive = verticalAnchor2;
    }

    public static UniformHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
        return new UniformHeight(verticalAnchor, verticalAnchor2);
    }

    @Override
    public int sample(Random random, WorldGenerationContext worldGenerationContext) {
        int j;
        int i = this.minInclusive.resolveY(worldGenerationContext);
        if (i > (j = this.maxInclusive.resolveY(worldGenerationContext))) {
            if (this.warnedFor.add((long)i << 32 | (long)j)) {
                LOGGER.warn("Empty height range: {}", (Object)this);
            }
            return i;
        }
        return Mth.randomBetweenInclusive(random, i, j);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

