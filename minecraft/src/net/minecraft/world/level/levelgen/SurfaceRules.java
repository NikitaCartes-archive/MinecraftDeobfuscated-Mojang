package net.minecraft.world.level.levelgen;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRules {
	public static final SurfaceRules.ConditionSource ON_FLOOR = stoneDepthCheck(0, false, false, CaveSurface.FLOOR);
	public static final SurfaceRules.ConditionSource UNDER_FLOOR = stoneDepthCheck(0, true, false, CaveSurface.FLOOR);
	public static final SurfaceRules.ConditionSource ON_CEILING = stoneDepthCheck(0, false, false, CaveSurface.CEILING);
	public static final SurfaceRules.ConditionSource UNDER_CEILING = stoneDepthCheck(0, true, false, CaveSurface.CEILING);

	public static SurfaceRules.ConditionSource stoneDepthCheck(int i, boolean bl, boolean bl2, CaveSurface caveSurface) {
		return new SurfaceRules.StoneDepthCheck(i, bl, bl2, caveSurface);
	}

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
		return noiseCondition(resourceKey, d, Double.MAX_VALUE);
	}

	public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d, double e) {
		return new SurfaceRules.NoiseThresholdConditionSource(resourceKey, d, e);
	}

	public static SurfaceRules.ConditionSource verticalGradient(String string, VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		return new SurfaceRules.VerticalGradientConditionSource(new ResourceLocation(string), verticalAnchor, verticalAnchor2);
	}

	public static SurfaceRules.ConditionSource steep() {
		return SurfaceRules.Steep.INSTANCE;
	}

	public static SurfaceRules.ConditionSource hole() {
		return SurfaceRules.Hole.INSTANCE;
	}

	public static SurfaceRules.ConditionSource abovePreliminarySurface() {
		return SurfaceRules.AbovePreliminarySurface.INSTANCE;
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

	static enum AbovePreliminarySurface implements SurfaceRules.ConditionSource {
		INSTANCE;

		static final Codec<SurfaceRules.AbovePreliminarySurface> CODEC = Codec.unit(INSTANCE);

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			return context.abovePreliminarySurface;
		}
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

			class BiomeCondition extends SurfaceRules.LazyYCondition {
				BiomeCondition() {
					super(context);
				}

				@Override
				protected boolean compute() {
					return set.contains(this.context.biomeKey.get());
				}
			}

			return new BiomeCondition();
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
		Codec<SurfaceRules.ConditionSource> CODEC = Registry.CONDITION.byNameCodec().dispatch(SurfaceRules.ConditionSource::codec, Function.identity());

		static Codec<? extends SurfaceRules.ConditionSource> bootstrap() {
			Registry.register(Registry.CONDITION, "biome", SurfaceRules.BiomeConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "noise_threshold", SurfaceRules.NoiseThresholdConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "vertical_gradient", SurfaceRules.VerticalGradientConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "y_above", SurfaceRules.YConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "water", SurfaceRules.WaterConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "temperature", SurfaceRules.Temperature.CODEC);
			Registry.register(Registry.CONDITION, "steep", SurfaceRules.Steep.CODEC);
			Registry.register(Registry.CONDITION, "not", SurfaceRules.NotConditionSource.CODEC);
			Registry.register(Registry.CONDITION, "hole", SurfaceRules.Hole.CODEC);
			Registry.register(Registry.CONDITION, "above_preliminary_surface", SurfaceRules.AbovePreliminarySurface.CODEC);
			Registry.register(Registry.CONDITION, "stone_depth", SurfaceRules.StoneDepthCheck.CODEC);
			return (Codec<? extends SurfaceRules.ConditionSource>)Registry.CONDITION.iterator().next();
		}

		Codec<? extends SurfaceRules.ConditionSource> codec();
	}

	protected static final class Context {
		final SurfaceSystem system;
		final SurfaceRules.Condition temperature = new SurfaceRules.Context.TemperatureHelperCondition(this);
		final SurfaceRules.Condition steep = new SurfaceRules.Context.SteepMaterialCondition(this);
		final SurfaceRules.Condition hole = new SurfaceRules.Context.HoleCondition(this);
		final SurfaceRules.Condition abovePreliminarySurface = new SurfaceRules.Context.AbovePreliminarySurfaceCondition();
		final ChunkAccess chunk;
		private final NoiseChunk noiseChunk;
		private final Function<BlockPos, Biome> biomeGetter;
		private final Registry<Biome> biomes;
		final WorldGenerationContext context;
		long lastUpdateXZ = -9223372036854775807L;
		int blockX;
		int blockZ;
		int surfaceDepth;
		private long lastSurfaceDepth2Update = this.lastUpdateXZ - 1L;
		private int surfaceSecondaryDepth;
		private long lastMinSurfaceLevelUpdate = this.lastUpdateXZ - 1L;
		private int minSurfaceLevel;
		long lastUpdateY = -9223372036854775807L;
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Supplier<Biome> biome;
		Supplier<ResourceKey<Biome>> biomeKey;
		int blockY;
		int waterHeight;
		int stoneDepthBelow;
		int stoneDepthAbove;

		protected Context(
			SurfaceSystem surfaceSystem,
			ChunkAccess chunkAccess,
			NoiseChunk noiseChunk,
			Function<BlockPos, Biome> function,
			Registry<Biome> registry,
			WorldGenerationContext worldGenerationContext
		) {
			this.system = surfaceSystem;
			this.chunk = chunkAccess;
			this.noiseChunk = noiseChunk;
			this.biomeGetter = function;
			this.biomes = registry;
			this.context = worldGenerationContext;
		}

		protected void updateXZ(int i, int j) {
			this.lastUpdateXZ++;
			this.lastUpdateY++;
			this.blockX = i;
			this.blockZ = j;
			this.surfaceDepth = this.system.getSurfaceDepth(i, j);
		}

		protected void updateY(int i, int j, int k, int l, int m, int n) {
			this.lastUpdateY++;
			this.biome = Suppliers.memoize(() -> (Biome)this.biomeGetter.apply(this.pos.set(l, m, n)));
			this.biomeKey = Suppliers.memoize(
				() -> (ResourceKey<Biome>)this.biomes
						.getResourceKey((Biome)this.biome.get())
						.orElseThrow(() -> new IllegalStateException("Unregistered biome: " + this.biome))
			);
			this.blockY = m;
			this.waterHeight = k;
			this.stoneDepthBelow = j;
			this.stoneDepthAbove = i;
		}

		protected int getSurfaceSecondaryDepth() {
			if (this.lastSurfaceDepth2Update != this.lastUpdateXZ) {
				this.lastSurfaceDepth2Update = this.lastUpdateXZ;
				this.surfaceSecondaryDepth = this.system.getSurfaceSecondaryDepth(this.blockX, this.blockZ);
			}

			return this.surfaceSecondaryDepth;
		}

		protected int getMinSurfaceLevel() {
			if (this.lastMinSurfaceLevelUpdate != this.lastUpdateXZ) {
				this.lastMinSurfaceLevelUpdate = this.lastUpdateXZ;
				this.minSurfaceLevel = this.system.getMinSurfaceLevel(this.noiseChunk, this.blockX, this.blockZ);
			}

			return this.minSurfaceLevel;
		}

		final class AbovePreliminarySurfaceCondition implements SurfaceRules.Condition {
			@Override
			public boolean test() {
				return Context.this.blockY >= Context.this.getMinSurfaceLevel();
			}
		}

		static final class HoleCondition extends SurfaceRules.LazyXZCondition {
			HoleCondition(SurfaceRules.Context context) {
				super(context);
			}

			@Override
			protected boolean compute() {
				return this.context.surfaceDepth <= 0;
			}
		}

		static class SteepMaterialCondition extends SurfaceRules.LazyXZCondition {
			SteepMaterialCondition(SurfaceRules.Context context) {
				super(context);
			}

			@Override
			protected boolean compute() {
				int i = this.context.blockX & 15;
				int j = this.context.blockZ & 15;
				int k = Math.max(j - 1, 0);
				int l = Math.min(j + 1, 15);
				ChunkAccess chunkAccess = this.context.chunk;
				int m = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, k);
				int n = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, l);
				if (n >= m + 4) {
					return true;
				} else {
					int o = Math.max(i - 1, 0);
					int p = Math.min(i + 1, 15);
					int q = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, o, j);
					int r = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p, j);
					return q >= r + 4;
				}
			}
		}

		static class TemperatureHelperCondition extends SurfaceRules.LazyYCondition {
			TemperatureHelperCondition(SurfaceRules.Context context) {
				super(context);
			}

			@Override
			protected boolean compute() {
				return ((Biome)this.context.biome.get()).getTemperature(this.context.pos.set(this.context.blockX, this.context.blockY, this.context.blockZ)) < 0.15F;
			}
		}
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

	abstract static class LazyCondition implements SurfaceRules.Condition {
		protected final SurfaceRules.Context context;
		private long lastUpdate;
		@Nullable
		Boolean result;

		protected LazyCondition(SurfaceRules.Context context) {
			this.context = context;
			this.lastUpdate = this.getContextLastUpdate() - 1L;
		}

		@Override
		public boolean test() {
			long l = this.getContextLastUpdate();
			if (l == this.lastUpdate) {
				if (this.result == null) {
					throw new IllegalStateException("Update triggered but the result is null");
				} else {
					return this.result;
				}
			} else {
				this.lastUpdate = l;
				this.result = this.compute();
				return this.result;
			}
		}

		protected abstract long getContextLastUpdate();

		protected abstract boolean compute();
	}

	abstract static class LazyXZCondition extends SurfaceRules.LazyCondition {
		protected LazyXZCondition(SurfaceRules.Context context) {
			super(context);
		}

		@Override
		protected long getContextLastUpdate() {
			return this.context.lastUpdateXZ;
		}
	}

	abstract static class LazyYCondition extends SurfaceRules.LazyCondition {
		protected LazyYCondition(SurfaceRules.Context context) {
			super(context);
		}

		@Override
		protected long getContextLastUpdate() {
			return this.context.lastUpdateY;
		}
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

			class NoiseThresholdCondition extends SurfaceRules.LazyXZCondition {
				NoiseThresholdCondition() {
					super(context);
				}

				@Override
				protected boolean compute() {
					double d = normalNoise.getValue((double)this.context.blockX, 0.0, (double)this.context.blockZ);
					return d >= NoiseThresholdConditionSource.this.minThreshold && d <= NoiseThresholdConditionSource.this.maxThreshold;
				}
			}

			return new NoiseThresholdCondition();
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
		Codec<SurfaceRules.RuleSource> CODEC = Registry.RULE.byNameCodec().dispatch(SurfaceRules.RuleSource::codec, Function.identity());

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
		final int offset;
		final boolean addSurfaceDepth;
		final boolean addSurfaceSecondaryDepth;
		private final CaveSurface surfaceType;
		static final Codec<SurfaceRules.StoneDepthCheck> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("offset").forGetter(SurfaceRules.StoneDepthCheck::offset),
						Codec.BOOL.fieldOf("add_surface_depth").forGetter(SurfaceRules.StoneDepthCheck::addSurfaceDepth),
						Codec.BOOL.fieldOf("add_surface_secondary_depth").forGetter(SurfaceRules.StoneDepthCheck::addSurfaceSecondaryDepth),
						CaveSurface.CODEC.fieldOf("surface_type").forGetter(SurfaceRules.StoneDepthCheck::surfaceType)
					)
					.apply(instance, SurfaceRules.StoneDepthCheck::new)
		);

		StoneDepthCheck(int i, boolean bl, boolean bl2, CaveSurface caveSurface) {
			this.offset = i;
			this.addSurfaceDepth = bl;
			this.addSurfaceSecondaryDepth = bl2;
			this.surfaceType = caveSurface;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			final boolean bl = this.surfaceType == CaveSurface.CEILING;

			class StoneDepthCondition extends SurfaceRules.LazyYCondition {
				StoneDepthCondition() {
					super(context);
				}

				@Override
				protected boolean compute() {
					return (bl ? this.context.stoneDepthBelow : this.context.stoneDepthAbove)
						<= 1
							+ StoneDepthCheck.this.offset
							+ (StoneDepthCheck.this.addSurfaceDepth ? this.context.surfaceDepth : 0)
							+ (StoneDepthCheck.this.addSurfaceSecondaryDepth ? this.context.getSurfaceSecondaryDepth() : 0);
				}
			}

			return new StoneDepthCondition();
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

	static record VerticalGradientConditionSource() implements SurfaceRules.ConditionSource {
		private final ResourceLocation randomName;
		private final VerticalAnchor trueAtAndBelow;
		private final VerticalAnchor falseAtAndAbove;
		static final Codec<SurfaceRules.VerticalGradientConditionSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ResourceLocation.CODEC.fieldOf("random_name").forGetter(SurfaceRules.VerticalGradientConditionSource::randomName),
						VerticalAnchor.CODEC.fieldOf("true_at_and_below").forGetter(SurfaceRules.VerticalGradientConditionSource::trueAtAndBelow),
						VerticalAnchor.CODEC.fieldOf("false_at_and_above").forGetter(SurfaceRules.VerticalGradientConditionSource::falseAtAndAbove)
					)
					.apply(instance, SurfaceRules.VerticalGradientConditionSource::new)
		);

		VerticalGradientConditionSource(ResourceLocation resourceLocation, VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
			this.randomName = resourceLocation;
			this.trueAtAndBelow = verticalAnchor;
			this.falseAtAndAbove = verticalAnchor2;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			final int i = this.trueAtAndBelow().resolveY(context.context);
			final int j = this.falseAtAndAbove().resolveY(context.context);
			final PositionalRandomFactory positionalRandomFactory = context.system.getOrCreateRandomFactory(this.randomName());

			class VerticalGradientCondition extends SurfaceRules.LazyYCondition {
				VerticalGradientCondition() {
					super(context);
				}

				@Override
				protected boolean compute() {
					int i = this.context.blockY;
					if (i <= i) {
						return true;
					} else if (i >= j) {
						return false;
					} else {
						double d = Mth.map((double)i, (double)i, (double)j, 1.0, 0.0);
						RandomSource randomSource = positionalRandomFactory.at(this.context.blockX, i, this.context.blockZ);
						return (double)randomSource.nextFloat() < d;
					}
				}
			}

			return new VerticalGradientCondition();
		}
	}

	static record WaterConditionSource() implements SurfaceRules.ConditionSource {
		final int offset;
		final int surfaceDepthMultiplier;
		final boolean addStoneDepth;
		static final Codec<SurfaceRules.WaterConditionSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("offset").forGetter(SurfaceRules.WaterConditionSource::offset),
						Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(SurfaceRules.WaterConditionSource::surfaceDepthMultiplier),
						Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.WaterConditionSource::addStoneDepth)
					)
					.apply(instance, SurfaceRules.WaterConditionSource::new)
		);

		WaterConditionSource(int i, int j, boolean bl) {
			this.offset = i;
			this.surfaceDepthMultiplier = j;
			this.addStoneDepth = bl;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			class WaterCondition extends SurfaceRules.LazyYCondition {
				WaterCondition() {
					super(context);
				}

				@Override
				protected boolean compute() {
					return this.context.waterHeight == Integer.MIN_VALUE
						|| this.context.blockY + (WaterConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0)
							>= this.context.waterHeight + WaterConditionSource.this.offset + this.context.surfaceDepth * WaterConditionSource.this.surfaceDepthMultiplier;
				}
			}

			return new WaterCondition();
		}
	}

	static record YConditionSource() implements SurfaceRules.ConditionSource {
		final VerticalAnchor anchor;
		final int surfaceDepthMultiplier;
		final boolean addStoneDepth;
		static final Codec<SurfaceRules.YConditionSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						VerticalAnchor.CODEC.fieldOf("anchor").forGetter(SurfaceRules.YConditionSource::anchor),
						Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(SurfaceRules.YConditionSource::surfaceDepthMultiplier),
						Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.YConditionSource::addStoneDepth)
					)
					.apply(instance, SurfaceRules.YConditionSource::new)
		);

		YConditionSource(VerticalAnchor verticalAnchor, int i, boolean bl) {
			this.anchor = verticalAnchor;
			this.surfaceDepthMultiplier = i;
			this.addStoneDepth = bl;
		}

		@Override
		public Codec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}

		public SurfaceRules.Condition apply(SurfaceRules.Context context) {
			class YCondition extends SurfaceRules.LazyYCondition {
				YCondition() {
					super(context);
				}

				@Override
				protected boolean compute() {
					return this.context.blockY + (YConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0)
						>= YConditionSource.this.anchor.resolveY(this.context.context) + this.context.surfaceDepth * YConditionSource.this.surfaceDepthMultiplier;
				}
			}

			return new YCondition();
		}
	}
}
