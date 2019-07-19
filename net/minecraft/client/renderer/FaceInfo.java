/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Direction;

@Environment(value=EnvType.CLIENT)
public enum FaceInfo {
    DOWN(new VertexInfo(Constants.MIN_X, Constants.MIN_Y, Constants.MAX_Z), new VertexInfo(Constants.MIN_X, Constants.MIN_Y, Constants.MIN_Z), new VertexInfo(Constants.MAX_X, Constants.MIN_Y, Constants.MIN_Z), new VertexInfo(Constants.MAX_X, Constants.MIN_Y, Constants.MAX_Z)),
    UP(new VertexInfo(Constants.MIN_X, Constants.MAX_Y, Constants.MIN_Z), new VertexInfo(Constants.MIN_X, Constants.MAX_Y, Constants.MAX_Z), new VertexInfo(Constants.MAX_X, Constants.MAX_Y, Constants.MAX_Z), new VertexInfo(Constants.MAX_X, Constants.MAX_Y, Constants.MIN_Z)),
    NORTH(new VertexInfo(Constants.MAX_X, Constants.MAX_Y, Constants.MIN_Z), new VertexInfo(Constants.MAX_X, Constants.MIN_Y, Constants.MIN_Z), new VertexInfo(Constants.MIN_X, Constants.MIN_Y, Constants.MIN_Z), new VertexInfo(Constants.MIN_X, Constants.MAX_Y, Constants.MIN_Z)),
    SOUTH(new VertexInfo(Constants.MIN_X, Constants.MAX_Y, Constants.MAX_Z), new VertexInfo(Constants.MIN_X, Constants.MIN_Y, Constants.MAX_Z), new VertexInfo(Constants.MAX_X, Constants.MIN_Y, Constants.MAX_Z), new VertexInfo(Constants.MAX_X, Constants.MAX_Y, Constants.MAX_Z)),
    WEST(new VertexInfo(Constants.MIN_X, Constants.MAX_Y, Constants.MIN_Z), new VertexInfo(Constants.MIN_X, Constants.MIN_Y, Constants.MIN_Z), new VertexInfo(Constants.MIN_X, Constants.MIN_Y, Constants.MAX_Z), new VertexInfo(Constants.MIN_X, Constants.MAX_Y, Constants.MAX_Z)),
    EAST(new VertexInfo(Constants.MAX_X, Constants.MAX_Y, Constants.MAX_Z), new VertexInfo(Constants.MAX_X, Constants.MIN_Y, Constants.MAX_Z), new VertexInfo(Constants.MAX_X, Constants.MIN_Y, Constants.MIN_Z), new VertexInfo(Constants.MAX_X, Constants.MAX_Y, Constants.MIN_Z));

    private static final FaceInfo[] BY_FACING;
    private final VertexInfo[] infos;

    public static FaceInfo fromFacing(Direction direction) {
        return BY_FACING[direction.get3DDataValue()];
    }

    private FaceInfo(VertexInfo ... vertexInfos) {
        this.infos = vertexInfos;
    }

    public VertexInfo getVertexInfo(int i) {
        return this.infos[i];
    }

    static {
        BY_FACING = Util.make(new FaceInfo[6], faceInfos -> {
            faceInfos[Constants.MIN_Y] = DOWN;
            faceInfos[Constants.MAX_Y] = UP;
            faceInfos[Constants.MIN_Z] = NORTH;
            faceInfos[Constants.MAX_Z] = SOUTH;
            faceInfos[Constants.MIN_X] = WEST;
            faceInfos[Constants.MAX_X] = EAST;
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static class VertexInfo {
        public final int xFace;
        public final int yFace;
        public final int zFace;

        private VertexInfo(int i, int j, int k) {
            this.xFace = i;
            this.yFace = j;
            this.zFace = k;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Constants {
        public static final int MAX_Z = Direction.SOUTH.get3DDataValue();
        public static final int MAX_Y = Direction.UP.get3DDataValue();
        public static final int MAX_X = Direction.EAST.get3DDataValue();
        public static final int MIN_Z = Direction.NORTH.get3DDataValue();
        public static final int MIN_Y = Direction.DOWN.get3DDataValue();
        public static final int MIN_X = Direction.WEST.get3DDataValue();
    }
}

