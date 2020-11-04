/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanRuinPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.jetbrains.annotations.Nullable;

public class OceanRuinFeature
extends StructureFeature<OceanRuinConfiguration> {
    public OceanRuinFeature(Codec<OceanRuinConfiguration> codec) {
        super(codec);
    }

    @Override
    public StructureFeature.StructureStartFactory<OceanRuinConfiguration> getStartFactory() {
        return OceanRuinStart::new;
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

    public static class OceanRuinStart
    extends StructureStart<OceanRuinConfiguration> {
        public OceanRuinStart(StructureFeature<OceanRuinConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
            super(structureFeature, i, j, boundingBox, k, l);
        }

        @Override
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, OceanRuinConfiguration oceanRuinConfiguration) {
            int k = SectionPos.sectionToBlockCoord(i);
            int l = SectionPos.sectionToBlockCoord(j);
            BlockPos blockPos = new BlockPos(k, 90, l);
            Rotation rotation = Rotation.getRandom(this.random);
            OceanRuinPieces.addPieces(structureManager, blockPos, rotation, this.pieces, this.random, oceanRuinConfiguration);
            this.calculateBoundingBox();
        }
    }
}

