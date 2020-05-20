/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class OreConfiguration
implements FeatureConfiguration {
    public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Predicates.CODEC.fieldOf("target")).forGetter(oreConfiguration -> oreConfiguration.target), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(oreConfiguration -> oreConfiguration.state), ((MapCodec)Codec.INT.fieldOf("size")).withDefault(0).forGetter(oreConfiguration -> oreConfiguration.size)).apply((Applicative<OreConfiguration, ?>)instance, OreConfiguration::new));
    public final Predicates target;
    public final int size;
    public final BlockState state;

    public OreConfiguration(Predicates predicates, BlockState blockState, int i) {
        this.size = i;
        this.state = blockState;
        this.target = predicates;
    }

    public static enum Predicates implements StringRepresentable
    {
        NATURAL_STONE("natural_stone", blockState -> {
            if (blockState != null) {
                return blockState.is(Blocks.STONE) || blockState.is(Blocks.GRANITE) || blockState.is(Blocks.DIORITE) || blockState.is(Blocks.ANDESITE);
            }
            return false;
        }),
        NETHERRACK("netherrack", new BlockPredicate(Blocks.NETHERRACK)),
        NETHER_ORE_REPLACEABLES("nether_ore_replaceables", blockState -> {
            if (blockState != null) {
                return blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.BASALT) || blockState.is(Blocks.BLACKSTONE);
            }
            return false;
        });

        public static final Codec<Predicates> CODEC;
        private static final Map<String, Predicates> BY_NAME;
        private final String name;
        private final Predicate<BlockState> predicate;

        private Predicates(String string2, Predicate<BlockState> predicate) {
            this.name = string2;
            this.predicate = predicate;
        }

        public String getName() {
            return this.name;
        }

        public static Predicates byName(String string) {
            return BY_NAME.get(string);
        }

        public Predicate<BlockState> getPredicate() {
            return this.predicate;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Predicates::values, Predicates::byName);
            BY_NAME = Arrays.stream(Predicates.values()).collect(Collectors.toMap(Predicates::getName, predicates -> predicates));
        }
    }
}

