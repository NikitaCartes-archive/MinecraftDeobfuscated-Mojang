/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.schedule;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import java.util.List;
import net.minecraft.world.entity.schedule.Keyframe;

public class Timeline {
    private final List<Keyframe> keyframes = Lists.newArrayList();
    private int previousIndex;

    public Timeline addKeyframe(int i, float f) {
        this.keyframes.add(new Keyframe(i, f));
        this.sortAndDeduplicateKeyframes();
        return this;
    }

    private void sortAndDeduplicateKeyframes() {
        Int2ObjectAVLTreeMap int2ObjectSortedMap = new Int2ObjectAVLTreeMap();
        this.keyframes.forEach(keyframe -> int2ObjectSortedMap.put(keyframe.getTimeStamp(), keyframe));
        this.keyframes.clear();
        this.keyframes.addAll(int2ObjectSortedMap.values());
        this.previousIndex = 0;
    }

    public float getValueAt(int i) {
        Keyframe keyframe3;
        if (this.keyframes.size() <= 0) {
            return 0.0f;
        }
        Keyframe keyframe = this.keyframes.get(this.previousIndex);
        Keyframe keyframe2 = this.keyframes.get(this.keyframes.size() - 1);
        boolean bl = i < keyframe.getTimeStamp();
        int j = bl ? 0 : this.previousIndex;
        float f = bl ? keyframe2.getValue() : keyframe.getValue();
        int k = j;
        while (k < this.keyframes.size() && (keyframe3 = this.keyframes.get(k)).getTimeStamp() <= i) {
            this.previousIndex = k++;
            f = keyframe3.getValue();
        }
        return f;
    }
}

