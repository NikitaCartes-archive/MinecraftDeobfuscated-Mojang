/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public class DataPackConfig {
    public static final DataPackConfig DEFAULT = new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of());
    public static final Codec<DataPackConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.listOf().fieldOf("Enabled")).forGetter(dataPackConfig -> dataPackConfig.enabled), ((MapCodec)Codec.STRING.listOf().fieldOf("Disabled")).forGetter(dataPackConfig -> dataPackConfig.disabled)).apply((Applicative<DataPackConfig, ?>)instance, DataPackConfig::new));
    private final List<String> enabled;
    private final List<String> disabled;

    public DataPackConfig(List<String> list, List<String> list2) {
        this.enabled = ImmutableList.copyOf(list);
        this.disabled = ImmutableList.copyOf(list2);
    }

    public List<String> getEnabled() {
        return this.enabled;
    }

    public List<String> getDisabled() {
        return this.disabled;
    }
}

