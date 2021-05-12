package net.minecraft.world.entity.schedule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.util.Collection;
import java.util.List;

public class Timeline {
	private final List<Keyframe> keyframes = Lists.<Keyframe>newArrayList();
	private int previousIndex;

	public ImmutableList<Keyframe> getKeyframes() {
		return ImmutableList.copyOf(this.keyframes);
	}

	public Timeline addKeyframe(int i, float f) {
		this.keyframes.add(new Keyframe(i, f));
		this.sortAndDeduplicateKeyframes();
		return this;
	}

	public Timeline addKeyframes(Collection<Keyframe> collection) {
		this.keyframes.addAll(collection);
		this.sortAndDeduplicateKeyframes();
		return this;
	}

	private void sortAndDeduplicateKeyframes() {
		Int2ObjectSortedMap<Keyframe> int2ObjectSortedMap = new Int2ObjectAVLTreeMap<>();
		this.keyframes.forEach(keyframe -> int2ObjectSortedMap.put(keyframe.getTimeStamp(), keyframe));
		this.keyframes.clear();
		this.keyframes.addAll(int2ObjectSortedMap.values());
		this.previousIndex = 0;
	}

	public float getValueAt(int i) {
		if (this.keyframes.size() <= 0) {
			return 0.0F;
		} else {
			Keyframe keyframe = (Keyframe)this.keyframes.get(this.previousIndex);
			Keyframe keyframe2 = (Keyframe)this.keyframes.get(this.keyframes.size() - 1);
			boolean bl = i < keyframe.getTimeStamp();
			int j = bl ? 0 : this.previousIndex;
			float f = bl ? keyframe2.getValue() : keyframe.getValue();

			for (int k = j; k < this.keyframes.size(); k++) {
				Keyframe keyframe3 = (Keyframe)this.keyframes.get(k);
				if (keyframe3.getTimeStamp() > i) {
					break;
				}

				this.previousIndex = k;
				f = keyframe3.getValue();
			}

			return f;
		}
	}
}
