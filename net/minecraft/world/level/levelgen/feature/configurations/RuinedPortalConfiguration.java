/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class RuinedPortalConfiguration
implements FeatureConfiguration {
    public static final Codec<RuinedPortalConfiguration> CODEC = ((MapCodec)RuinedPortalFeature.Type.CODEC.fieldOf("portal_type")).xmap(RuinedPortalConfiguration::new, ruinedPortalConfiguration -> ruinedPortalConfiguration.portalType).codec();
    public final RuinedPortalFeature.Type portalType;

    public RuinedPortalConfiguration(RuinedPortalFeature.Type type) {
        this.portalType = type;
    }
}

