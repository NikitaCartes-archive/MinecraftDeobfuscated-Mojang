/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.animation;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationChannel;
import org.apache.commons.compress.utils.Lists;

@Environment(value=EnvType.CLIENT)
public record AnimationDefinition(float lengthInSeconds, boolean looping, Map<String, List<AnimationChannel>> boneAnimations) {

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final float length;
        private final Map<String, List<AnimationChannel>> animationByBone = Maps.newHashMap();
        private boolean looping;

        public static Builder withLength(float f) {
            return new Builder(f);
        }

        private Builder(float f) {
            this.length = f;
        }

        public Builder looping() {
            this.looping = true;
            return this;
        }

        public Builder addAnimation(String string2, AnimationChannel animationChannel) {
            this.animationByBone.computeIfAbsent(string2, string -> Lists.newArrayList()).add(animationChannel);
            return this;
        }

        public AnimationDefinition build() {
            return new AnimationDefinition(this.length, this.looping, this.animationByBone);
        }
    }
}

