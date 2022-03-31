/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.biome.MobSpawnSettings;

public record StructureSpawnOverride(BoundingBoxType boundingBox, WeightedRandomList<MobSpawnSettings.SpawnerData> spawns) {
    public static final Codec<StructureSpawnOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BoundingBoxType.CODEC.fieldOf("bounding_box")).forGetter(StructureSpawnOverride::boundingBox), ((MapCodec)WeightedRandomList.codec(MobSpawnSettings.SpawnerData.CODEC).fieldOf("spawns")).forGetter(StructureSpawnOverride::spawns)).apply((Applicative<StructureSpawnOverride, ?>)instance, StructureSpawnOverride::new));

    public static enum BoundingBoxType implements StringRepresentable
    {
        PIECE("piece"),
        STRUCTURE("full");

        public static final Codec<BoundingBoxType> CODEC;
        private final String id;

        private BoundingBoxType(String string2) {
            this.id = string2;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(BoundingBoxType::values);
        }
    }
}

