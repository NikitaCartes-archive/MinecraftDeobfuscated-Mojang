package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;

public class GenerationStep {
	public static enum Carving implements StringRepresentable {
		AIR("air"),
		LIQUID("liquid");

		public static final Codec<GenerationStep.Carving> CODEC = StringRepresentable.fromEnum(GenerationStep.Carving::values, GenerationStep.Carving::byName);
		private static final Map<String, GenerationStep.Carving> BY_NAME = (Map<String, GenerationStep.Carving>)Arrays.stream(values())
			.collect(Collectors.toMap(GenerationStep.Carving::getName, carving -> carving));
		private final String name;

		private Carving(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		@Nullable
		public static GenerationStep.Carving byName(String string) {
			return (GenerationStep.Carving)BY_NAME.get(string);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public static enum Decoration {
		RAW_GENERATION,
		LAKES,
		LOCAL_MODIFICATIONS,
		UNDERGROUND_STRUCTURES,
		SURFACE_STRUCTURES,
		STRONGHOLDS,
		UNDERGROUND_ORES,
		UNDERGROUND_DECORATION,
		VEGETAL_DECORATION,
		TOP_LAYER_MODIFICATION;
	}
}
