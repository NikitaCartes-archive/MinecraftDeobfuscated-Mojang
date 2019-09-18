package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface FluidState extends StateHolder<FluidState> {
	Fluid getType();

	default boolean isSource() {
		return this.getType().isSource(this);
	}

	default boolean isEmpty() {
		return this.getType().isEmpty();
	}

	default float getHeight(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getType().getHeight(this, blockGetter, blockPos);
	}

	default float getOwnHeight() {
		return this.getType().getOwnHeight(this);
	}

	default int getAmount() {
		return this.getType().getAmount(this);
	}

	@Environment(EnvType.CLIENT)
	default boolean shouldRenderBackwardUpFace(BlockGetter blockGetter, BlockPos blockPos) {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				BlockPos blockPos2 = blockPos.offset(i, 0, j);
				FluidState fluidState = blockGetter.getFluidState(blockPos2);
				if (!fluidState.getType().isSame(this.getType()) && !blockGetter.getBlockState(blockPos2).isSolidRender(blockGetter, blockPos2)) {
					return true;
				}
			}
		}

		return false;
	}

	default void tick(Level level, BlockPos blockPos) {
		this.getType().tick(level, blockPos, this);
	}

	@Environment(EnvType.CLIENT)
	default void animateTick(Level level, BlockPos blockPos, Random random) {
		this.getType().animateTick(level, blockPos, this, random);
	}

	default boolean isRandomlyTicking() {
		return this.getType().isRandomlyTicking();
	}

	default void randomTick(Level level, BlockPos blockPos, Random random) {
		this.getType().randomTick(level, blockPos, this, random);
	}

	default Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getType().getFlow(blockGetter, blockPos, this);
	}

	default BlockState createLegacyBlock() {
		return this.getType().createLegacyBlock(this);
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	default ParticleOptions getDripParticle() {
		return this.getType().getDripParticle();
	}

	default boolean is(Tag<Fluid> tag) {
		return this.getType().is(tag);
	}

	default float getExplosionResistance() {
		return this.getType().getExplosionResistance();
	}

	default boolean canBeReplacedWith(BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
		return this.getType().canBeReplacedWith(this, blockGetter, blockPos, fluid, direction);
	}

	static <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps, FluidState fluidState) {
		ImmutableMap<Property<?>, Comparable<?>> immutableMap = fluidState.getValues();
		T object;
		if (immutableMap.isEmpty()) {
			object = dynamicOps.createMap(
				ImmutableMap.of(dynamicOps.createString("Name"), dynamicOps.createString(Registry.FLUID.getKey(fluidState.getType()).toString()))
			);
		} else {
			object = dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("Name"),
					dynamicOps.createString(Registry.FLUID.getKey(fluidState.getType()).toString()),
					dynamicOps.createString("Properties"),
					dynamicOps.createMap(
						(Map<T, T>)immutableMap.entrySet()
							.stream()
							.map(
								entry -> Pair.of(
										dynamicOps.createString(((Property)entry.getKey()).getName()),
										dynamicOps.createString(StateHolder.getName((Property<T>)entry.getKey(), (Comparable<?>)entry.getValue()))
									)
							)
							.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
					)
				)
			);
		}

		return new Dynamic<>(dynamicOps, object);
	}

	static <T> FluidState deserialize(Dynamic<T> dynamic) {
		Fluid fluid = Registry.FLUID
			.get(new ResourceLocation((String)dynamic.getElement("Name").flatMap(dynamic.getOps()::getStringValue).orElse("minecraft:empty")));
		Map<String, String> map = dynamic.get("Properties").asMap(dynamicx -> dynamicx.asString(""), dynamicx -> dynamicx.asString(""));
		FluidState fluidState = fluid.defaultFluidState();
		StateDefinition<Fluid, FluidState> stateDefinition = fluid.getStateDefinition();

		for (Entry<String, String> entry : map.entrySet()) {
			String string = (String)entry.getKey();
			Property<?> property = stateDefinition.getProperty(string);
			if (property != null) {
				fluidState = StateHolder.setValueHelper(fluidState, property, string, dynamic.toString(), (String)entry.getValue());
			}
		}

		return fluidState;
	}

	default VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getType().getShape(this, blockGetter, blockPos);
	}
}
