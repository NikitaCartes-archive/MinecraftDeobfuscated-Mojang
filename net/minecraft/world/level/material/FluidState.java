/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface FluidState
extends StateHolder<FluidState> {
    public Fluid getType();

    default public boolean isSource() {
        return this.getType().isSource(this);
    }

    default public boolean isEmpty() {
        return this.getType().isEmpty();
    }

    default public float getHeight(BlockGetter blockGetter, BlockPos blockPos) {
        return this.getType().getHeight(this, blockGetter, blockPos);
    }

    default public float getOwnHeight() {
        return this.getType().getOwnHeight(this);
    }

    default public int getAmount() {
        return this.getType().getAmount(this);
    }

    @Environment(value=EnvType.CLIENT)
    default public boolean shouldRenderBackwardUpFace(BlockGetter blockGetter, BlockPos blockPos) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                BlockPos blockPos2 = blockPos.offset(i, 0, j);
                FluidState fluidState = blockGetter.getFluidState(blockPos2);
                if (fluidState.getType().isSame(this.getType()) || blockGetter.getBlockState(blockPos2).isSolidRender(blockGetter, blockPos2)) continue;
                return true;
            }
        }
        return false;
    }

    default public void tick(Level level, BlockPos blockPos) {
        this.getType().tick(level, blockPos, this);
    }

    @Environment(value=EnvType.CLIENT)
    default public void animateTick(Level level, BlockPos blockPos, Random random) {
        this.getType().animateTick(level, blockPos, this, random);
    }

    default public boolean isRandomlyTicking() {
        return this.getType().isRandomlyTicking();
    }

    default public void randomTick(Level level, BlockPos blockPos, Random random) {
        this.getType().randomTick(level, blockPos, this, random);
    }

    default public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos) {
        return this.getType().getFlow(blockGetter, blockPos, this);
    }

    default public BlockState createLegacyBlock() {
        return this.getType().createLegacyBlock(this);
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    default public ParticleOptions getDripParticle() {
        return this.getType().getDripParticle();
    }

    @Environment(value=EnvType.CLIENT)
    default public BlockLayer getRenderLayer() {
        return this.getType().getRenderLayer();
    }

    default public boolean is(Tag<Fluid> tag) {
        return this.getType().is(tag);
    }

    default public float getExplosionResistance() {
        return this.getType().getExplosionResistance();
    }

    default public boolean canBeReplacedWith(BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
        return this.getType().canBeReplacedWith(this, blockGetter, blockPos, fluid, direction);
    }

    public static <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps, FluidState fluidState) {
        ImmutableMap<Property<?>, Comparable<?>> immutableMap = fluidState.getValues();
        Object object = immutableMap.isEmpty() ? dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("Name"), dynamicOps.createString(Registry.FLUID.getKey(fluidState.getType()).toString()))) : dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("Name"), dynamicOps.createString(Registry.FLUID.getKey(fluidState.getType()).toString()), dynamicOps.createString("Properties"), dynamicOps.createMap(immutableMap.entrySet().stream().map(entry -> Pair.of(dynamicOps.createString(((Property)entry.getKey()).getName()), dynamicOps.createString(StateHolder.getName((Property)entry.getKey(), (Comparable)entry.getValue())))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))));
        return new Dynamic<T>(dynamicOps, object);
    }

    public static <T> FluidState deserialize(Dynamic<T> dynamic2) {
        Fluid fluid = Registry.FLUID.get(new ResourceLocation(dynamic2.getElement("Name").flatMap(dynamic2.getOps()::getStringValue).orElse("minecraft:empty")));
        Map<String, String> map = dynamic2.get("Properties").asMap(dynamic -> dynamic.asString(""), dynamic -> dynamic.asString(""));
        FluidState fluidState = fluid.defaultFluidState();
        StateDefinition<Fluid, FluidState> stateDefinition = fluid.getStateDefinition();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String string = entry.getKey();
            Property<?> property = stateDefinition.getProperty(string);
            if (property == null) continue;
            fluidState = StateHolder.setValueHelper(fluidState, property, string, dynamic2.toString(), entry.getValue());
        }
        return fluidState;
    }

    default public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
        return this.getType().getShape(this, blockGetter, blockPos);
    }
}

