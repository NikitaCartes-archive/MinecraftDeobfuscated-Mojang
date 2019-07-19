/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.io.File;
import java.util.function.BiFunction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.NetherDimension;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.dimension.end.TheEndDimension;
import org.jetbrains.annotations.Nullable;

public class DimensionType
implements Serializable {
    public static final DimensionType OVERWORLD = DimensionType.register("overworld", new DimensionType(1, "", "", NormalDimension::new, true));
    public static final DimensionType NETHER = DimensionType.register("the_nether", new DimensionType(0, "_nether", "DIM-1", NetherDimension::new, false));
    public static final DimensionType THE_END = DimensionType.register("the_end", new DimensionType(2, "_end", "DIM1", TheEndDimension::new, false));
    private final int id;
    private final String fileSuffix;
    private final String folder;
    private final BiFunction<Level, DimensionType, ? extends Dimension> factory;
    private final boolean hasSkylight;

    private static DimensionType register(String string, DimensionType dimensionType) {
        return Registry.registerMapping(Registry.DIMENSION_TYPE, dimensionType.id, string, dimensionType);
    }

    protected DimensionType(int i, String string, String string2, BiFunction<Level, DimensionType, ? extends Dimension> biFunction, boolean bl) {
        this.id = i;
        this.fileSuffix = string;
        this.folder = string2;
        this.factory = biFunction;
        this.hasSkylight = bl;
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
        if (this.folder.isEmpty()) {
            return file;
        }
        return new File(file, this.folder);
    }

    public Dimension create(Level level) {
        return this.factory.apply(level, this);
    }

    public String toString() {
        return DimensionType.getName(this).toString();
    }

    @Nullable
    public static DimensionType getById(int i) {
        return (DimensionType)Registry.DIMENSION_TYPE.byId(i - -1);
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

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        return dynamicOps.createString(Registry.DIMENSION_TYPE.getKey(this).toString());
    }
}

