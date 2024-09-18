package net.minecraft.client.gui.screens.worldselection;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.storage.PrimaryLevelData;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface CreateWorldCallback {
	boolean create(
		CreateWorldScreen createWorldScreen, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PrimaryLevelData primaryLevelData, @Nullable Path path
	);
}
