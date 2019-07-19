/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.metadata.animation;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.animation.VillagerMetadataSectionSerializer;

@Environment(value=EnvType.CLIENT)
public class VillagerMetaDataSection {
    public static final VillagerMetadataSectionSerializer SERIALIZER = new VillagerMetadataSectionSerializer();
    private final Hat hat;

    public VillagerMetaDataSection(Hat hat) {
        this.hat = hat;
    }

    public Hat getHat() {
        return this.hat;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Hat {
        NONE("none"),
        PARTIAL("partial"),
        FULL("full");

        private static final Map<String, Hat> BY_NAME;
        private final String name;

        private Hat(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        public static Hat getByName(String string) {
            return BY_NAME.getOrDefault(string, NONE);
        }

        static {
            BY_NAME = Arrays.stream(Hat.values()).collect(Collectors.toMap(Hat::getName, hat -> hat));
        }
    }
}

