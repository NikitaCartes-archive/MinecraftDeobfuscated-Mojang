package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.flag.FeatureFlagSet;

@Environment(EnvType.CLIENT)
public record CommonListenerCookie(
	GameProfile localGameProfile,
	WorldSessionTelemetryManager telemetryManager,
	RegistryAccess.Frozen receivedRegistries,
	FeatureFlagSet enabledFeatures,
	@Nullable String serverBrand,
	@Nullable ServerData serverData,
	@Nullable Screen postDisconnectScreen
) {
}
