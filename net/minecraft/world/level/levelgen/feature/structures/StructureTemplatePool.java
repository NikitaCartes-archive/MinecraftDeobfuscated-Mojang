/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureTemplatePool {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SIZE_UNSET = Integer.MIN_VALUE;
    public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.fieldOf("name")).forGetter(StructureTemplatePool::getName), ((MapCodec)ResourceLocation.CODEC.fieldOf("fallback")).forGetter(StructureTemplatePool::getFallback), ((MapCodec)Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.INT.fieldOf("weight")).codec().listOf().promotePartial((Consumer)Util.prefix("Pool element: ", LOGGER::error)).fieldOf("elements")).forGetter(structureTemplatePool -> structureTemplatePool.rawTemplates)).apply((Applicative<StructureTemplatePool, ?>)instance, StructureTemplatePool::new));
    public static final Codec<Supplier<StructureTemplatePool>> CODEC = RegistryFileCodec.create(Registry.TEMPLATE_POOL_REGISTRY, DIRECT_CODEC);
    private final ResourceLocation name;
    private final List<Pair<StructurePoolElement, Integer>> rawTemplates;
    private final List<StructurePoolElement> templates;
    private final ResourceLocation fallback;
    private int maxSize = Integer.MIN_VALUE;

    public StructureTemplatePool(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, List<Pair<StructurePoolElement, Integer>> list) {
        this.name = resourceLocation;
        this.rawTemplates = list;
        this.templates = Lists.newArrayList();
        for (Pair<StructurePoolElement, Integer> pair : list) {
            StructurePoolElement structurePoolElement = pair.getFirst();
            for (int i = 0; i < pair.getSecond(); ++i) {
                this.templates.add(structurePoolElement);
            }
        }
        this.fallback = resourceLocation2;
    }

    public StructureTemplatePool(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, List<Pair<Function<Projection, ? extends StructurePoolElement>, Integer>> list, Projection projection) {
        this.name = resourceLocation;
        this.rawTemplates = Lists.newArrayList();
        this.templates = Lists.newArrayList();
        for (Pair<Function<Projection, ? extends StructurePoolElement>, Integer> pair : list) {
            StructurePoolElement structurePoolElement = pair.getFirst().apply(projection);
            this.rawTemplates.add(Pair.of(structurePoolElement, pair.getSecond()));
            for (int i = 0; i < pair.getSecond(); ++i) {
                this.templates.add(structurePoolElement);
            }
        }
        this.fallback = resourceLocation2;
    }

    public int getMaxSize(StructureManager structureManager) {
        if (this.maxSize == Integer.MIN_VALUE) {
            this.maxSize = this.templates.stream().filter(structurePoolElement -> structurePoolElement != EmptyPoolElement.INSTANCE).mapToInt(structurePoolElement -> structurePoolElement.getBoundingBox(structureManager, BlockPos.ZERO, Rotation.NONE).getYSpan()).max().orElse(0);
        }
        return this.maxSize;
    }

    public ResourceLocation getFallback() {
        return this.fallback;
    }

    public StructurePoolElement getRandomTemplate(Random random) {
        return this.templates.get(random.nextInt(this.templates.size()));
    }

    public List<StructurePoolElement> getShuffledTemplates(Random random) {
        return ImmutableList.copyOf(ObjectArrays.shuffle(this.templates.toArray(new StructurePoolElement[0]), random));
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public int size() {
        return this.templates.size();
    }

    public static enum Projection implements StringRepresentable
    {
        TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
        RIGID("rigid", ImmutableList.of());

        public static final Codec<Projection> CODEC;
        private static final Map<String, Projection> BY_NAME;
        private final String name;
        private final ImmutableList<StructureProcessor> processors;

        private Projection(String string2, ImmutableList<StructureProcessor> immutableList) {
            this.name = string2;
            this.processors = immutableList;
        }

        public String getName() {
            return this.name;
        }

        public static Projection byName(String string) {
            return BY_NAME.get(string);
        }

        public ImmutableList<StructureProcessor> getProcessors() {
            return this.processors;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Projection::values, Projection::byName);
            BY_NAME = Arrays.stream(Projection.values()).collect(Collectors.toMap(Projection::getName, projection -> projection));
        }
    }
}

