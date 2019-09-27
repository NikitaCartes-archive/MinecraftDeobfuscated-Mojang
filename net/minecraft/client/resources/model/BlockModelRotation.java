/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public enum BlockModelRotation implements ModelState
{
    X0_Y0(0, 0),
    X0_Y90(0, 90),
    X0_Y180(0, 180),
    X0_Y270(0, 270),
    X90_Y0(90, 0),
    X90_Y90(90, 90),
    X90_Y180(90, 180),
    X90_Y270(90, 270),
    X180_Y0(180, 0),
    X180_Y90(180, 90),
    X180_Y180(180, 180),
    X180_Y270(180, 270),
    X270_Y0(270, 0),
    X270_Y90(270, 90),
    X270_Y180(270, 180),
    X270_Y270(270, 270);

    private static final Map<Integer, BlockModelRotation> BY_INDEX;
    private final int index;
    private final Quaternion rotation;
    private final int xSteps;
    private final int ySteps;

    private static int getIndex(int i, int j) {
        return i * 360 + j;
    }

    private BlockModelRotation(int j, int k) {
        this.index = BlockModelRotation.getIndex(j, k);
        Quaternion quaternion = new Quaternion(new Vector3f(0.0f, 1.0f, 0.0f), -k, true);
        quaternion.mul(new Quaternion(new Vector3f(1.0f, 0.0f, 0.0f), -j, true));
        this.rotation = quaternion;
        this.xSteps = Mth.abs(j / 90);
        this.ySteps = Mth.abs(k / 90);
    }

    @Override
    public Transformation getRotation() {
        return new Transformation(null, this.rotation, null, null);
    }

    public static BlockModelRotation by(int i, int j) {
        return BY_INDEX.get(BlockModelRotation.getIndex(Mth.positiveModulo(i, 360), Mth.positiveModulo(j, 360)));
    }

    static {
        BY_INDEX = Arrays.stream(BlockModelRotation.values()).collect(Collectors.toMap(blockModelRotation -> blockModelRotation.index, blockModelRotation -> blockModelRotation));
    }
}

