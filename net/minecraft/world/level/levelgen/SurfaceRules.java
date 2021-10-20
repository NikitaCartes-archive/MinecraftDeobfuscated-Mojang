/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

public class SurfaceRules {
    public static final ConditionSource ON_FLOOR = new StoneDepthCheck(false, CaveSurface.FLOOR);
    public static final ConditionSource UNDER_FLOOR = new StoneDepthCheck(true, CaveSurface.FLOOR);
    public static final ConditionSource UNDER_CEILING = new StoneDepthCheck(true, CaveSurface.CEILING);

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
        return SurfaceRules.noiseCondition(resourceKey, d, Double.POSITIVE_INFINITY);
    }

    public static ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> resourceKey, double d, double e) {
        return new NoiseThresholdConditionSource(resourceKey, d, e);
    }

    public static ConditionSource steep() {
        return Steep.INSTANCE;
    }

    public static ConditionSource hole() {
        return Hole.INSTANCE;
    }

    public static ConditionSource temperature() {
        return Temperature.INSTANCE;
    }

    public static RuleSource ifTrue(ConditionSource conditionSource, RuleSource ruleSource) {
        return new TestRuleSource(conditionSource, ruleSource);
    }

    public static RuleSource sequence(RuleSource ruleSource, RuleSource ... ruleSources) {
        return new SequenceRuleSource(Stream.concat(Stream.of(ruleSource), Arrays.stream(ruleSources)).toList());
    }

    public static RuleSource state(BlockState blockState) {
        return new BlockRuleSource(blockState);
    }

    public static RuleSource bandlands() {
        return Bandlands.INSTANCE;
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
        public static final Codec<ConditionSource> CODEC = Registry.CONDITION.dispatch(ConditionSource::codec, Function.identity());

        public static Codec<? extends ConditionSource> bootstrap() {
            Registry.register(Registry.CONDITION, "biome", BiomeConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "noise_threshold", NoiseThresholdConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "y_above", YConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "water", WaterConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "temperature", Temperature.CODEC);
            Registry.register(Registry.CONDITION, "steep", Steep.CODEC);
            Registry.register(Registry.CONDITION, "not", NotConditionSource.CODEC);
            Registry.register(Registry.CONDITION, "hole", Hole.CODEC);
            Registry.register(Registry.CONDITION, "stone_depth", StoneDepthCheck.CODEC);
            return (Codec)Registry.CONDITION.iterator().next();
        }

        public Codec<? extends ConditionSource> codec();
    }

    record YConditionSource(VerticalAnchor anchor, int runDepthMultiplier, boolean addStoneDepth) implements ConditionSource
    {
        static final Codec<YConditionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)VerticalAnchor.CODEC.fieldOf("anchor")).forGetter(YConditionSource::anchor), ((MapCodec)Codec.intRange(-20, 20).fieldOf("run_depth_multiplier")).forGetter(YConditionSource::runDepthMultiplier), ((MapCodec)Codec.BOOL.fieldOf("add_stone_depth")).forGetter(YConditionSource::addStoneDepth)).apply((Applicative<YConditionSource, ?>)instance, YConditionSource::new));

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(final Context context) {
            class YCondition
            extends EagerCondition<YConditionState> {
                YCondition() {
                }

                @Override
                protected boolean compute(YConditionState yConditionState) {
                    return yConditionState.blockY + (YConditionSource.this.addStoneDepth ? yConditionState.stoneDepthAbove : 0) >= YConditionSource.this.anchor.resolveY(context.context) + yConditionState.runDepth * YConditionSource.this.runDepthMultiplier;
                }
            }
            YCondition lv = new YCondition();
            context.yConditions.add(lv);
            return lv;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
        }
    }

    record WaterConditionSource(int offset, int runDepthMultiplier, boolean addStoneDepth) implements ConditionSource
    {
        static final Codec<WaterConditionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("offset")).forGetter(WaterConditionSource::offset), ((MapCodec)Codec.intRange(-20, 20).fieldOf("run_depth_multiplier")).forGetter(WaterConditionSource::runDepthMultiplier), ((MapCodec)Codec.BOOL.fieldOf("add_stone_depth")).forGetter(WaterConditionSource::addStoneDepth)).apply((Applicative<WaterConditionSource, ?>)instance, WaterConditionSource::new));

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            class WaterCondition
            extends EagerCondition<YConditionState> {
                WaterCondition() {
                }

                @Override
                protected boolean compute(YConditionState yConditionState) {
                    return yConditionState.waterHeight == Integer.MIN_VALUE || yConditionState.blockY + (WaterConditionSource.this.addStoneDepth ? yConditionState.stoneDepthAbove : 0) >= yConditionState.waterHeight + WaterConditionSource.this.offset + yConditionState.runDepth * WaterConditionSource.this.runDepthMultiplier;
                }
            }
            WaterCondition lv = new WaterCondition();
            context.yConditions.add(lv);
            return lv;
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
        public Condition apply(Context context) {
            final Set<ResourceKey<Biome>> set = Set.copyOf(this.biomes);
            class BiomeCondition
            extends EagerCondition<ResourceKey<Biome>> {
                BiomeCondition() {
                }

                @Override
                protected boolean compute(ResourceKey<Biome> resourceKey) {
                    return set.contains(resourceKey);
                }
            }
            BiomeCondition lv = new BiomeCondition();
            context.biomeConditions.add(lv);
            return lv;
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
        public Condition apply(Context context) {
            final NormalNoise normalNoise = context.system.getOrCreateNoise(this.noise);
            class NoiseThresholdCondition
            extends LazyCondition<NoiseThresholdConditionState> {
                NoiseThresholdCondition() {
                }

                @Override
                protected boolean compute(NoiseThresholdConditionState noiseThresholdConditionState) {
                    double d = normalNoise.getValue(noiseThresholdConditionState.blockX, 0.0, noiseThresholdConditionState.blockZ);
                    return d >= NoiseThresholdConditionSource.this.minThreshold && d <= NoiseThresholdConditionSource.this.maxThreshold;
                }
            }
            NoiseThresholdCondition lv = new NoiseThresholdCondition();
            context.noiseThresholdConditions.add(lv);
            return lv;
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
        public static final Codec<RuleSource> CODEC = Registry.RULE.dispatch(RuleSource::codec, Function.identity());

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

    record StoneDepthCheck(boolean addRunDepth, CaveSurface surfaceType) implements ConditionSource
    {
        static final Codec<StoneDepthCheck> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("add_run_depth")).forGetter(StoneDepthCheck::addRunDepth), ((MapCodec)CaveSurface.CODEC.fieldOf("surface_type")).forGetter(StoneDepthCheck::surfaceType)).apply((Applicative<StoneDepthCheck, ?>)instance, StoneDepthCheck::new));

        @Override
        public Codec<? extends ConditionSource> codec() {
            return CODEC;
        }

        @Override
        public Condition apply(Context context) {
            final boolean bl = this.surfaceType == CaveSurface.CEILING;
            class StoneDepthCondition
            extends EagerCondition<YConditionState> {
                StoneDepthCondition() {
                }

                @Override
                protected boolean compute(YConditionState yConditionState) {
                    return (bl ? yConditionState.stoneDepthBelow : yConditionState.stoneDepthAbove) <= 1 + (StoneDepthCheck.this.addRunDepth ? yConditionState.runDepth : 0);
                }
            }
            StoneDepthCondition lv = new StoneDepthCondition();
            context.yConditions.add(lv);
            if (bl) {
                context.hasCeilingRules = true;
            }
            return lv;
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((Context)object);
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

    record NoiseThresholdConditionState(int blockX, int blockZ) {
    }

    record YConditionState(int blockY, int stoneDepthAbove, int stoneDepthBelow, int runDepth, int waterHeight) {
    }

    record NotCondition(Condition target) implements Condition
    {
        @Override
        public boolean test() {
            return !this.target.test();
        }
    }

    static abstract class LazyCondition<S>
    implements UpdatableCondition<S> {
        @Nullable
        private S state;
        @Nullable
        Boolean result;

        LazyCondition() {
        }

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

        protected abstract boolean compute(S var1);
    }

    static abstract class EagerCondition<S>
    implements UpdatableCondition<S> {
        boolean state = false;

        EagerCondition() {
        }

        @Override
        public void update(S object) {
            this.state = this.compute(object);
        }

        @Override
        public boolean test() {
            return this.state;
        }

        protected abstract boolean compute(S var1);
    }

    static interface UpdatableCondition<S>
    extends Condition {
        public void update(S var1);
    }

    static interface Condition {
        public boolean test();
    }

    protected static final class Context {
        final SurfaceSystem system;
        final UpdatableCondition<TemperatureHelperCondition.State> temperature = new TemperatureHelperCondition();
        final UpdatableCondition<SteepMaterialCondition.State> steep = new SteepMaterialCondition();
        final UpdatableCondition<Integer> hole = new HoleCondition();
        final List<UpdatableCondition<ResourceKey<Biome>>> biomeConditions = new ObjectArrayList<UpdatableCondition<ResourceKey<Biome>>>();
        final List<UpdatableCondition<NoiseThresholdConditionState>> noiseThresholdConditions = new ObjectArrayList<UpdatableCondition<NoiseThresholdConditionState>>();
        final List<UpdatableCondition<YConditionState>> yConditions = new ObjectArrayList<UpdatableCondition<YConditionState>>();
        boolean hasCeilingRules;
        final WorldGenerationContext context;

        protected Context(SurfaceSystem surfaceSystem, WorldGenerationContext worldGenerationContext) {
            this.system = surfaceSystem;
            this.context = worldGenerationContext;
        }

        protected void updateXZ(ChunkAccess chunkAccess, int i, int j, int k) {
            NoiseThresholdConditionState noiseThresholdConditionState = new NoiseThresholdConditionState(i, j);
            for (UpdatableCondition<NoiseThresholdConditionState> updatableCondition : this.noiseThresholdConditions) {
                updatableCondition.update(noiseThresholdConditionState);
            }
            this.steep.update(new SteepMaterialCondition.State(chunkAccess, i, j));
            this.hole.update(k);
        }

        protected void updateY(ResourceKey<Biome> resourceKey, Biome biome, int i, int j, int k, int l, int m, int n, int o) {
            for (UpdatableCondition<ResourceKey<Biome>> updatableCondition : this.biomeConditions) {
                updatableCondition.update(resourceKey);
            }
            YConditionState yConditionState = new YConditionState(n, j, k, i, l);
            for (UpdatableCondition<YConditionState> updatableCondition2 : this.yConditions) {
                updatableCondition2.update(yConditionState);
            }
            this.temperature.update(new TemperatureHelperCondition.State(biome, m, n, o));
        }

        protected boolean hasCeilingRules() {
            return this.hasCeilingRules;
        }

        static class TemperatureHelperCondition
        extends LazyCondition<State> {
            TemperatureHelperCondition() {
            }

            @Override
            protected boolean compute(State state) {
                return state.biome.getTemperature(new BlockPos(state.blockX, state.blockY, state.blockZ)) < 0.15f;
            }

            record State(Biome biome, int blockX, int blockY, int blockZ) {
            }
        }

        static class SteepMaterialCondition
        extends LazyCondition<State> {
            SteepMaterialCondition() {
            }

            @Override
            protected boolean compute(State state) {
                int r;
                int i = state.blockX & 0xF;
                int j = state.blockZ & 0xF;
                int k = Math.max(j - 1, 0);
                int l = Math.min(j + 1, 15);
                int m = state.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, k);
                int n = state.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, l);
                if (n >= m + 4) {
                    return true;
                }
                int o = Math.max(i - 1, 0);
                int p = Math.min(i + 1, 15);
                int q = state.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, o, j);
                return q >= (r = state.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p, j)) + 4;
            }

            record State(ChunkAccess chunk, int blockX, int blockZ) {
            }
        }

        static final class HoleCondition
        extends EagerCondition<Integer> {
            HoleCondition() {
            }

            @Override
            protected boolean compute(Integer integer) {
                return integer <= 0;
            }
        }
    }
}

