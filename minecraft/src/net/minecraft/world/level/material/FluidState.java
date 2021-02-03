package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FluidState extends StateHolder<Fluid, FluidState> {
	public static final Codec<FluidState> CODEC = codec(Registry.FLUID, Fluid::defaultFluidState).stable();

	public FluidState(Fluid fluid, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<FluidState> mapCodec) {
		super(fluid, immutableMap, mapCodec);
	}

	public Fluid getType() {
		return this.owner;
	}

	public boolean isSource() {
		return this.getType().isSource(this);
	}

	public boolean isSourceOfType(Fluid fluid) {
		return this.owner == fluid && this.owner.isSource(this);
	}

	public boolean isEmpty() {
		return this.getType().isEmpty();
	}

	public float getHeight(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getType().getHeight(this, blockGetter, blockPos);
	}

	public float getOwnHeight() {
		return this.getType().getOwnHeight(this);
	}

	public int getAmount() {
		return this.getType().getAmount(this);
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldRenderBackwardUpFace(BlockGetter blockGetter, BlockPos blockPos) {
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

	public void tick(Level level, BlockPos blockPos) {
		this.getType().tick(level, blockPos, this);
	}

	@Environment(EnvType.CLIENT)
	public void animateTick(Level level, BlockPos blockPos, Random random) {
		this.getType().animateTick(level, blockPos, this, random);
	}

	public boolean isRandomlyTicking() {
		return this.getType().isRandomlyTicking();
	}

	public void randomTick(Level level, BlockPos blockPos, Random random) {
		this.getType().randomTick(level, blockPos, this, random);
	}

	public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getType().getFlow(blockGetter, blockPos, this);
	}

	public BlockState createLegacyBlock() {
		return this.getType().createLegacyBlock(this);
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public ParticleOptions getDripParticle() {
		return this.getType().getDripParticle();
	}

	public boolean is(Tag<Fluid> tag) {
		return this.getType().is(tag);
	}

	public float getExplosionResistance() {
		return this.getType().getExplosionResistance();
	}

	public boolean canBeReplacedWith(BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
		return this.getType().canBeReplacedWith(this, blockGetter, blockPos, fluid, direction);
	}

	public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getType().getShape(this, blockGetter, blockPos);
	}
}
