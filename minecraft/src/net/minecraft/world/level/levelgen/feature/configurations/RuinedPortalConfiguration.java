package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;

public class RuinedPortalConfiguration implements FeatureConfiguration {
	public final RuinedPortalFeature.Type portalType;

	public RuinedPortalConfiguration() {
		this(RuinedPortalFeature.Type.STANDARD);
	}

	public RuinedPortalConfiguration(RuinedPortalFeature.Type type) {
		this.portalType = type;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("portal_type"), dynamicOps.createString(this.portalType.getName())))
		);
	}

	public static <T> RuinedPortalConfiguration deserialize(Dynamic<T> dynamic) {
		RuinedPortalFeature.Type type = RuinedPortalFeature.Type.byName(dynamic.get("portal_type").asString(""));
		return new RuinedPortalConfiguration(type);
	}
}
