package net.minecraft.client.gui.screens.worldselection;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;

@Environment(EnvType.CLIENT)
public record WorldCreationContext(
	WorldOptions options,
	Registry<LevelStem> datapackDimensions,
	WorldDimensions selectedDimensions,
	LayeredRegistryAccess<RegistryLayer> worldgenRegistries,
	ReloadableServerResources dataPackResources,
	WorldDataConfiguration dataConfiguration
) {
	public WorldCreationContext(
		WorldGenSettings worldGenSettings,
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
		ReloadableServerResources reloadableServerResources,
		WorldDataConfiguration worldDataConfiguration
	) {
		this(worldGenSettings.options(), worldGenSettings.dimensions(), layeredRegistryAccess, reloadableServerResources, worldDataConfiguration);
	}

	public WorldCreationContext(
		WorldOptions worldOptions,
		WorldDimensions worldDimensions,
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
		ReloadableServerResources reloadableServerResources,
		WorldDataConfiguration worldDataConfiguration
	) {
		this(
			worldOptions,
			layeredRegistryAccess.getLayer(RegistryLayer.DIMENSIONS).registryOrThrow(Registry.LEVEL_STEM_REGISTRY),
			worldDimensions,
			layeredRegistryAccess.replaceFrom(RegistryLayer.DIMENSIONS),
			reloadableServerResources,
			worldDataConfiguration
		);
	}

	public WorldCreationContext withSettings(WorldOptions worldOptions, WorldDimensions worldDimensions) {
		return new WorldCreationContext(
			worldOptions, this.datapackDimensions, worldDimensions, this.worldgenRegistries, this.dataPackResources, this.dataConfiguration
		);
	}

	public WorldCreationContext withOptions(WorldCreationContext.OptionsModifier optionsModifier) {
		return new WorldCreationContext(
			(WorldOptions)optionsModifier.apply(this.options),
			this.datapackDimensions,
			this.selectedDimensions,
			this.worldgenRegistries,
			this.dataPackResources,
			this.dataConfiguration
		);
	}

	public WorldCreationContext withDimensions(WorldCreationContext.DimensionsUpdater dimensionsUpdater) {
		return new WorldCreationContext(
			this.options,
			this.datapackDimensions,
			(WorldDimensions)dimensionsUpdater.apply(this.worldgenLoadContext(), this.selectedDimensions),
			this.worldgenRegistries,
			this.dataPackResources,
			this.dataConfiguration
		);
	}

	public RegistryAccess.Frozen worldgenLoadContext() {
		return this.worldgenRegistries.compositeAccess();
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface DimensionsUpdater extends BiFunction<RegistryAccess.Frozen, WorldDimensions, WorldDimensions> {
	}

	@Environment(EnvType.CLIENT)
	public interface OptionsModifier extends UnaryOperator<WorldOptions> {
	}
}
