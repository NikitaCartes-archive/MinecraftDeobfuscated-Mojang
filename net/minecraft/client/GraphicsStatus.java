/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

@Environment(value=EnvType.CLIENT)
public enum GraphicsStatus implements OptionEnum
{
    FAST(0, "options.graphics.fast"),
    FANCY(1, "options.graphics.fancy"),
    FABULOUS(2, "options.graphics.fabulous");

    private static final IntFunction<GraphicsStatus> BY_ID;
    private final int id;
    private final String key;

    private GraphicsStatus(int j, String string2) {
        this.id = j;
        this.key = string2;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public String toString() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case FAST -> "fast";
            case FANCY -> "fancy";
            case FABULOUS -> "fabulous";
        };
    }

    public static GraphicsStatus byId(int i) {
        return BY_ID.apply(i);
    }

    static {
        BY_ID = ByIdMap.continuous(GraphicsStatus::getId, GraphicsStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

