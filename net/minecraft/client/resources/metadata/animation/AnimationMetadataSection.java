/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSectionSerializer;
import net.minecraft.client.resources.metadata.animation.FrameSize;

@Environment(value=EnvType.CLIENT)
public class AnimationMetadataSection {
    public static final AnimationMetadataSectionSerializer SERIALIZER = new AnimationMetadataSectionSerializer();
    public static final String SECTION_NAME = "animation";
    public static final int DEFAULT_FRAME_TIME = 1;
    public static final int UNKNOWN_SIZE = -1;
    public static final AnimationMetadataSection EMPTY = new AnimationMetadataSection((List)Lists.newArrayList(), -1, -1, 1, false){

        @Override
        public FrameSize calculateFrameSize(int i, int j) {
            return new FrameSize(i, j);
        }
    };
    private final List<AnimationFrame> frames;
    private final int frameWidth;
    private final int frameHeight;
    private final int defaultFrameTime;
    private final boolean interpolatedFrames;

    public AnimationMetadataSection(List<AnimationFrame> list, int i, int j, int k, boolean bl) {
        this.frames = list;
        this.frameWidth = i;
        this.frameHeight = j;
        this.defaultFrameTime = k;
        this.interpolatedFrames = bl;
    }

    public FrameSize calculateFrameSize(int i, int j) {
        if (this.frameWidth != -1) {
            if (this.frameHeight != -1) {
                return new FrameSize(this.frameWidth, this.frameHeight);
            }
            return new FrameSize(this.frameWidth, j);
        }
        if (this.frameHeight != -1) {
            return new FrameSize(i, this.frameHeight);
        }
        int k = Math.min(i, j);
        return new FrameSize(k, k);
    }

    public int getDefaultFrameTime() {
        return this.defaultFrameTime;
    }

    public boolean isInterpolatedFrames() {
        return this.interpolatedFrames;
    }

    public void forEachFrame(FrameOutput frameOutput) {
        for (AnimationFrame animationFrame : this.frames) {
            frameOutput.accept(animationFrame.getIndex(), animationFrame.getTime(this.defaultFrameTime));
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface FrameOutput {
        public void accept(int var1, int var2);
    }
}

