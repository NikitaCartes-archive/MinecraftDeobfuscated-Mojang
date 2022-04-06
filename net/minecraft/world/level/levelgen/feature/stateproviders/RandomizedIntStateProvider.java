/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import org.jetbrains.annotations.Nullable;

public class RandomizedIntStateProvider
extends BlockStateProvider {
    public static final Codec<RandomizedIntStateProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("source")).forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.source), ((MapCodec)Codec.STRING.fieldOf("property")).forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.propertyName), ((MapCodec)IntProvider.CODEC.fieldOf("values")).forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.values)).apply((Applicative<RandomizedIntStateProvider, ?>)instance, RandomizedIntStateProvider::new));
    private final BlockStateProvider source;
    private final String propertyName;
    @Nullable
    private IntegerProperty property;
    private final IntProvider values;

    public RandomizedIntStateProvider(BlockStateProvider blockStateProvider, IntegerProperty integerProperty, IntProvider intProvider) {
        this.source = blockStateProvider;
        this.property = integerProperty;
        this.propertyName = integerProperty.getName();
        this.values = intProvider;
        Collection<Integer> collection = integerProperty.getPossibleValues();
        for (int i = intProvider.getMinValue(); i <= intProvider.getMaxValue(); ++i) {
            if (collection.contains(i)) continue;
            throw new IllegalArgumentException("Property value out of range: " + integerProperty.getName() + ": " + i);
        }
    }

    public RandomizedIntStateProvider(BlockStateProvider blockStateProvider, String string, IntProvider intProvider) {
        this.source = blockStateProvider;
        this.propertyName = string;
        this.values = intProvider;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.RANDOMIZED_INT_STATE_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
        BlockState blockState = this.source.getState(randomSource, blockPos);
        if (this.property == null || !blockState.hasProperty(this.property)) {
            this.property = RandomizedIntStateProvider.findProperty(blockState, this.propertyName);
        }
        return (BlockState)blockState.setValue(this.property, this.values.sample(randomSource));
    }

    private static IntegerProperty findProperty(BlockState blockState, String string) {
        Collection<Property<?>> collection = blockState.getProperties();
        Optional<IntegerProperty> optional = collection.stream().filter(property -> property.getName().equals(string)).filter(property -> property instanceof IntegerProperty).map(property -> (IntegerProperty)property).findAny();
        return optional.orElseThrow(() -> new IllegalArgumentException("Illegal property: " + string));
    }
}

