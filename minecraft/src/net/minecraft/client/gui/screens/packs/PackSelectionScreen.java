package net.minecraft.client.gui.screens.packs;

import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class PackSelectionScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Component DRAG_AND_DROP = new TranslatableComponent("pack.dropInfo").withStyle(ChatFormatting.DARK_GRAY);
	private static final Component DIRECTORY_BUTTON_TOOLTIP = new TranslatableComponent("pack.folderInfo");
	private final Function<Runnable, PackSelectionModel<?>> modelSupplier;
	private PackSelectionModel<?> model;
	private final Screen lastScreen;
	private boolean hasChanges;
	private boolean shouldCommit;
	private TransferableSelectionList availablePackList;
	private TransferableSelectionList selectedPackList;
	private final Function<Minecraft, File> packDir;
	private Button doneButton;

	public PackSelectionScreen(
		Screen screen, TranslatableComponent translatableComponent, Function<Runnable, PackSelectionModel<?>> function, Function<Minecraft, File> function2
	) {
		super(translatableComponent);
		this.lastScreen = screen;
		this.modelSupplier = function;
		this.model = (PackSelectionModel<?>)function.apply(this::onListsChanged);
		this.packDir = function2;
	}

	@Override
	public void removed() {
		if (this.shouldCommit) {
			this.shouldCommit = false;
			this.model.commit(false);
		}
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	protected void init() {
		this.doneButton = this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 20, CommonComponents.GUI_DONE, button -> {
			this.shouldCommit = this.hasChanges;
			this.onClose();
		}));
		this.addButton(
			new Button(
				this.width / 2 - 154,
				this.height - 48,
				150,
				20,
				new TranslatableComponent("pack.openFolder"),
				button -> Util.getPlatform().openFile((File)this.packDir.apply(this.minecraft)),
				(button, poseStack, i, j) -> this.renderTooltip(poseStack, DIRECTORY_BUTTON_TOOLTIP, i, j)
			)
		);
		this.availablePackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.available.title"));
		this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
		this.children.add(this.availablePackList);
		this.selectedPackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.selected.title"));
		this.selectedPackList.setLeftPos(this.width / 2 + 4);
		this.children.add(this.selectedPackList);
		this.populateLists();
	}

	private void populateLists() {
		this.updateList(this.selectedPackList, this.model.getSelected());
		this.updateList(this.availablePackList, this.model.getUnselected());
		this.doneButton.active = !this.selectedPackList.children().isEmpty();
	}

	private void updateList(TransferableSelectionList transferableSelectionList, Stream<PackSelectionModel.Entry> stream) {
		transferableSelectionList.children().clear();
		stream.forEach(
			entry -> transferableSelectionList.children().add(new TransferableSelectionList.PackEntry(this.minecraft, transferableSelectionList, this, entry))
		);
	}

	protected void onListsChanged() {
		this.populateLists();
		this.hasChanges = true;
	}

	protected void reload() {
		this.model.commit(true);
		this.model = (PackSelectionModel<?>)this.modelSupplier.apply(this::onListsChanged);
		this.populateLists();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(0);
		this.availablePackList.render(poseStack, i, j, f);
		this.selectedPackList.render(poseStack, i, j, f);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
		this.drawCenteredString(poseStack, this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
		super.render(poseStack, i, j, f);
	}

	protected static void copyPacks(Minecraft minecraft, List<Path> list, Path path) {
		MutableBoolean mutableBoolean = new MutableBoolean();
		list.forEach(path2 -> {
			try {
				Stream<Path> stream = Files.walk(path2);
				Throwable var4 = null;

				try {
					stream.forEach(path3 -> {
						try {
							Util.copyBetweenDirs(path2.getParent(), path, path3);
						} catch (IOException var5) {
							LOGGER.warn("Failed to copy datapack file  from {} to {}", path3, path, var5);
							mutableBoolean.setTrue();
						}
					});
				} catch (Throwable var14) {
					var4 = var14;
					throw var14;
				} finally {
					if (stream != null) {
						if (var4 != null) {
							try {
								stream.close();
							} catch (Throwable var13) {
								var4.addSuppressed(var13);
							}
						} else {
							stream.close();
						}
					}
				}
			} catch (IOException var16) {
				LOGGER.warn("Failed to copy datapack file from {} to {}", path2, path);
				mutableBoolean.setTrue();
			}
		});
		if (mutableBoolean.isTrue()) {
			SystemToast.onPackCopyFailure(minecraft, path.toString());
		}
	}

	@Override
	public void onFilesDrop(List<Path> list) {
		String string = (String)list.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
		this.minecraft.setScreen(new ConfirmScreen(bl -> {
			if (bl) {
				copyPacks(this.minecraft, list, ((File)this.packDir.apply(this.minecraft)).toPath());
				this.reload();
			}

			this.minecraft.setScreen(this);
		}, new TranslatableComponent("pack.dropConfirm"), new TextComponent(string)));
	}
}
