package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class PackSelectionScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Component DRAG_AND_DROP = new TranslatableComponent("pack.dropInfo").withStyle(ChatFormatting.DARK_GRAY);
	private static final Component DIRECTORY_BUTTON_TOOLTIP = new TranslatableComponent("pack.folderInfo");
	private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
	private final PackSelectionModel model;
	private final Screen lastScreen;
	@Nullable
	private PackSelectionScreen.Watcher watcher;
	private long ticksToReload;
	private TransferableSelectionList availablePackList;
	private TransferableSelectionList selectedPackList;
	private final File packDir;
	private Button doneButton;
	private final Map<String, ResourceLocation> packIcons = Maps.<String, ResourceLocation>newHashMap();

	public PackSelectionScreen(Screen screen, PackRepository packRepository, Consumer<PackRepository> consumer, File file, Component component) {
		super(component);
		this.lastScreen = screen;
		this.model = new PackSelectionModel(this::populateLists, this::getPackIcon, packRepository, consumer);
		this.packDir = file;
		this.watcher = PackSelectionScreen.Watcher.create(file);
	}

	@Override
	public void onClose() {
		this.model.commit();
		this.minecraft.setScreen(this.lastScreen);
		this.closeWatcher();
	}

	private void closeWatcher() {
		if (this.watcher != null) {
			try {
				this.watcher.close();
				this.watcher = null;
			} catch (Exception var2) {
			}
		}
	}

	@Override
	protected void init() {
		this.doneButton = this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 20, CommonComponents.GUI_DONE, button -> this.onClose()));
		this.addButton(
			new Button(
				this.width / 2 - 154,
				this.height - 48,
				150,
				20,
				new TranslatableComponent("pack.openFolder"),
				button -> Util.getPlatform().openFile(this.packDir),
				(button, poseStack, i, j) -> this.renderTooltip(poseStack, DIRECTORY_BUTTON_TOOLTIP, i, j)
			)
		);
		this.availablePackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.available.title"));
		this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
		this.children.add(this.availablePackList);
		this.selectedPackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.selected.title"));
		this.selectedPackList.setLeftPos(this.width / 2 + 4);
		this.children.add(this.selectedPackList);
		this.reload();
	}

	@Override
	public void tick() {
		if (this.watcher != null) {
			try {
				if (this.watcher.pollForChanges()) {
					this.ticksToReload = 20L;
				}
			} catch (IOException var2) {
				LOGGER.warn("Failed to poll for directory {} changes, stopping", this.packDir);
				this.closeWatcher();
			}
		}

		if (this.ticksToReload > 0L && --this.ticksToReload == 0L) {
			this.reload();
		}
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

	private void reload() {
		this.model.findNewPacks();
		this.populateLists();
		this.ticksToReload = 0L;
		this.packIcons.clear();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(0);
		this.availablePackList.render(poseStack, i, j, f);
		this.selectedPackList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
		drawCenteredString(poseStack, this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
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
				copyPacks(this.minecraft, list, this.packDir.toPath());
				this.reload();
			}

			this.minecraft.setScreen(this);
		}, new TranslatableComponent("pack.dropConfirm"), new TextComponent(string)));
	}

	private ResourceLocation loadPackIcon(TextureManager textureManager, Pack pack) {
		try (PackResources packResources = pack.open()) {
			InputStream inputStream = packResources.getRootResource("pack.png");
			Throwable var6 = null;

			ResourceLocation var10;
			try {
				String string = pack.getId();
				ResourceLocation resourceLocation = new ResourceLocation(
					"minecraft", "pack/" + Util.sanitizeName(string, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon"
				);
				NativeImage nativeImage = NativeImage.read(inputStream);
				textureManager.register(resourceLocation, new DynamicTexture(nativeImage));
				var10 = resourceLocation;
			} catch (Throwable var37) {
				var6 = var37;
				throw var37;
			} finally {
				if (inputStream != null) {
					if (var6 != null) {
						try {
							inputStream.close();
						} catch (Throwable var36) {
							var6.addSuppressed(var36);
						}
					} else {
						inputStream.close();
					}
				}
			}

			return var10;
		} catch (FileNotFoundException var41) {
		} catch (Exception var42) {
			LOGGER.warn("Failed to load icon from pack {}", pack.getId(), var42);
		}

		return DEFAULT_ICON;
	}

	private ResourceLocation getPackIcon(Pack pack) {
		return (ResourceLocation)this.packIcons.computeIfAbsent(pack.getId(), string -> this.loadPackIcon(this.minecraft.getTextureManager(), pack));
	}

	@Environment(EnvType.CLIENT)
	static class Watcher implements AutoCloseable {
		private final WatchService watcher;
		private final Path packPath;

		public Watcher(File file) throws IOException {
			this.packPath = file.toPath();
			this.watcher = this.packPath.getFileSystem().newWatchService();

			try {
				this.watchDir(this.packPath);
				DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.packPath);
				Throwable var3 = null;

				try {
					for (Path path : directoryStream) {
						if (Files.isDirectory(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
							this.watchDir(path);
						}
					}
				} catch (Throwable var14) {
					var3 = var14;
					throw var14;
				} finally {
					if (directoryStream != null) {
						if (var3 != null) {
							try {
								directoryStream.close();
							} catch (Throwable var13) {
								var3.addSuppressed(var13);
							}
						} else {
							directoryStream.close();
						}
					}
				}
			} catch (Exception var16) {
				this.watcher.close();
				throw var16;
			}
		}

		@Nullable
		public static PackSelectionScreen.Watcher create(File file) {
			try {
				return new PackSelectionScreen.Watcher(file);
			} catch (IOException var2) {
				PackSelectionScreen.LOGGER.warn("Failed to initialize pack directory {} monitoring", file, var2);
				return null;
			}
		}

		private void watchDir(Path path) throws IOException {
			path.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		}

		public boolean pollForChanges() throws IOException {
			boolean bl = false;

			WatchKey watchKey;
			while ((watchKey = this.watcher.poll()) != null) {
				for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
					bl = true;
					if (watchKey.watchable() == this.packPath && watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						Path path = this.packPath.resolve((Path)watchEvent.context());
						if (Files.isDirectory(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
							this.watchDir(path);
						}
					}
				}

				watchKey.reset();
			}

			return bl;
		}

		public void close() throws IOException {
			this.watcher.close();
		}
	}
}
