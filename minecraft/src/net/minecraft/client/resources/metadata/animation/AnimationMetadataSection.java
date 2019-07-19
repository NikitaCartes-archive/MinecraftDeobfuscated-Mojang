package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AnimationMetadataSection {
	public static final AnimationMetadataSectionSerializer SERIALIZER = new AnimationMetadataSectionSerializer();
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

	public int getFrameHeight() {
		return this.frameHeight;
	}

	public int getFrameWidth() {
		return this.frameWidth;
	}

	public int getFrameCount() {
		return this.frames.size();
	}

	public int getDefaultFrameTime() {
		return this.defaultFrameTime;
	}

	public boolean isInterpolatedFrames() {
		return this.interpolatedFrames;
	}

	private AnimationFrame getFrame(int i) {
		return (AnimationFrame)this.frames.get(i);
	}

	public int getFrameTime(int i) {
		AnimationFrame animationFrame = this.getFrame(i);
		return animationFrame.isTimeUnknown() ? this.defaultFrameTime : animationFrame.getTime();
	}

	public int getFrameIndex(int i) {
		return ((AnimationFrame)this.frames.get(i)).getIndex();
	}

	public Set<Integer> getUniqueFrameIndices() {
		Set<Integer> set = Sets.<Integer>newHashSet();

		for (AnimationFrame animationFrame : this.frames) {
			set.add(animationFrame.getIndex());
		}

		return set;
	}
}
