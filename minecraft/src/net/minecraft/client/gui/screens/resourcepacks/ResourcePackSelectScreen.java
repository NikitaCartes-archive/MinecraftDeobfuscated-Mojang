package net.minecraft.client.gui.screens.resourcepacks;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.resourcepacks.lists.AvailableResourcePackList;
import net.minecraft.client.gui.screens.resourcepacks.lists.ResourcePackList;
import net.minecraft.client.gui.screens.resourcepacks.lists.SelectedResourcePackList;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackRepository;

@Environment(EnvType.CLIENT)
public class ResourcePackSelectScreen extends OptionsSubScreen {
	private AvailableResourcePackList availableResourcePackList;
	private SelectedResourcePackList selectedResourcePackList;
	private boolean changed;

	public ResourcePackSelectScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("resourcePack.title"));
	}

	@Override
	protected void init() {
		this.addButton(
			new Button(
				this.width / 2 - 154,
				this.height - 48,
				150,
				20,
				I18n.get("resourcePack.openFolder"),
				button -> Util.getPlatform().openFile(this.minecraft.getResourcePackDirectory())
			)
		);
		this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 20, I18n.get("gui.done"), button -> {
			if (this.changed) {
				List<UnopenedResourcePack> listx = Lists.<UnopenedResourcePack>newArrayList();

				for (ResourcePackList.ResourcePackEntry resourcePackEntry : this.selectedResourcePackList.children()) {
					listx.add(resourcePackEntry.getResourcePack());
				}

				Collections.reverse(listx);
				this.minecraft.getResourcePackRepository().setSelected(listx);
				this.options.resourcePacks.clear();
				this.options.incompatibleResourcePacks.clear();

				for (UnopenedResourcePack unopenedResourcePackx : listx) {
					if (!unopenedResourcePackx.isFixedPosition()) {
						this.options.resourcePacks.add(unopenedResourcePackx.getId());
						if (!unopenedResourcePackx.getCompatibility().isCompatible()) {
							this.options.incompatibleResourcePacks.add(unopenedResourcePackx.getId());
						}
					}
				}

				this.options.save();
				this.minecraft.setScreen(this.lastScreen);
				this.minecraft.reloadResourcePacks();
			} else {
				this.minecraft.setScreen(this.lastScreen);
			}
		}));
		AvailableResourcePackList availableResourcePackList = this.availableResourcePackList;
		SelectedResourcePackList selectedResourcePackList = this.selectedResourcePackList;
		this.availableResourcePackList = new AvailableResourcePackList(this.minecraft, 200, this.height);
		this.availableResourcePackList.setLeftPos(this.width / 2 - 4 - 200);
		if (availableResourcePackList != null) {
			this.availableResourcePackList.children().addAll(availableResourcePackList.children());
		}

		this.children.add(this.availableResourcePackList);
		this.selectedResourcePackList = new SelectedResourcePackList(this.minecraft, 200, this.height);
		this.selectedResourcePackList.setLeftPos(this.width / 2 + 4);
		if (selectedResourcePackList != null) {
			selectedResourcePackList.children().forEach(resourcePackEntry -> {
				this.selectedResourcePackList.children().add(resourcePackEntry);
				resourcePackEntry.updateParentList(this.selectedResourcePackList);
			});
		}

		this.children.add(this.selectedResourcePackList);
		if (!this.changed) {
			this.availableResourcePackList.children().clear();
			this.selectedResourcePackList.children().clear();
			PackRepository<UnopenedResourcePack> packRepository = this.minecraft.getResourcePackRepository();
			packRepository.reload();
			List<UnopenedResourcePack> list = Lists.<UnopenedResourcePack>newArrayList(packRepository.getAvailable());
			list.removeAll(packRepository.getSelected());

			for (UnopenedResourcePack unopenedResourcePack : list) {
				this.availableResourcePackList.addResourcePackEntry(new ResourcePackList.ResourcePackEntry(this.availableResourcePackList, this, unopenedResourcePack));
			}

			for (UnopenedResourcePack unopenedResourcePack : Lists.reverse(Lists.newArrayList(packRepository.getSelected()))) {
				this.selectedResourcePackList.addResourcePackEntry(new ResourcePackList.ResourcePackEntry(this.selectedResourcePackList, this, unopenedResourcePack));
			}
		}
	}

	public void select(ResourcePackList.ResourcePackEntry resourcePackEntry) {
		this.availableResourcePackList.children().remove(resourcePackEntry);
		resourcePackEntry.addToList(this.selectedResourcePackList);
		this.setChanged();
	}

	public void deselect(ResourcePackList.ResourcePackEntry resourcePackEntry) {
		this.selectedResourcePackList.children().remove(resourcePackEntry);
		this.availableResourcePackList.addResourcePackEntry(resourcePackEntry);
		this.setChanged();
	}

	public boolean isSelected(ResourcePackList.ResourcePackEntry resourcePackEntry) {
		return this.selectedResourcePackList.children().contains(resourcePackEntry);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderDirtBackground(0);
		this.availableResourcePackList.render(i, j, f);
		this.selectedResourcePackList.render(i, j, f);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 16, 16777215);
		this.drawCenteredString(this.font, I18n.get("resourcePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);
		super.render(i, j, f);
	}

	public void setChanged() {
		this.changed = true;
	}
}
