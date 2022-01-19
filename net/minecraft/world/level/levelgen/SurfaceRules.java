/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

public class SurfaceRules {
    public static final ConditionSource ON_FLOOR = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);
    public static final ConditionSource UNDER_FLOOR = SurfaceRules.stoneDepthCheck(0, true, CaveSurface.FLOOR);
    public static final ConditionSource DEEP_UNDER_FLOOR = SurfaceRules.stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
    public static final ConditionSource VERY_DEEP_UNDER_FLOOR = SurfaceRules.stoneDepthCheck(0, true, 30, CaveSurface.FLOOR);
    public static final ConditionSource ON_CEILING = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.CEILING);
    public static final ConditionSource UNDER_CEILING = SurfaceRules.stoneDepthCheck(0, true, CaveSurface.CEILING);

    public static ConditionSource stoneDepthCheck(int i, boolean bl, CaveSurface caveSurface) {
        return new StoneDepthCheck(i, bl, 0, caveSurface);
    }

    public static ConditionSource stoneDepthCheck(int i, boolean bl, int j, CaveSurface caveSurface) {
        return new StoneDepthCheck(i, bl, j, caveSurface);
    }

    public static ConditionSource not(ConditionSource conditionSource) {
        return new NotConditionSource(conditionSource);
    }

    public static ConditionSource yBlockCheck(VerticalAnchor verticalAnchor, int i) {
        return new YConditionSource(verticalAnchor, i, false);
    }

    public static ConditionSource yStartCheck(VerticalAnchor verticalAnchor, int i) {
        return new YConditionSource(verticalAnchor, i, true);
    }

    public static ConditionSource waterBlockCheck(int i, int j) {
        return new WaterConditionSource(i, j, false);
    }

    public static ConditionSource waterStartCheck(int i, int j) {
        return new WaterConditionSource(i, j, true);
    }

    @SafeVarargs
    public static ConditionSource isBiome(ResourceKey<Biome> ... resourceKeys) {
        return SurfaceRules.isBiome(List.of(resourceKeys));
    }

    private static BiomeConditionSource isBiome(List<ResourceKey<Biome>> list) {
        return new BiomeConditionSource(list);
    }

    public static ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d) {
        return SurfaceRules.noiseCondition(resourceKey, d, Double.MAX_VALUE);
    }

    public static ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d, double e) {
        return new NoiseThresholdConditionSource(resourceKey, d, e);
    }

    public static ConditionSource verticalGradient(String string, VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
        return new VerticalGradientConditionSource(new ResourceLocation(string), verticalAnchor, verticalAnchor2);
    }

    public static ConditionSource steep() {
        return Steep.INSTANCE;
    }

    public static ConditionSource hole() {
        return Hole.INSTANCE;
    }

    public static ConditionSource abovePreliminarySurface() {
        return AbovePreliminarySurface.INSTANCE;
    }

    public static ConditionSource temperature() {
        return Temperature.INSTANCE;
    }

    public static RuleSource ifTrue(ConditionSource conditionSource, RuleSource ruleSource) {
        return new TestRuleSource(conditionSource, ruleSource);
    }

    public static RuleSource sequence(RuleSource ... ruleSources) {
        if (ruleSources.length == 0) {
            throw new IllegalArgumentException("Need at least 1 rule for a sequence");
        }
        return new SequenceRuleSource(Arrays.asList(ruleSources));
    }

    public static RuleSource state(BlockState blockState) {
        return new BlockRuleSource(blockState);
    }

    public static RuleSource bandlands() {
        return Bandlands.INSTANCE;
    }

    record StoneDepthCheck(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType) implements ConditionSource
    {
        static final Codec<StoneDepthCheck> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("offset")).forGetter(StoneDepthCheck::offset), ((MapCodec)Codec.BOOL.fieldOf("add_surface_depth")).forGetter(StoneDepthCheck::addSurfaceDepth), ((MapCodec)Codec.INT.fieldOf("secondary_depth_range")).forGetter(StoneDepthCheck::secondaryDepthRange), ((MapCodec)CaveSurface.CODEC.fieldOf("surface_type")).forGetter(StoneDepthCheck::surfaceType)).apply((Applicative<StoneDepthCheck, ?>)instance, StoneDepthCheck::new));

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            final boolean bl = this.surfaceType == CaveSurface.CEILING;
            class StoneDepthCondition
            extends LazyYCondition {
                StoneDepthCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    int i = bl ? this.context.stoneDepthBelow : this.context.stoneDepthAbove;
                    int j = StoneDepthCheck.this.addSurfaceDepth ? this.context.surfaceDepth : 0;
                    int k = StoneDepthCheck.this.secondaryDepthRange == 0 ? 0 : (int)Mth.map(this.context.getSurfaceSecondary(), -1.0, 1.0, 0.0, (double)StoneDepthCheck.this.secondaryDepthRange);
                    return i <= 1 + StoneDepthCheck.this.offset + j + k;
                }
            }
            return new StoneDepthCondition();
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record NotConditionSource(ConditionSource target) implements ConditionSource
    {
        static final Codec<NotConditionSource> CODEC = ((MapCodec)ConditionSource.CODEC.xmap(NotConditionSource::new, NotConditionSource::target).fieldOf("invert")).codec();

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return new NotCondition((Condition)this.target.apply(context));
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    public static interface ConditionSource
    extends Function<Context, Condition> {
        public static final Codec<ConditionSource> CODEC = Registry.CONDITION.byNameCodec().dispatch(ConditionSource::codec, Function.identity());

        public static Codec<? extends ConditionSource> bootstrap() {
            Registry.register(Registry.CONDITION, "biome", BiomeConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "noise_threshold", NoiseThresholdConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "vertical_gradient", VerticalGradientConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "y_above", YConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "water", WaterConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "temperature", Temperature.CODEC);
            Registry.register(Registry.CONDITION, "steep", Steep.CODEC);
            Registry.register(Registry.CONDITION, "not", NotConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "hole", Hole.CODEC);
            Registry.register(Registry.CONDITION, "above_preliminary_surface", AbovePreliminarySurface.CODEC);
            Registry.register(Registry.CONDITION, "stone_depth", StoneDepthCheck.CODEC);
            return (Codec)Registry.CONDITION.iterator().next();
        }

        public Codec<? extends ConditionSource> codec();
    }

    record YConditionSource(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) implements ConditionSource
    {
        static final Codec<YConditionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)VerticalAnchor.CODEC.fieldOf("anchor")).forGetter(YConditionSource::anchor), ((MapCodec)Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier")).forGetter(YConditionSource::surfaceDepthMultiplier), ((MapCodec)Codec.BOOL.fieldOf("add_stone_depth")).forGetter(YConditionSource::addStoneDepth)).apply((Applicative<YConditionSource, ?>)instance, YConditionSource::new));

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            class YCondition
            extends LazyYCondition {
                YCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    return this.context.blockY + (YConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0) >= YConditionSource.this.anchor.resolveY(this.context.context) + this.context.surfaceDepth * YConditionSource.this.surfaceDepthMultiplier;
                }
            }
            return new YCondition();
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record WaterConditionSource(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) implements ConditionSource
    {
        static final Codec<WaterConditionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("offset")).forGetter(WaterConditionSource::offset), ((MapCodec)Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier")).forGetter(WaterConditionSource::surfaceDepthMultiplier), ((MapCodec)Codec.BOOL.fieldOf("add_stone_depth")).forGetter(WaterConditionSource::addStoneDepth)).apply((Applicative<WaterConditionSource, ?>)instance, WaterConditionSource::new));

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            class WaterCondition
            extends LazyYCondition {
                WaterCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    return this.context.waterHeight == Integer.MIN_VALUE || this.context.blockY + (WaterConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0) >= this.context.waterHeight + WaterConditionSource.this.offset + this.context.surfaceDepth * WaterConditionSource.this.surfaceDepthMultiplier;
                }
            }
            return new WaterCondition();
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record BiomeConditionSource(List<ResourceKey<Biome>> biomes) implements ConditionSource
    {
        static final Codec<BiomeConditionSource> CODEC = ((MapCodec)ResourceKey.codec(Registry.BIOME_REGISTRY).listOf().fieldOf("biome_is")).xmap(SurfaceRules::isBiome, BiomeConditionSource::biomes).codec();

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            final Set<ResourceKey<Biome>> set = Set.copyOf(this.biomes);
            class BiomeCondition
            extends LazyYCondition {
                BiomeCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    return set.contains(this.context.biomeKey.get());
                }
            }
            return new BiomeCondition();
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record NoiseThresholdConditionSource(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold, double maxThreshold) implements ConditionSource
    {
        static final Codec<NoiseThresholdConditionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceKey.codec(Registry.NOISE_REGISTRY).fieldOf("noise")).forGetter(NoiseThresholdConditionSource::noise), ((MapCodec)Codec.DOUBLE.fieldOf("min_threshold")).forGetter(NoiseThresholdConditionSource::minThreshold), ((MapCodec)Codec.DOUBLE.fieldOf("max_threshold")).forGetter(NoiseThresholdConditionSource::maxThreshold)).apply((Applicative<NoiseThresholdConditionSource, ?>)instance, NoiseThresholdConditionSource::new));

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            final NormalNoise normalNoise = context.system.getOrCreateNoise(this.noise);
            class NoiseThresholdCondition
            extends LazyXZCondition {
                NoiseThresholdCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    double d = normalNoise.getValue(this.context.blockX, 0.0, this.context.blockZ);
                    return d >= NoiseThresholdConditionSource.this.minThreshold && d <= NoiseThresholdConditionSource.this.maxThreshold;
                }
            }
            return new NoiseThresholdCondition();
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record VerticalGradientConditionSource(ResourceLocation randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove) implements ConditionSource
    {
        static final Codec<VerticalGradientConditionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.fieldOf("random_name")).forGetter(VerticalGradientConditionSource::randomName), ((MapCodec)VerticalAnchor.CODEC.fieldOf("true_at_and_below")).forGetter(VerticalGradientConditionSource::trueAtAndBelow), ((MapCodec)VerticalAnchor.CODEC.fieldOf("false_at_and_above")).forGetter(VerticalGradientConditionSource::falseAtAndAbove)).apply((Applicative<VerticalGradientConditionSource, ?>)instance, VerticalGradientConditionSource::new));

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            final int i = this.trueAtAndBelow().resolveY(context.context);
            final int j = this.falseAtAndAbove().resolveY(context.context);
            final PositionalRandomFactory positionalRandomFactory = context.system.getOrCreateRandomFactory(this.randomName());
            class VerticalGradientCondition
            extends LazyYCondition {
                VerticalGradientCondition() {
                    super(context2);
                }

                @Override
                protected boolean compute() {
                    int i2 = this.context.blockY;
                    if (i2 <= i) {
                        return true;
                    }
                    if (i2 >= j) {
                        return false;
                    }
                    double d = Mth.map((double)i2, (double)i, (double)j, 1.0, 0.0);
                    RandomSource randomSource = positionalRandomFactory.at(this.context.blockX, i2, this.context.blockZ);
                    return (double)randomSource.nextFloat() < d;
                }
            }
            return new VerticalGradientCondition();
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    static enum Steep implements ConditionSource
    {
        INSTANCE;

        static final Codec<Steep> CODEC;

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return context.steep;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = Codec.unit(INSTANCE);
        }
    }

    static enum Hole implements ConditionSource
    {
        INSTANCE;

        static final Codec<Hole> CODEC;

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return context.hole;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = Codec.unit(INSTANCE);
        }
    }

    static enum AbovePreliminarySurface implements ConditionSource
    {
        INSTANCE;

        static final Codec<AbovePreliminarySurface> CODEC;

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return context.abovePreliminarySurface;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = Codec.unit(INSTANCE);
        }
    }

    static enum Temperature implements ConditionSource
    {
        INSTANCE;

        static final Codec<Temperature> CODEC;

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            return context.temperature;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = Codec.unit(INSTANCE);
        }
    }

    record TestRuleSource(ConditionSource ifTrue, RuleSource thenRun) implements RuleSource
    {
        static final Codec<TestRuleSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConditionSource.CODEC.fieldOf("if_true")).forGetter(TestRuleSource::ifTrue), ((MapCodec)RuleSource.CODEC.fieldOf("then_run")).forGetter(TestRuleSource::thenRun)).apply((Applicative<TestRuleSource, ?>)instance, TestRuleSource::new));

        @Override
        public Codec<? extends RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return new TestRule((Condition)this.ifTrue.apply(context), (SurfaceRule)this.thenRun.apply(context));
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    public static interface RuleSource
    extends Function<Context, SurfaceRule> {
        public static final Codec<RuleSource> CODEC = Registry.RULE.byNameCodec().dispatch(RuleSource::codec, Function.identity());

        public static Codec<? extends RuleSource> bootstrap() {
            Registry.register(Registry.RULE, "bandlands", Bandlands.CODEC);
            Registry.register(Registry.RULE, "block", BlockRuleSource.CODEC);
            Registry.register(Registry.RULE, "sequence", SequenceRuleSource.CODEC);
            Registry.register(Registry.RULE, "condition", TestRuleSource.CODEC);
            return (Codec)Registry.RULE.iterator().next();
        }

        public Codec<? extends RuleSource> codec();
    }

    record SequenceRuleSource(List<RuleSource> sequence) implements RuleSource
    {
        static final Codec<SequenceRuleSource> CODEC = ((MapCodec)RuleSource.CODEC.listOf().xmap(SequenceRuleSource::new, SequenceRuleSource::sequence).fieldOf("sequence")).codec();

        @Override
        public Codec<? extends RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            if (this.sequence.size() == 1) {
                return (SurfaceRule)this.sequence.get(0).apply(context);
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            for (RuleSource ruleSource : this.sequence) {
                builder.add((SurfaceRule)ruleSource.apply(context));
            }
            return new SequenceRule((List<SurfaceRule>)((Object)builder.build()));
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record BlockRuleSource(BlockState resultState, StateRule rule) implements RuleSource
    {
        static final Codec<BlockRuleSource> CODEC = ((MapCodec)BlockState.CODEC.xmap(BlockRuleSource::new, BlockRuleSource::resultState).fieldOf("result_state")).codec();

        BlockRuleSource(BlockState blockState) {
            this(blockState, new StateRule(blockState));
        }

        @Override
        public Codec<? extends RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return this.rule;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    static enum Bandlands implements RuleSource
    {
        INSTANCE;

        static final Codec<Bandlands> CODEC;

        @Override
        public Codec<? extends RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return context.system::getBand;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }

        static {
            CODEC = Codec.unit(INSTANCE);
        }
    }

    record SequenceRule(List<SurfaceRule> rules) implements SurfaceRule
    {
        @Override
        @Nullable
        public BlockState tryApply(int i, int j, int k) {
            for (SurfaceRule surfaceRule : this.rules) {
                BlockState blockState = surfaceRule.tryApply(i, j, k);
                if (blockState == null) continue;
                return blockState;
            }
            return null;
        }
    }

    record TestRule(Condition condition, SurfaceRule followup) implements SurfaceRule
    {
        @Override
        @Nullable
        public BlockState tryApply(int i, int j, int k) {
            if (!this.condition.test()) {
                return null;
            }
            return this.followup.tryApply(i, j, k);
        }
    }

    record StateRule(BlockState state) implements SurfaceRule
    {
        @Override
        public BlockState tryApply(int i, int j, int k) {
            return this.state;
        }
    }

    protected static interface SurfaceRule {
        @Nullable
        public BlockState tryApply(int var1, int var2, int var3);
    }

    record NotCondition(Condition target) implements Condition
    {
        @Override
        public boolean test() {
            return !this.target.test();
        }
    }

    static abstract class LazyYCondition
    extends LazyCondition {
        protected LazyYCondition(Context context) {
            super(context);
        }

        @Override
        protected long getContextLastUpdate() {
            return this.context.lastUpdateY;
        }
    }

    static abstract class LazyXZCondition
    extends LazyCondition {
        protected LazyXZCondition(Context context) {
            super(context);
        }

        @Override
        protected long getContextLastUpdate() {
            return this.context.lastUpdateXZ;
        }
    }

    static abstract class LazyCondition
    implements Condition {
        protected final Context context;
        private long lastUpdate;
        @Nullable
        Boolean result;

        protected LazyCondition(Context context) {
            this.context = context;
            this.lastUpdate = this.getContextLastUpdate() - 1L;
        }

        @Override
        public boolean test() {
            long l = this.getContextLastUpdate();
            if (l == this.lastUpdate) {
                if (this.result == null) {
                    throw new IllegalStateException("Update triggered but the result is null");
                }
                return this.result;
            }
            this.lastUpdate = l;
            this.result = this.compute();
            return this.result;
        }

        protected abstract long getContextLastUpdate();

        protected abstract boolean compute();
    }

    static interface Condition {
        public boolean test();
    }

    protected static final class Context {
        private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
        private static final int SURFACE_CELL_BITS = 4;
        private static final int SURFACE_CELL_SIZE = 16;
        private static final int SURFACE_CELL_MASK = 15;
        final SurfaceSystem system;
        final Condition temperature = new TemperatureHelperCondition(this);
        final Condition steep = new SteepMaterialCondition(this);
        final Condition hole = new HoleCondition(this);
        final Condition abovePreliminarySurface = new AbovePreliminarySurfaceCondition();
        final ChunkAccess chunk;
        private final NoiseChunk noiseChunk;
        private final Function<BlockPos, Biome> biomeGetter;
        private final Registry<Biome> biomes;
        final WorldGenerationContext context;
        private long lastPreliminarySurfaceCellOrigin = Long.MAX_VALUE;
        private final int[] preliminarySurfaceCache = new int[4];
        long lastUpdateXZ = -9223372036854775807L;
        int blockX;
        int blockZ;
        int surfaceDepth;
        private long lastSurfaceDepth2Update = this.lastUpdateXZ - 1L;
        private double surfaceSecondary;
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

        protected Context(SurfaceSystem surfaceSystem, ChunkAccess chunkAccess, NoiseChunk noiseChunk, Function<BlockPos, Biome> function, Registry<Biome> registry, WorldGenerationContext worldGenerationContext) {
            this.system = surfaceSystem;
            this.chunk = chunkAccess;
            this.noiseChunk = noiseChunk;
            this.biomeGetter = function;
            this.biomes = registry;
            this.context = worldGenerationContext;
        }

        protected void updateXZ(int i, int j) {
            ++this.lastUpdateXZ;
            ++this.lastUpdateY;
            this.blockX = i;
            this.blockZ = j;
            this.surfaceDepth = this.system.getSurfaceDepth(i, j);
        }

        protected void updateY(int i, int j, int k, int l, int m, int n) {
            ++this.lastUpdateY;
            this.biome = Suppliers.memoize(() -> this.biomeGetter.apply(this.pos.set(l, m, n)));
            this.biomeKey = Suppliers.memoize(() -> this.biomes.getResourceKey(this.biome.get()).orElseThrow(() -> new IllegalStateException("Unregistered biome: " + this.biome)));
            this.blockY = m;
            this.waterHeight = k;
            this.stoneDepthBelow = j;
            this.stoneDepthAbove = i;
        }

        protected double getSurfaceSecondary() {
            if (this.lastSurfaceDepth2Update != this.lastUpdateXZ) {
                this.lastSurfaceDepth2Update = this.lastUpdateXZ;
                this.surfaceSecondary = this.system.getSurfaceSecondary(this.blockX, this.blockZ);
            }
            return this.surfaceSecondary;
        }

        private static int blockCoordToSurfaceCell(int i) {
            return i >> 4;
        }

        private static int surfaceCellToBlockCoord(int i) {
            return i << 4;
        }

        protected int getMinSurfaceLevel() {
            if (this.lastMinSurfaceLevelUpdate != this.lastUpdateXZ) {
                int j;
                this.lastMinSurfaceLevelUpdate = this.lastUpdateXZ;
                int i = Context.blockCoordToSurfaceCell(this.blockX);
                long l = ChunkPos.asLong(i, j = Context.blockCoordToSurfaceCell(this.blockZ));
                if (this.lastPreliminarySurfaceCellOrigin != l) {
                    this.lastPreliminarySurfaceCellOrigin = l;
                    this.preliminarySurfaceCache[0] = this.noiseChunk.preliminarySurfaceLevel(Context.surfaceCellToBlockCoord(i), Context.surfaceCellToBlockCoord(j));
                    this.preliminarySurfaceCache[1] = this.noiseChunk.preliminarySurfaceLevel(Context.surfaceCellToBlockCoord(i + 1), Context.surfaceCellToBlockCoord(j));
                    this.preliminarySurfaceCache[2] = this.noiseChunk.preliminarySurfaceLevel(Context.surfaceCellToBlockCoord(i), Context.surfaceCellToBlockCoord(j + 1));
                    this.preliminarySurfaceCache[3] = this.noiseChunk.preliminarySurfaceLevel(Context.surfaceCellToBlockCoord(i + 1), Context.surfaceCellToBlockCoord(j + 1));
                }
                int k = Mth.floor(Mth.lerp2((float)(this.blockX & 0xF) / 16.0f, (float)(this.blockZ & 0xF) / 16.0f, this.preliminarySurfaceCache[0], this.preliminarySurfaceCache[1], this.preliminarySurfaceCache[2], this.preliminarySurfaceCache[3]));
                this.minSurfaceLevel = k + this.surfaceDepth - 8;
            }
            return this.minSurfaceLevel;
        }

        static class TemperatureHelperCondition
        extends LazyYCondition {
            TemperatureHelperCondition(Context context) {
                super(context);
            }

            @Override
            protected boolean compute() {
                return this.context.biome.get().coldEnoughToSnow(this.context.pos.set(this.context.blockX, this.context.blockY, this.context.blockZ));
            }
        }

        static class SteepMaterialCondition
        extends LazyXZCondition {
            SteepMaterialCondition(Context context) {
                super(context);
            }

            @Override
            protected boolean compute() {
                int r;
                int i = this.context.blockX & 0xF;
                int j = this.context.blockZ & 0xF;
                int k = Math.max(j - 1, 0);
                int l = Math.min(j + 1, 15);
                ChunkAccess chunkAccess = this.context.chunk;
                int m = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, k);
                int n = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, l);
                if (n >= m + 4) {
                    return true;
                }
                int o = Math.max(i - 1, 0);
                int p = Math.min(i + 1, 15);
                int q = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, o, j);
                return q >= (r = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p, j)) + 4;
            }
        }

        static final class HoleCondition
        extends LazyXZCondition {
            HoleCondition(Context context) {
                super(context);
            }

            @Override
            protected boolean compute() {
                return this.context.surfaceDepth <= 0;
            }
        }

        final class AbovePreliminarySurfaceCondition
        implements Condition {
            AbovePreliminarySurfaceCondition() {
            }

            @Override
            public boolean test() {
                return Context.this.blockY >= Context.this.getMinSurfaceLevel();
            }
        }
    }
}

