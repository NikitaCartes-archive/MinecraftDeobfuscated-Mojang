package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AnimationMetadataSection {
	public static final AnimationMetadataSectionSerializer SERIALIZER = new AnimationMetadataSectionSerializer();
	public static final String SECTION_NAME = "animation";
	public static final int DEFAULT_FRAME_TIME = 1;
	public static final int UNKNOWN_SIZE = -1;
	public static final AnimationMetadataSection EMPTY = new AnimationMetadataSection(Lists.newArrayList(), -1, -1, 1, false) {
		@Override
		public Pair<Integer, Integer> getFrameSize(int i, int j) {
			return Pair.of(i, j);
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

	private static boolean isDivisionInteger(int i, int j) {
		return i / j * j == i;
	}

	public Pair<Integer, Integer> getFrameSize(int i, int j) {
		Pair<Integer, Integer> pair = this.calculateFrameSize(i, j);
		int k = pair.getFirst();
		int l = pair.getSecond();
		if (isDivisionInteger(i, k) && isDivisionInteger(j, l)) {
			return pair;
		} else {
			throw new IllegalArgumentException(String.format("Image size %s,%s is not multiply of frame size %s,%s", i, j, k, l));
		}
	}

	private Pair<Integer, Integer> calculateFrameSize(int i, int j) {
		if (this.frameWidth != -1) {
			return this.frameHeight != -1 ? Pair.of(this.frameWidth, this.frameHeight) : Pair.of(this.frameWidth, j);
		} else if (this.frameHeight != -1) {
			return Pair.of(i, this.frameHeight);
		} else {
			int k = Math.min(i, j);
			return Pair.of(k, k);
		}
	}

	public int getFrameHeight(int i) {
		return this.frameHeight == -1 ? i : this.frameHeight;
	}

	public int getFrameWidth(int i) {
		return this.frameWidth == -1 ? i : this.frameWidth;
	}

	public int getDefaultFrameTime() {
		return this.defaultFrameTime;
	}

	public boolean isInterpolatedFrames() {
		return this.interpolatedFrames;
	}

	public void forEachFrame(AnimationMetadataSection.FrameOutput frameOutput) {
		for (AnimationFrame animationFrame : this.frames) {
			frameOutput.accept(animationFrame.getIndex(), animationFrame.getTime(this.defaultFrameTime));
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface FrameOutput {
		void accept(int i, int j);
	}
}
