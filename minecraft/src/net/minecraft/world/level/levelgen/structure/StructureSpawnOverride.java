package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.biome.MobSpawnSettings;

public record StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType boundingBox, WeightedRandomList<MobSpawnSettings.SpawnerData> spawns) {
	public static final Codec<StructureSpawnOverride> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					StructureSpawnOverride.BoundingBoxType.CODEC.fieldOf("bounding_box").forGetter(StructureSpawnOverride::boundingBox),
					WeightedRandomList.codec(MobSpawnSettings.SpawnerData.CODEC).fieldOf("spawns").forGetter(StructureSpawnOverride::spawns)
				)
				.apply(instance, StructureSpawnOverride::new)
	);

	public static enum BoundingBoxType implements StringRepresentable {
		PIECE("piece"),
		STRUCTURE("full");

		public static final StructureSpawnOverride.BoundingBoxType[] VALUES = values();
		public static final Codec<StructureSpawnOverride.BoundingBoxType> CODEC = StringRepresentable.fromEnum(
			() -> VALUES, StructureSpawnOverride.BoundingBoxType::byName
		);
		private final String id;

		private BoundingBoxType(String string2) {
			this.id = string2;
		}

		@Override
		public String getSerializedName() {
			return this.id;
		}

		@Nullable
		public static StructureSpawnOverride.BoundingBoxType byName(@Nullable String string) {
			if (string == null) {
				return null;
			} else {
				for (StructureSpawnOverride.BoundingBoxType boundingBoxType : VALUES) {
					if (boundingBoxType.id.equals(string)) {
						return boundingBoxType;
					}
				}

				return null;
			}
		}
	}
}
