/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;

public class OreConfiguration
implements FeatureConfiguration {
    public final Predicates target;
    public final int size;
    public final BlockState state;

    public OreConfiguration(Predicates predicates, BlockState blockState, int i) {
        this.size = i;
        this.state = blockState;
        this.target = predicates;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("size"), dynamicOps.createInt(this.size), dynamicOps.createString("target"), dynamicOps.createString(this.target.getName()), dynamicOps.createString("state"), BlockState.serialize(dynamicOps, this.state).getValue())));
    }

    public static OreConfiguration deserialize(Dynamic<?> dynamic) {
        int i = dynamic.get("size").asInt(0);
        Predicates predicates = Predicates.byName(dynamic.get("target").asString(""));
        BlockState blockState = dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new OreConfiguration(predicates, blockState, i);
    }

    public static enum Predicates {
        NATURAL_STONE("natural_stone", blockState -> {
            if (blockState != null) {
                Block block = blockState.getBlock();
                return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.DIORITE || block == Blocks.ANDESITE;
            }
            return false;
        }),
        NETHERRACK("netherrack", new BlockPredicate(Blocks.NETHERRACK));

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

        static {
            BY_NAME = Arrays.stream(Predicates.values()).collect(Collectors.toMap(Predicates::getName, predicates -> predicates));
        }
    }
}

