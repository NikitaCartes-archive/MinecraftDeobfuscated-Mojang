/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class TreeConfiguration
implements FeatureConfiguration {
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider leavesProvider;
    public final List<TreeDecorator> decorators;
    public final int baseHeight;
    public transient boolean fromSapling;

    protected TreeConfiguration(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, List<TreeDecorator> list, int i) {
        this.trunkProvider = blockStateProvider;
        this.leavesProvider = blockStateProvider2;
        this.decorators = list;
        this.baseHeight = i;
    }

    public void setFromSapling() {
        this.fromSapling = true;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("trunk_provider"), this.trunkProvider.serialize(dynamicOps)).put(dynamicOps.createString("leaves_provider"), this.leavesProvider.serialize(dynamicOps)).put(dynamicOps.createString("decorators"), dynamicOps.createList(this.decorators.stream().map(treeDecorator -> treeDecorator.serialize(dynamicOps)))).put(dynamicOps.createString("base_height"), dynamicOps.createInt(this.baseHeight));
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(builder.build()));
    }

    public static <T> TreeConfiguration deserialize(Dynamic<T> dynamic2) {
        BlockStateProviderType<T> blockStateProviderType = Registry.BLOCKSTATE_PROVIDER_TYPES.get(new ResourceLocation(dynamic2.get("trunk_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        BlockStateProviderType<T> blockStateProviderType2 = Registry.BLOCKSTATE_PROVIDER_TYPES.get(new ResourceLocation(dynamic2.get("leaves_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        return new TreeConfiguration((BlockStateProvider)blockStateProviderType.deserialize(dynamic2.get("trunk_provider").orElseEmptyMap()), (BlockStateProvider)blockStateProviderType2.deserialize(dynamic2.get("leaves_provider").orElseEmptyMap()), dynamic2.get("decorators").asList(dynamic -> Registry.TREE_DECORATOR_TYPES.get(new ResourceLocation(dynamic.get("type").asString().orElseThrow(RuntimeException::new))).deserialize((Dynamic<?>)dynamic)), dynamic2.get("base_height").asInt(0));
    }

    public static class TreeConfigurationBuilder {
        public final BlockStateProvider trunkProvider;
        public final BlockStateProvider leavesProvider;
        private List<TreeDecorator> decorators = Lists.newArrayList();
        private int baseHeight = 0;

        public TreeConfigurationBuilder(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2) {
            this.trunkProvider = blockStateProvider;
            this.leavesProvider = blockStateProvider2;
        }

        public TreeConfigurationBuilder baseHeight(int i) {
            this.baseHeight = i;
            return this;
        }

        public TreeConfiguration build() {
            return new TreeConfiguration(this.trunkProvider, this.leavesProvider, this.decorators, this.baseHeight);
        }
    }
}

