package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.DataPackConfig;

@Environment(EnvType.CLIENT)
public class DataPackSelectScreen extends PackSelectionScreen {
	private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");

	public DataPackSelectScreen(Screen screen, DataPackConfig dataPackConfig, BiConsumer<DataPackConfig, PackRepository<Pack>> biConsumer, File file) {
		super(
			screen,
			new TranslatableComponent("dataPack.title"),
			new Function<Runnable, PackSelectionModel<?>>() {
				private DataPackConfig workingConfig = dataPackConfig;
				private final PackRepository<Pack> repository = new PackRepository<>(
					Pack::new, new ServerPacksSource(), new FolderRepositorySource(file, PackSource.DEFAULT)
				);

				public PackSelectionModel<?> apply(Runnable runnable) {
					this.repository.reload();
					List<String> list = this.workingConfig.getEnabled();
					List<Pack> list2 = DataPackSelectScreen.getPacksByName(this.repository, list.stream());
					List<Pack> list3 = DataPackSelectScreen.getPacksByName(
						this.repository, this.repository.getAvailableIds().stream().filter(string -> !list.contains(string))
					);
					return new PackSelectionModel(
						runnable, (pack, textureManager) -> textureManager.bind(DataPackSelectScreen.DEFAULT_ICON), Lists.reverse(list2), list3, (listx, list2x, bl) -> {
							List<String> list3x = (List<String>)Lists.reverse(listx).stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
							List<String> list4 = (List<String>)list2x.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
							this.workingConfig = new DataPackConfig(list3x, list4);
							if (!bl) {
								this.repository.setSelected(list3x);
								biConsumer.accept(this.workingConfig, this.repository);
							}
						}
					);
				}
			},
			minecraft -> file
		);
	}

	private static List<Pack> getPacksByName(PackRepository<Pack> packRepository, Stream<String> stream) {
		return (List<Pack>)stream.map(packRepository::getPack).filter(Objects::nonNull).collect(ImmutableList.toImmutableList());
	}
}
