package net.minecraft.client.gui.screens;

import java.io.File;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

@Environment(EnvType.CLIENT)
public class DataPackSelectScreen extends PackSelectionScreen {
	private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");

	public DataPackSelectScreen(Screen screen, PackRepository<Pack> packRepository, Consumer<PackRepository<Pack>> consumer, File file) {
		super(
			screen,
			new TranslatableComponent("dataPack.title"),
			runnable -> new PackSelectionModel<>(runnable, (pack, textureManager) -> textureManager.bind(DEFAULT_ICON), packRepository, consumer),
			file
		);
	}
}
