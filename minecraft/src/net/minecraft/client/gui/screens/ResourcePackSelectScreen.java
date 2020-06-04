package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.resources.ResourcePack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

@Environment(EnvType.CLIENT)
public class ResourcePackSelectScreen extends PackSelectionScreen {
	public ResourcePackSelectScreen(Screen screen, Options options, PackRepository<ResourcePack> packRepository, Runnable runnable) {
		super(screen, new TranslatableComponent("resourcePack.title"), runnable2 -> {
			packRepository.reload();
			List<ResourcePack> list = Lists.<ResourcePack>newArrayList(packRepository.getSelectedPacks());
			List<ResourcePack> list2 = Lists.<ResourcePack>newArrayList(packRepository.getAvailablePacks());
			list2.removeAll(list);
			return new PackSelectionModel(runnable2, ResourcePack::bindIcon, Lists.reverse(list), list2, (listx, list2x, bl) -> {
				List<ResourcePack> list3 = Lists.reverse(listx);
				List<String> list4 = (List<String>)list3.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
				packRepository.setSelected(list4);
				options.resourcePacks.clear();
				options.incompatibleResourcePacks.clear();

				for (ResourcePack resourcePack : list3) {
					if (!resourcePack.isFixedPosition()) {
						options.resourcePacks.add(resourcePack.getId());
						if (!resourcePack.getCompatibility().isCompatible()) {
							options.incompatibleResourcePacks.add(resourcePack.getId());
						}
					}
				}

				options.save();
				if (!bl) {
					runnable.run();
				}
			});
		}, Minecraft::getResourcePackDirectory);
	}
}
