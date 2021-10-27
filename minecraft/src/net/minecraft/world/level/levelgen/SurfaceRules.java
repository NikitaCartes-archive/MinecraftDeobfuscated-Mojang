package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRules {
	public static final SurfaceRules.ConditionSource ON_FLOOR = new SurfaceRules.StoneDepthCheck(false, CaveSurface.FLOOR);
	public static final SurfaceRules.ConditionSource UNDER_FLOOR = new SurfaceRules.StoneDepthCheck(true, CaveSurface.FLOOR);
	public static final SurfaceRules.ConditionSource ON_CEILING = new SurfaceRules.StoneDepthCheck(false, CaveSurface.CEILING);
	public static final SurfaceRules.ConditionSource UNDER_CEILING = new SurfaceRules.StoneDepthCheck(true, CaveSurface.CEILING);

	public static SurfaceRules.ConditionSource not(SurfaceRules.ConditionSource conditionSource) {
		return new SurfaceRules.NotConditionSource(conditionSource);
	}

	public static SurfaceRules.ConditionSource yBlockCheck(VerticalAnchor verticalAnchor, int i) {
		return new SurfaceRules.YConditionSource(verticalAnchor, i, false);
	}

	public static SurfaceRules.ConditionSource yStartCheck(VerticalAnchor verticalAnchor, int i) {
		return new SurfaceRules.YConditionSource(verticalAnchor, i, true);
	}

	public static SurfaceRules.ConditionSource waterBlockCheck(int i, int j) {
		return new SurfaceRules.WaterConditionSource(i, j, false);
	}

	public static SurfaceRules.ConditionSource waterStartCheck(int i, int j) {
		return new SurfaceRules.WaterConditionSource(i, j, true);
	}

	@SafeVarargs
	public static SurfaceRules.ConditionSource isBiome(ResourceKey<Biome>... resourceKeys) {
		return isBiome(List.of(resourceKeys));
	}

	private static SurfaceRules.BiomeConditionSource isBiome(List<ResourceKey<Biome>> list) {
		return new SurfaceRules.BiomeConditionSource(list);
	}

	public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d) {
		return noiseCondition(resourceKey, d, Double.POSITIVE_INFINITY);
	}

	public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d, double e) {
		return new SurfaceRules.NoiseThresholdConditionSource(resourceKey, d, e);
	}

	public static SurfaceRules.ConditionSource steep() {
		return SurfaceRules.Steep.INSTANCE;
	}

	public static SurfaceRules.ConditionSource hole() {
		return SurfaceRules.Hole.INSTANCE;
	}

	public static SurfaceRules.ConditionSource temperature() {
		return SurfaceRules.Temperature.INSTANCE;
	}

	public static SurfaceRules.RuleSource ifTrue(SurfaceRules.ConditionSource conditionSource, SurfaceRules.RuleSource ruleSource) {
		return new SurfaceRules.TestRuleSource(conditionSource, ruleSource);
	}

	public static SurfaceRules.RuleSource sequence(SurfaceRules.RuleSource ruleSource, SurfaceRules.RuleSource... ruleSources) {
		return new SurfaceRules.SequenceRuleSource(Stream.concat(Stream.of(ruleSource), Arrays.stream(ruleSources)).toList());
	}

	public static SurfaceRules.RuleSource state(BlockState blockState) {
		return new SurfaceRules.BlockRuleSource(blockState);
	}

	public static SurfaceRules.RuleSource bandlands() {
		return SurfaceRules.Bandlands.INSTANCE;
	}

	static enum Bandlands implements SurfaceRules.RuleSource {
		INSTANCE;

		static final Codec<SurfaceRules.Bandlands> CODEC = Codec.unit(INSTANCE);

		@Override
		public Codec<? extends SurfaceRules.RuleSource> codec() {
			return CODEC;
		}

		public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
			return context.system::getBand;
		}
	}

	static record BiomeConditionSource() implements SurfaceRules.ConditionSource {
		private final List<ResourceKey<Biome>> biomes;
		static final Codec<SurfaceRules.BiomeConditionSource> CODEC = ResourceKey.codec(Registry.BIOME_REGISTRY)
			.listOf()
			.fieldOf("biome_is")
			.<SurfaceRules.BiomeConditionSource>xmap(SurfaceRules::isBiome, SurfaceRules.BiomeConditionSource::biomes)
			.codec();

		BiomeConditionSource(List<ResourceKey<Biome>> list) {
			this.biomes = list;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			final Set<ResourceKey<Biome>> set = Set.copyOf(this.biomes);

			class BiomeCondition extends SurfaceRules.EagerCondition<ResourceKey<Biome>> {
				protected boolean compute(ResourceKey<Biome> resourceKey) {
					return set.contains(resourceKey);
				}
			}

			BiomeCondition lv = new BiomeCondition();
			context.biomeConditions.add(lv);
			return lv;
		}
	}

	static record BlockRuleSource() implements SurfaceRules.RuleSource {
		private final BlockState resultState;
		private final SurfaceRules.StateRule rule;
		static final Codec<SurfaceRules.BlockRuleSource> CODEC = BlockState.CODEC
			.<SurfaceRules.BlockRuleSource>xmap(SurfaceRules.BlockRuleSource::new, SurfaceRules.BlockRuleSource::resultState)
			.fieldOf("result_state")
			.codec();

		BlockRuleSource(BlockState blockState) {
			this(blockState, new SurfaceRules.StateRule(blockState));
		}

		private BlockRuleSource(BlockState blockState, SurfaceRules.StateRule stateRule) {
			this.resultState = blockState;
			this.rule = stateRule;
		}

		@Override
		public Codec<? extends SurfaceRules.RuleSource> codec() {
			return CODEC;
		}

		public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
			return this.rule;
		}
	}

	interface Condition {
		boolean test();
	}

	public interface ConditionSource extends Function<SurfaceRules.Context, SurfaceRules.Condition> {
		Codec<SurfaceRules.ConditionSource> CODEC = Registry.CONDITION.dispatch(SurfaceRules.ConditionSource::codec, Function.identity());

		static Codec<? extends SurfaceRules.ConditionSource> bootstrap() {
			Registry.register(Registry.CONDITION, "biome", SurfaceRules.BiomeConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "noise_threshold", SurfaceRules.NoiseThresholdConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "y_above", SurfaceRules.YConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "water", SurfaceRules.WaterConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "temperature", SurfaceRules.Temperature.CODEC);
			Registry.register(Registry.CONDITION, "steep", SurfaceRules.Steep.CODEC);
			Registry.register(Registry.CONDITION, "not", SurfaceRules.NotConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "hole", SurfaceRules.Hole.CODEC);
			Registry.register(Registry.CONDITION, "stone_depth", SurfaceRules.StoneDepthCheck.CODEC);
			return (Codec<? extends SurfaceRules.ConditionSource>)Registry.CONDITION.iterator().next();
		}

		Codec<? extends SurfaceRules.ConditionSource> codec();
	}

	protected static final class Context {
		final SurfaceSystem system;
		final SurfaceRules.UpdatableCondition<SurfaceRules.Context.TemperatureHelperCondition.State> temperature = new SurfaceRules.Context.TemperatureHelperCondition();
		final SurfaceRules.UpdatableCondition<SurfaceRules.Context.SteepMaterialCondition.State> steep = new SurfaceRules.Context.SteepMaterialCondition();
		final SurfaceRules.UpdatableCondition<Integer> hole = new SurfaceRules.Context.HoleCondition();
		final List<SurfaceRules.UpdatableCondition<ResourceKey<Biome>>> biomeConditions = new ObjectArrayList<>();
		final List<SurfaceRules.UpdatableCondition<SurfaceRules.NoiseThresholdConditionState>> noiseThresholdConditions = new ObjectArrayList<>();
		final List<SurfaceRules.UpdatableCondition<SurfaceRules.YConditionState>> yConditions = new ObjectArrayList<>();
		final WorldGenerationContext context;

		protected Context(SurfaceSystem surfaceSystem, WorldGenerationContext worldGenerationContext) {
			this.system = surfaceSystem;
			this.context = worldGenerationContext;
		}

		protected void updateXZ(ChunkAccess chunkAccess, int i, int j, int k) {
			SurfaceRules.NoiseThresholdConditionState noiseThresholdConditionState = new SurfaceRules.NoiseThresholdConditionState(i, j);

			for (SurfaceRules.UpdatableCondition<SurfaceRules.NoiseThresholdConditionState> updatableCondition : this.noiseThresholdConditions) {
				updatableCondition.update(noiseThresholdConditionState);
			}

			this.steep.update(new SurfaceRules.Context.SteepMaterialCondition.State(chunkAccess, i, j));
			this.hole.update(k);
		}

		protected void updateY(ResourceKey<Biome> resourceKey, Biome biome, int i, int j, int k, int l, int m, int n, int o) {
			for (SurfaceRules.UpdatableCondition<ResourceKey<Biome>> updatableCondition : this.biomeConditions) {
				updatableCondition.update(resourceKey);
			}

			SurfaceRules.YConditionState yConditionState = new SurfaceRules.YConditionState(n, j, k, i, l);

			for (SurfaceRules.UpdatableCondition<SurfaceRules.YConditionState> updatableCondition2 : this.yConditions) {
				updatableCondition2.update(yConditionState);
			}

			this.temperature.update(new SurfaceRules.Context.TemperatureHelperCondition.State(biome, m, n, o));
		}

		static final class HoleCondition extends SurfaceRules.EagerCondition<Integer> {
			protected boolean compute(Integer integer) {
				return integer <= 0;
			}
		}

		static class SteepMaterialCondition extends SurfaceRules.LazyCondition<SurfaceRules.Context.SteepMaterialCondition.State> {
			protected boolean compute(SurfaceRules.Context.SteepMaterialCondition.State state) {
				int i = state.blockX & 15;
				int j = state.blockZ & 15;
				int k = Math.max(j - 1, 0);
				int l = Math.min(j + 1, 15);
				int m = state.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, k);
				int n = state.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, l);
				if (n >= m + 4) {
					return true;
				} else {
					int o = Math.max(i - 1, 0);
					int p = Math.min(i + 1, 15);
					int q = state.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, o, j);
					int r = state.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p, j);
					return q >= r + 4;
				}
			}

			static record State() {
				final ChunkAccess chunk;
				final int blockX;
				final int blockZ;

				State(ChunkAccess chunkAccess, int i, int j) {
					this.chunk = chunkAccess;
					this.blockX = i;
					this.blockZ = j;
				}
			}
		}

		static class TemperatureHelperCondition extends SurfaceRules.LazyCondition<SurfaceRules.Context.TemperatureHelperCondition.State> {
			protected boolean compute(SurfaceRules.Context.TemperatureHelperCondition.State state) {
				return state.biome.getTemperature(new BlockPos(state.blockX, state.blockY, state.blockZ)) < 0.15F;
			}

			static record State() {
				final Biome biome;
				final int blockX;
				final int blockY;
				final int blockZ;

				State(Biome biome, int i, int j, int k) {
					this.biome = biome;
					this.blockX = i;
					this.blockY = j;
					this.blockZ = k;
				}
			}
		}
	}

	abstract static class EagerCondition<S> implements SurfaceRules.UpdatableCondition<S> {
		boolean state = false;

		@Override
		public void update(S object) {
			this.state = this.compute(object);
		}

		@Override
		public boolean test() {
			return this.state;
		}

		protected abstract boolean compute(S object);
	}

	static enum Hole implements SurfaceRules.ConditionSource {
		INSTANCE;

		static final Codec<SurfaceRules.Hole> CODEC = Codec.unit(INSTANCE);

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			return context.hole;
		}
	}

	abstract static class LazyCondition<S> implements SurfaceRules.UpdatableCondition<S> {
		@Nullable
		private S state;
		@Nullable
		Boolean result;

		@Override
		public void update(S object) {
			this.state = object;
			this.result = null;
		}

		@Override
		public boolean test() {
			if (this.result == null) {
				if (this.state == null) {
					throw new IllegalStateException("Calling test without update");
				}

				this.result = this.compute(this.state);
			}

			return this.result;
		}

		protected abstract boolean compute(S object);
	}

	static record NoiseThresholdConditionSource() implements SurfaceRules.ConditionSource {
		private final ResourceKey<NormalNoise.NoiseParameters> noise;
		final double minThreshold;
		final double maxThreshold;
		static final Codec<SurfaceRules.NoiseThresholdConditionSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ResourceKey.codec(Registry.NOISE_REGISTRY).fieldOf("noise").forGetter(SurfaceRules.NoiseThresholdConditionSource::noise),
						Codec.DOUBLE.fieldOf("min_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::minThreshold),
						Codec.DOUBLE.fieldOf("max_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::maxThreshold)
					)
					.apply(instance, SurfaceRules.NoiseThresholdConditionSource::new)
		);

		NoiseThresholdConditionSource(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d, double e) {
			this.noise = resourceKey;
			this.minThreshold = d;
			this.maxThreshold = e;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			final NormalNoise normalNoise = context.system.getOrCreateNoise(this.noise);

			class NoiseThresholdCondition extends SurfaceRules.LazyCondition<SurfaceRules.NoiseThresholdConditionState> {
				protected boolean compute(SurfaceRules.NoiseThresholdConditionState noiseThresholdConditionState) {
					double d = normalNoise.getValue((double)noiseThresholdConditionState.blockX, 0.0, (double)noiseThresholdConditionState.blockZ);
					return d >= NoiseThresholdConditionSource.this.minThreshold && d <= NoiseThresholdConditionSource.this.maxThreshold;
				}
			}

			NoiseThresholdCondition lv = new NoiseThresholdCondition();
			context.noiseThresholdConditions.add(lv);
			return lv;
		}
	}

	static record NoiseThresholdConditionState() {
		final int blockX;
		final int blockZ;

		NoiseThresholdConditionState(int i, int j) {
			this.blockX = i;
			this.blockZ = j;
		}
	}

	static record NotCondition() implements SurfaceRules.Condition {
		private final SurfaceRules.Condition target;

		NotCondition(SurfaceRules.Condition condition) {
			this.target = condition;
		}

		@Override
		public boolean test() {
			return !this.target.test();
		}
	}

	static record NotConditionSource() implements SurfaceRules.ConditionSource {
		private final SurfaceRules.ConditionSource target;
		static final Codec<SurfaceRules.NotConditionSource> CODEC = SurfaceRules.ConditionSource.CODEC
			.<SurfaceRules.NotConditionSource>xmap(SurfaceRules.NotConditionSource::new, SurfaceRules.NotConditionSource::target)
			.fieldOf("invert")
			.codec();

		NotConditionSource(SurfaceRules.ConditionSource conditionSource) {
			this.target = conditionSource;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			return new SurfaceRules.NotCondition((SurfaceRules.Condition)this.target.apply(context));
		}
	}

	public interface RuleSource extends Function<SurfaceRules.Context, SurfaceRules.SurfaceRule> {
		Codec<SurfaceRules.RuleSource> CODEC = Registry.RULE.dispatch(SurfaceRules.RuleSource::codec, Function.identity());

		static Codec<? extends SurfaceRules.RuleSource> bootstrap() {
			Registry.register(Registry.RULE, "bandlands", SurfaceRules.Bandlands.CODEC);
			Registry.register(Registry.RULE, "block", SurfaceRules.BlockRuleSource.CODEC);
			Registry.register(Registry.RULE, "sequence", SurfaceRules.SequenceRuleSource.CODEC);
			Registry.register(Registry.RULE, "condition", SurfaceRules.TestRuleSource.CODEC);
			return (Codec<? extends SurfaceRules.RuleSource>)Registry.RULE.iterator().next();
		}

		Codec<? extends SurfaceRules.RuleSource> codec();
	}

	static record SequenceRule() implements SurfaceRules.SurfaceRule {
		private final List<SurfaceRules.SurfaceRule> rules;

		SequenceRule(List<SurfaceRules.SurfaceRule> list) {
			this.rules = list;
		}

		@Nullable
		@Override
		public BlockState tryApply(int i, int j, int k) {
			for (SurfaceRules.SurfaceRule surfaceRule : this.rules) {
				BlockState blockState = surfaceRule.tryApply(i, j, k);
				if (blockState != null) {
					return blockState;
				}
			}

			return null;
		}
	}

	static record SequenceRuleSource() implements SurfaceRules.RuleSource {
		private final List<SurfaceRules.RuleSource> sequence;
		static final Codec<SurfaceRules.SequenceRuleSource> CODEC = SurfaceRules.RuleSource.CODEC
			.listOf()
			.<SurfaceRules.SequenceRuleSource>xmap(SurfaceRules.SequenceRuleSource::new, SurfaceRules.SequenceRuleSource::sequence)
			.fieldOf("sequence")
			.codec();

		SequenceRuleSource(List<SurfaceRules.RuleSource> list) {
			this.sequence = list;
		}

		@Override
		public Codec<? extends SurfaceRules.RuleSource> codec() {
			return CODEC;
		}

		public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
			if (this.sequence.size() == 1) {
				return (SurfaceRules.SurfaceRule)((SurfaceRules.RuleSource)this.sequence.get(0)).apply(context);
			} else {
				Builder<SurfaceRules.SurfaceRule> builder = ImmutableList.builder();

				for (SurfaceRules.RuleSource ruleSource : this.sequence) {
					builder.add((SurfaceRules.SurfaceRule)ruleSource.apply(context));
				}

				return new SurfaceRules.SequenceRule(builder.build());
			}
		}
	}

	static record StateRule() implements SurfaceRules.SurfaceRule {
		private final BlockState state;

		StateRule(BlockState blockState) {
			this.state = blockState;
		}

		@Override
		public BlockState tryApply(int i, int j, int k) {
			return this.state;
		}
	}

	static enum Steep implements SurfaceRules.ConditionSource {
		INSTANCE;

		static final Codec<SurfaceRules.Steep> CODEC = Codec.unit(INSTANCE);

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			return context.steep;
		}
	}

	static record StoneDepthCheck() implements SurfaceRules.ConditionSource {
		final boolean addRunDepth;
		private final CaveSurface surfaceType;
		static final Codec<SurfaceRules.StoneDepthCheck> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.BOOL.fieldOf("add_run_depth").forGetter(SurfaceRules.StoneDepthCheck::addRunDepth),
						CaveSurface.CODEC.fieldOf("surface_type").forGetter(SurfaceRules.StoneDepthCheck::surfaceType)
					)
					.apply(instance, SurfaceRules.StoneDepthCheck::new)
		);

		StoneDepthCheck(boolean bl, CaveSurface caveSurface) {
			this.addRunDepth = bl;
			this.surfaceType = caveSurface;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			final boolean bl = this.surfaceType == CaveSurface.CEILING;

			class StoneDepthCondition extends SurfaceRules.EagerCondition<SurfaceRules.YConditionState> {
				protected boolean compute(SurfaceRules.YConditionState yConditionState) {
					return (bl ? yConditionState.stoneDepthBelow : yConditionState.stoneDepthAbove) <= 1 + (StoneDepthCheck.this.addRunDepth ? yConditionState.runDepth : 0);
				}
			}

			StoneDepthCondition lv = new StoneDepthCondition();
			context.yConditions.add(lv);
			return lv;
		}
	}

	protected interface SurfaceRule {
		@Nullable
		BlockState tryApply(int i, int j, int k);
	}

	static enum Temperature implements SurfaceRules.ConditionSource {
		INSTANCE;

		static final Codec<SurfaceRules.Temperature> CODEC = Codec.unit(INSTANCE);

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			return context.temperature;
		}
	}

	static record TestRule() implements SurfaceRules.SurfaceRule {
		private final SurfaceRules.Condition condition;
		private final SurfaceRules.SurfaceRule followup;

		TestRule(SurfaceRules.Condition condition, SurfaceRules.SurfaceRule surfaceRule) {
			this.condition = condition;
			this.followup = surfaceRule;
		}

		@Nullable
		@Override
		public BlockState tryApply(int i, int j, int k) {
			return !this.condition.test() ? null : this.followup.tryApply(i, j, k);
		}
	}

	static record TestRuleSource() implements SurfaceRules.RuleSource {
		private final SurfaceRules.ConditionSource ifTrue;
		private final SurfaceRules.RuleSource thenRun;
		static final Codec<SurfaceRules.TestRuleSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						SurfaceRules.ConditionSource.CODEC.fieldOf("if_true").forGetter(SurfaceRules.TestRuleSource::ifTrue),
						SurfaceRules.RuleSource.CODEC.fieldOf("then_run").forGetter(SurfaceRules.TestRuleSource::thenRun)
					)
					.apply(instance, SurfaceRules.TestRuleSource::new)
		);

		TestRuleSource(SurfaceRules.ConditionSource conditionSource, SurfaceRules.RuleSource ruleSource) {
			this.ifTrue = conditionSource;
			this.thenRun = ruleSource;
		}

		@Override
		public Codec<? extends SurfaceRules.RuleSource> codec() {
			return CODEC;
		}

		public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
			return new SurfaceRules.TestRule((SurfaceRules.Condition)this.ifTrue.apply(context), (SurfaceRules.SurfaceRule)this.thenRun.apply(context));
		}
	}

	interface UpdatableCondition<S> extends SurfaceRules.Condition {
		void update(S object);
	}

	static record WaterConditionSource() implements SurfaceRules.ConditionSource {
		final int offset;
		final int runDepthMultiplier;
		final boolean addStoneDepth;
		static final Codec<SurfaceRules.WaterConditionSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("offset").forGetter(SurfaceRules.WaterConditionSource::offset),
						Codec.intRange(-20, 20).fieldOf("run_depth_multiplier").forGetter(SurfaceRules.WaterConditionSource::runDepthMultiplier),
						Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.WaterConditionSource::addStoneDepth)
					)
					.apply(instance, SurfaceRules.WaterConditionSource::new)
		);

		WaterConditionSource(int i, int j, boolean bl) {
			this.offset = i;
			this.runDepthMultiplier = j;
			this.addStoneDepth = bl;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			class WaterCondition extends SurfaceRules.EagerCondition<SurfaceRules.YConditionState> {
				protected boolean compute(SurfaceRules.YConditionState yConditionState) {
					return yConditionState.waterHeight == Integer.MIN_VALUE
						|| yConditionState.blockY + (WaterConditionSource.this.addStoneDepth ? yConditionState.stoneDepthAbove : 0)
							>= yConditionState.waterHeight + WaterConditionSource.this.offset + yConditionState.runDepth * WaterConditionSource.this.runDepthMultiplier;
				}
			}

			WaterCondition lv = new WaterCondition();
			context.yConditions.add(lv);
			return lv;
		}
	}

	static record YConditionSource() implements SurfaceRules.ConditionSource {
		final VerticalAnchor anchor;
		final int runDepthMultiplier;
		final boolean addStoneDepth;
		static final Codec<SurfaceRules.YConditionSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						VerticalAnchor.CODEC.fieldOf("anchor").forGetter(SurfaceRules.YConditionSource::anchor),
						Codec.intRange(-20, 20).fieldOf("run_depth_multiplier").forGetter(SurfaceRules.YConditionSource::runDepthMultiplier),
						Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.YConditionSource::addStoneDepth)
					)
					.apply(instance, SurfaceRules.YConditionSource::new)
		);

		YConditionSource(VerticalAnchor verticalAnchor, int i, boolean bl) {
			this.anchor = verticalAnchor;
			this.runDepthMultiplier = i;
			this.addStoneDepth = bl;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			class YCondition extends SurfaceRules.EagerCondition<SurfaceRules.YConditionState> {
				protected boolean compute(SurfaceRules.YConditionState yConditionState) {
					return yConditionState.blockY + (YConditionSource.this.addStoneDepth ? yConditionState.stoneDepthAbove : 0)
						>= YConditionSource.this.anchor.resolveY(context.context) + yConditionState.runDepth * YConditionSource.this.runDepthMultiplier;
				}
			}

			YCondition lv = new YCondition();
			context.yConditions.add(lv);
			return lv;
		}
	}

	static record YConditionState() {
		final int blockY;
		final int stoneDepthAbove;
		final int stoneDepthBelow;
		final int runDepth;
		final int waterHeight;

		YConditionState(int i, int j, int k, int l, int m) {
			this.blockY = i;
			this.stoneDepthAbove = j;
			this.stoneDepthBelow = k;
			this.runDepth = l;
			this.waterHeight = m;
		}
	}
}
