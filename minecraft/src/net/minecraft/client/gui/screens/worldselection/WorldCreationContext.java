package net.minecraft.client.gui.screens.worldselection;

import com.mojang.serialization.Lifecycle;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.levelgen.WorldGenSettings;

@Environment(EnvType.CLIENT)
public record WorldCreationContext(
	WorldGenSettings worldGenSettings, Lifecycle worldSettingsStability, RegistryAccess.Frozen registryAccess, ReloadableServerResources dataPackResources
) {
	public WorldCreationContext withSettings(WorldGenSettings worldGenSettings) {
		return new WorldCreationContext(worldGenSettings, this.worldSettingsStability, this.registryAccess, this.dataPackResources);
	}

	public WorldCreationContext withSettings(WorldCreationContext.SimpleUpdater simpleUpdater) {
		WorldGenSettings worldGenSettings = (WorldGenSettings)simpleUpdater.apply(this.worldGenSettings);
		return this.withSettings(worldGenSettings);
	}

	public WorldCreationContext withSettings(WorldCreationContext.Updater updater) {
		WorldGenSettings worldGenSettings = (WorldGenSettings)updater.apply(this.registryAccess, this.worldGenSettings);
		return this.withSettings(worldGenSettings);
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface SimpleUpdater extends UnaryOperator<WorldGenSettings> {
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Updater extends BiFunction<RegistryAccess.Frozen, WorldGenSettings, WorldGenSettings> {
	}
}
