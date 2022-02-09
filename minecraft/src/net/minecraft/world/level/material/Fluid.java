package net.minecraft.world.level.material;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Fluid {
	public static final IdMapper<FluidState> FLUID_STATE_REGISTRY = new IdMapper<>();
	protected final StateDefinition<Fluid, FluidState> stateDefinition;
	private FluidState defaultFluidState;
	private final Holder.Reference<Fluid> builtInRegistryHolder = Registry.FLUID.createIntrusiveHolder(this);

	protected Fluid() {
		StateDefinition.Builder<Fluid, FluidState> builder = new StateDefinition.Builder<>(this);
		this.createFluidStateDefinition(builder);
		this.stateDefinition = builder.create(Fluid::defaultFluidState, FluidState::new);
		this.registerDefaultState(this.stateDefinition.any());
	}

	protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
	}

	public StateDefinition<Fluid, FluidState> getStateDefinition() {
		return this.stateDefinition;
	}

	protected final void registerDefaultState(FluidState fluidState) {
		this.defaultFluidState = fluidState;
	}

	public final FluidState defaultFluidState() {
		return this.defaultFluidState;
	}

	public abstract Item getBucket();

	protected void animateTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
	}

	protected void tick(Level level, BlockPos blockPos, FluidState fluidState) {
	}

	protected void randomTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
	}

	@Nullable
	protected ParticleOptions getDripParticle() {
		return null;
	}

	protected abstract boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction);

	protected abstract Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState);

	public abstract int getTickDelay(LevelReader levelReader);

	protected boolean isRandomlyTicking() {
		return false;
	}

	protected boolean isEmpty() {
		return false;
	}

	protected abstract float getExplosionResistance();

	public abstract float getHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos);

	public abstract float getOwnHeight(FluidState fluidState);

	protected abstract BlockState createLegacyBlock(FluidState fluidState);

	public abstract boolean isSource(FluidState fluidState);

	public abstract int getAmount(FluidState fluidState);

	public boolean isSame(Fluid fluid) {
		return fluid == this;
	}

	@Deprecated
	public boolean is(TagKey<Fluid> tagKey) {
		return this.builtInRegistryHolder.is(tagKey);
	}

	public abstract VoxelShape getShape(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos);

	public Optional<SoundEvent> getPickupSound() {
		return Optional.empty();
	}

	@Deprecated
	public Holder.Reference<Fluid> builtInRegistryHolder() {
		return this.builtInRegistryHolder;
	}
}
