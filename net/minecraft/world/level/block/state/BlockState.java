/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockState
extends BlockBehaviour.BlockStateBase {
    public BlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap) {
        super(block, immutableMap);
    }

    @Override
    protected BlockState asState() {
        return this;
    }

    public static <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps, BlockState blockState) {
        ImmutableMap<Property<?>, Comparable<?>> immutableMap = blockState.getValues();
        Object object = immutableMap.isEmpty() ? dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("Name"), dynamicOps.createString(Registry.BLOCK.getKey(blockState.getBlock()).toString()))) : dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("Name"), dynamicOps.createString(Registry.BLOCK.getKey(blockState.getBlock()).toString()), dynamicOps.createString("Properties"), dynamicOps.createMap(immutableMap.entrySet().stream().map(entry -> Pair.of(dynamicOps.createString(((Property)entry.getKey()).getName()), dynamicOps.createString(StateHolder.getName((Property)entry.getKey(), (Comparable)entry.getValue())))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))));
        return new Dynamic<T>(dynamicOps, object);
    }

    public static <T> BlockState deserialize(Dynamic<T> dynamic2) {
        Block block = Registry.BLOCK.get(new ResourceLocation(dynamic2.getElement("Name").flatMap(dynamic2.getOps()::getStringValue).orElse("minecraft:air")));
        Map<String, String> map = dynamic2.get("Properties").asMap(dynamic -> dynamic.asString(""), dynamic -> dynamic.asString(""));
        BlockState blockState = block.defaultBlockState();
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String string = entry.getKey();
            Property<?> property = stateDefinition.getProperty(string);
            if (property == null) continue;
            blockState = StateHolder.setValueHelper(blockState, property, string, dynamic2.toString(), entry.getValue());
        }
        return blockState;
    }
}

