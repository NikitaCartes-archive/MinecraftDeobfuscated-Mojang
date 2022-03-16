/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.OceanRuinPieces;
import org.jetbrains.annotations.Nullable;

public class OceanRuinStructure
extends Structure {
    public static final Codec<OceanRuinStructure> CODEC = RecordCodecBuilder.create(instance -> OceanRuinStructure.codec(instance).and(instance.group(((MapCodec)Type.CODEC.fieldOf("biome_temp")).forGetter(oceanRuinStructure -> oceanRuinStructure.biomeTemp), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("large_probability")).forGetter(oceanRuinStructure -> Float.valueOf(oceanRuinStructure.largeProbability)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("cluster_probability")).forGetter(oceanRuinStructure -> Float.valueOf(oceanRuinStructure.clusterProbability)))).apply((Applicative<OceanRuinStructure, ?>)instance, OceanRuinStructure::new));
    public final Type biomeTemp;
    public final float largeProbability;
    public final float clusterProbability;

    public OceanRuinStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl, Type type, float f, float g) {
        super(holderSet, map, decoration, bl);
        this.biomeTemp = type;
        this.largeProbability = f;
        this.clusterProbability = g;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        return OceanRuinStructure.onTopOfChunkCenter(generationContext, Heightmap.Types.OCEAN_FLOOR_WG, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, generationContext));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        BlockPos blockPos = new BlockPos(generationContext.chunkPos().getMinBlockX(), 90, generationContext.chunkPos().getMinBlockZ());
        Rotation rotation = Rotation.getRandom(generationContext.random());
        OceanRuinPieces.addPieces(generationContext.structureTemplateManager(), blockPos, rotation, structurePiecesBuilder, generationContext.random(), this);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.OCEAN_RUIN;
    }

    public static enum Type implements StringRepresentable
    {
        WARM("warm"),
        COLD("cold");

        public static final Codec<Type> CODEC;
        private static final Map<String, Type> BY_NAME;
        private final String name;

        private Type(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static Type byName(String string) {
            return BY_NAME.get(string);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Type::values, Type::byName);
            BY_NAME = Arrays.stream(Type.values()).collect(Collectors.toMap(Type::getName, type -> type));
        }
    }
}

