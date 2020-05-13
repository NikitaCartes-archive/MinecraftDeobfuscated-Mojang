package net.minecraft.world.level.dimension;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.io.File;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.dimension.end.TheEndDimension;

public class DimensionType implements Serializable {
	public static final DimensionType OVERWORLD = register(
		"overworld", new DimensionType(1, "", "", NormalDimension::new, true, false, false, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE)
	);
	public static final DimensionType NETHER = register(
		"the_nether", new DimensionType(0, "_nether", "DIM-1", NetherDimension::new, false, true, true, FuzzyOffsetBiomeZoomer.INSTANCE)
	);
	public static final DimensionType THE_END = register(
		"the_end", new DimensionType(2, "_end", "DIM1", TheEndDimension::new, false, false, false, FuzzyOffsetBiomeZoomer.INSTANCE)
	);
	private final int id;
	private final String fileSuffix;
	private final String folder;
	private final BiFunction<Level, DimensionType, ? extends Dimension> factory;
	private final boolean hasSkylight;
	private final boolean hasCeiling;
	private final boolean ultraWarm;
	private final BiomeZoomer biomeZoomer;

	private static DimensionType register(String string, DimensionType dimensionType) {
		return Registry.registerMapping(Registry.DIMENSION_TYPE, dimensionType.id, string, dimensionType);
	}

	protected DimensionType(
		int i,
		String string,
		String string2,
		BiFunction<Level, DimensionType, ? extends Dimension> biFunction,
		boolean bl,
		boolean bl2,
		boolean bl3,
		BiomeZoomer biomeZoomer
	) {
		this.id = i;
		this.fileSuffix = string;
		this.folder = string2;
		this.factory = biFunction;
		this.hasSkylight = bl;
		this.hasCeiling = bl2;
		this.ultraWarm = bl3;
		this.biomeZoomer = biomeZoomer;
	}

	public static DimensionType of(Dynamic<?> dynamic) {
		return Registry.DIMENSION_TYPE.get(new ResourceLocation(dynamic.asString("")));
	}

	public static Iterable<DimensionType> getAllTypes() {
		return Registry.DIMENSION_TYPE;
	}

	public int getId() {
		return this.id + -1;
	}

	public String getFileSuffix() {
		return this.fileSuffix;
	}

	public File getStorageFolder(File file) {
		return this.folder.isEmpty() ? file : new File(file, this.folder);
	}

	public Dimension create(Level level) {
		return (Dimension)this.factory.apply(level, this);
	}

	public String toString() {
		return getName(this).toString();
	}

	@Nullable
	public static DimensionType getById(int i) {
		return Registry.DIMENSION_TYPE.byId(i - -1);
	}

	@Nullable
	public static DimensionType getByName(ResourceLocation resourceLocation) {
		return Registry.DIMENSION_TYPE.get(resourceLocation);
	}

	@Nullable
	public static ResourceLocation getName(DimensionType dimensionType) {
		return Registry.DIMENSION_TYPE.getKey(dimensionType);
	}

	public boolean hasSkyLight() {
		return this.hasSkylight;
	}

	public boolean hasCeiling() {
		return this.hasCeiling;
	}

	public boolean ultraWarm() {
		return this.ultraWarm;
	}

	public BiomeZoomer getBiomeZoomer() {
		return this.biomeZoomer;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return dynamicOps.createString(Registry.DIMENSION_TYPE.getKey(this).toString());
	}
}
