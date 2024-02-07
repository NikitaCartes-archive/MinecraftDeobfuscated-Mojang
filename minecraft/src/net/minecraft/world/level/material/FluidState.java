package net.minecraft.world.level.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FluidState extends StateHolder<Fluid, FluidState> {
	public static final Codec<FluidState> CODEC = codec(BuiltInRegistries.FLUID.byNameCodec(), Fluid::defaultFluidState).stable();
	public static final int AMOUNT_MAX = 9;
	public static final int AMOUNT_FULL = 8;

	public FluidState(Fluid fluid, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<FluidState> mapCodec) {
		super(fluid, reference2ObjectArrayMap, mapCodec);
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

	public void animateTick(Level level, BlockPos blockPos, RandomSource randomSource) {
		this.getType().animateTick(level, blockPos, this, randomSource);
	}

	public boolean isRandomlyTicking() {
		return this.getType().isRandomlyTicking();
	}

	public void randomTick(Level level, BlockPos blockPos, RandomSource randomSource) {
		this.getType().randomTick(level, blockPos, this, randomSource);
	}

	public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos) {
		return this.getType().getFlow(blockGetter, blockPos, this);
	}

	public BlockState createLegacyBlock() {
		return this.getType().createLegacyBlock(this);
	}

	@Nullable
	public ParticleOptions getDripParticle() {
		return this.getType().getDripParticle();
	}

	public boolean is(TagKey<Fluid> tagKey) {
		return this.getType().builtInRegistryHolder().is(tagKey);
	}

	public boolean is(HolderSet<Fluid> holderSet) {
		return holderSet.contains(this.getType().builtInRegistryHolder());
	}

	public boolean is(Fluid fluid) {
		return this.getType() == fluid;
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

	public Holder<Fluid> holder() {
		return this.owner.builtInRegistryHolder();
	}

	public Stream<TagKey<Fluid>> getTags() {
		return this.owner.builtInRegistryHolder().tags();
	}
}
