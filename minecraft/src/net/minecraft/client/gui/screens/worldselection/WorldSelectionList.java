package net.minecraft.client.gui.screens.worldselection;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldSelectionList extends ObjectSelectionList<WorldSelectionList.WorldListEntry> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
	private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
	private final SelectWorldScreen screen;
	@Nullable
	private List<LevelSummary> cachedList;

	public WorldSelectionList(
		SelectWorldScreen selectWorldScreen,
		Minecraft minecraft,
		int i,
		int j,
		int k,
		int l,
		int m,
		Supplier<String> supplier,
		@Nullable WorldSelectionList worldSelectionList
	) {
		super(minecraft, i, j, k, l, m);
		this.screen = selectWorldScreen;
		if (worldSelectionList != null) {
			this.cachedList = worldSelectionList.cachedList;
		}

		this.refreshList(supplier, false);
	}

	public void refreshList(Supplier<String> supplier, boolean bl) {
		this.clearEntries();
		LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
		if (this.cachedList == null || bl) {
			try {
				this.cachedList = levelStorageSource.getLevelList();
			} catch (LevelStorageException var7) {
				LOGGER.error("Couldn't load level list", (Throwable)var7);
				this.minecraft.setScreen(new ErrorScreen(new TranslatableComponent("selectWorld.unable_to_load"), var7.getMessage()));
				return;
			}

			Collections.sort(this.cachedList);
		}

		String string = ((String)supplier.get()).toLowerCase(Locale.ROOT);

		for (LevelSummary levelSummary : this.cachedList) {
			if (levelSummary.getLevelName().toLowerCase(Locale.ROOT).contains(string) || levelSummary.getLevelId().toLowerCase(Locale.ROOT).contains(string)) {
				this.addEntry(new WorldSelectionList.WorldListEntry(this, levelSummary, this.minecraft.getLevelSource()));
			}
		}
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 20;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	protected boolean isFocused() {
		return this.screen.getFocused() == this;
	}

	public void setSelected(@Nullable WorldSelectionList.WorldListEntry worldListEntry) {
		super.setSelected(worldListEntry);
		if (worldListEntry != null) {
			LevelSummary levelSummary = worldListEntry.summary;
			NarratorChatListener.INSTANCE
				.sayNow(
					new TranslatableComponent(
							"narrator.select",
							new TranslatableComponent(
								"narrator.select.world",
								levelSummary.getLevelName(),
								new Date(levelSummary.getLastPlayed()),
								levelSummary.isHardcore() ? I18n.get("gameMode.hardcore") : I18n.get("gameMode." + levelSummary.getGameMode().getName()),
								levelSummary.hasCheats() ? I18n.get("selectWorld.cheats") : "",
								levelSummary.getWorldVersionName()
							)
						)
						.getString()
				);
		}
	}

	@Override
	protected void moveSelection(int i) {
		super.moveSelection(i);
		this.screen.updateButtonStatus(true);
	}

	public Optional<WorldSelectionList.WorldListEntry> getSelectedOpt() {
		return Optional.ofNullable(this.getSelected());
	}

	public SelectWorldScreen getScreen() {
		return this.screen;
	}

	@Environment(EnvType.CLIENT)
	public final class WorldListEntry extends ObjectSelectionList.Entry<WorldSelectionList.WorldListEntry> implements AutoCloseable {
		private final Minecraft minecraft;
		private final SelectWorldScreen screen;
		private final LevelSummary summary;
		private final ResourceLocation iconLocation;
		private File iconFile;
		@Nullable
		private final DynamicTexture icon;
		private long lastClickTime;

		public WorldListEntry(WorldSelectionList worldSelectionList2, LevelSummary levelSummary, LevelStorageSource levelStorageSource) {
			this.screen = worldSelectionList2.getScreen();
			this.summary = levelSummary;
			this.minecraft = Minecraft.getInstance();
			this.iconLocation = new ResourceLocation("worlds/" + Hashing.sha1().hashUnencodedChars(levelSummary.getLevelId()) + "/icon");
			this.iconFile = levelSummary.getIcon();
			if (!this.iconFile.isFile()) {
				this.iconFile = null;
			}

			this.icon = this.loadServerIcon();
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			String string = this.summary.getLevelName();
			String string2 = this.summary.getLevelId() + " (" + WorldSelectionList.DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
			if (StringUtils.isEmpty(string)) {
				string = I18n.get("selectWorld.world") + " " + (i + 1);
			}

			String string3;
			if (this.summary.isLocked()) {
				string3 = ChatFormatting.DARK_RED + I18n.get("selectWorld.locked") + ChatFormatting.RESET;
			} else if (this.summary.isRequiresConversion()) {
				string3 = I18n.get("selectWorld.conversion");
			} else {
				if (this.summary.isHardcore()) {
					string3 = ChatFormatting.DARK_RED + I18n.get("gameMode.hardcore") + ChatFormatting.RESET;
				} else {
					string3 = I18n.get("gameMode." + this.summary.getGameMode().getName());
				}

				if (this.summary.hasCheats()) {
					string3 = string3 + ", " + I18n.get("selectWorld.cheats");
				}

				String string4 = this.summary.getWorldVersionName().getColoredString();
				if (this.summary.markVersionInList()) {
					if (this.summary.askToOpenWorld()) {
						string3 = string3 + ", " + I18n.get("selectWorld.version") + " " + ChatFormatting.RED + string4 + ChatFormatting.RESET;
					} else {
						string3 = string3 + ", " + I18n.get("selectWorld.version") + " " + ChatFormatting.ITALIC + string4 + ChatFormatting.RESET;
					}
				} else {
					string3 = string3 + ", " + I18n.get("selectWorld.version") + " " + string4;
				}
			}

			this.minecraft.font.draw(string, (float)(k + 32 + 3), (float)(j + 1), 16777215);
			this.minecraft.font.draw(string2, (float)(k + 32 + 3), (float)(j + 9 + 3), 8421504);
			this.minecraft.font.draw(string3, (float)(k + 32 + 3), (float)(j + 9 + 9 + 3), 8421504);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getTextureManager().bind(this.icon != null ? this.iconLocation : WorldSelectionList.ICON_MISSING);
			RenderSystem.enableBlend();
			GuiComponent.blit(k, j, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
			if (this.minecraft.options.touchscreen || bl) {
				this.minecraft.getTextureManager().bind(WorldSelectionList.ICON_OVERLAY_LOCATION);
				GuiComponent.fill(k, j, k + 32, j + 32, -1601138544);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				int p = n - k;
				boolean bl2 = p < 32;
				int q = bl2 ? 32 : 0;
				if (this.summary.isLocked()) {
					GuiComponent.blit(k, j, 96.0F, (float)q, 32, 32, 256, 256);
					if (bl2) {
						Component component = new TranslatableComponent("selectWorld.locked").withStyle(ChatFormatting.RED);
						this.screen.setToolTip(this.minecraft.font.insertLineBreaks(component.getColoredString(), 175));
					}
				} else if (this.summary.markVersionInList()) {
					GuiComponent.blit(k, j, 32.0F, (float)q, 32, 32, 256, 256);
					if (this.summary.isOldCustomizedWorld()) {
						GuiComponent.blit(k, j, 96.0F, (float)q, 32, 32, 256, 256);
						if (bl2) {
							Component component = new TranslatableComponent("selectWorld.tooltip.unsupported", this.summary.getWorldVersionName()).withStyle(ChatFormatting.RED);
							this.screen.setToolTip(this.minecraft.font.insertLineBreaks(component.getColoredString(), 175));
						}
					} else if (this.summary.askToOpenWorld()) {
						GuiComponent.blit(k, j, 96.0F, (float)q, 32, 32, 256, 256);
						if (bl2) {
							this.screen
								.setToolTip(
									ChatFormatting.RED + I18n.get("selectWorld.tooltip.fromNewerVersion1") + "\n" + ChatFormatting.RED + I18n.get("selectWorld.tooltip.fromNewerVersion2")
								);
						}
					} else if (!SharedConstants.getCurrentVersion().isStable()) {
						GuiComponent.blit(k, j, 64.0F, (float)q, 32, 32, 256, 256);
						if (bl2) {
							this.screen
								.setToolTip(ChatFormatting.GOLD + I18n.get("selectWorld.tooltip.snapshot1") + "\n" + ChatFormatting.GOLD + I18n.get("selectWorld.tooltip.snapshot2"));
						}
					}
				} else {
					GuiComponent.blit(k, j, 0.0F, (float)q, 32, 32, 256, 256);
				}
			}
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.summary.isLocked()) {
				return true;
			} else {
				WorldSelectionList.this.setSelected(this);
				this.screen.updateButtonStatus(WorldSelectionList.this.getSelectedOpt().isPresent());
				if (d - (double)WorldSelectionList.this.getRowLeft() <= 32.0) {
					this.joinWorld();
					return true;
				} else if (Util.getMillis() - this.lastClickTime < 250L) {
					this.joinWorld();
					return true;
				} else {
					this.lastClickTime = Util.getMillis();
					return false;
				}
			}
		}

		public void joinWorld() {
			if (!this.summary.isLocked()) {
				if (this.summary.shouldBackup() || this.summary.isOldCustomizedWorld()) {
					Component component = new TranslatableComponent("selectWorld.backupQuestion");
					Component component2 = new TranslatableComponent(
						"selectWorld.backupWarning", this.summary.getWorldVersionName().getColoredString(), SharedConstants.getCurrentVersion().getName()
					);
					if (this.summary.isOldCustomizedWorld()) {
						component = new TranslatableComponent("selectWorld.backupQuestion.customized");
						component2 = new TranslatableComponent("selectWorld.backupWarning.customized");
					}

					this.minecraft.setScreen(new BackupConfirmScreen(this.screen, (bl, bl2) -> {
						if (bl) {
							String string = this.summary.getLevelId();

							try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(string)) {
								EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
							} catch (IOException var17) {
								SystemToast.onWorldAccessFailure(this.minecraft, string);
								WorldSelectionList.LOGGER.error("Failed to backup level {}", string, var17);
							}
						}

						this.loadWorld();
					}, component, component2, false));
				} else if (this.summary.askToOpenWorld()) {
					this.minecraft
						.setScreen(
							new ConfirmScreen(
								bl -> {
									if (bl) {
										try {
											this.loadWorld();
										} catch (Exception var3) {
											WorldSelectionList.LOGGER.error("Failure to open 'future world'", (Throwable)var3);
											this.minecraft
												.setScreen(
													new AlertScreen(
														() -> this.minecraft.setScreen(this.screen),
														new TranslatableComponent("selectWorld.futureworld.error.title"),
														new TranslatableComponent("selectWorld.futureworld.error.text")
													)
												);
										}
									} else {
										this.minecraft.setScreen(this.screen);
									}
								},
								new TranslatableComponent("selectWorld.versionQuestion"),
								new TranslatableComponent("selectWorld.versionWarning", this.summary.getWorldVersionName().getColoredString()),
								I18n.get("selectWorld.versionJoinButton"),
								I18n.get("gui.cancel")
							)
						);
				} else {
					this.loadWorld();
				}
			}
		}

		public void deleteWorld() {
			this.minecraft
				.setScreen(
					new ConfirmScreen(
						bl -> {
							if (bl) {
								this.minecraft.setScreen(new ProgressScreen());
								LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
								String string = this.summary.getLevelId();

								try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string)) {
									levelStorageAccess.deleteLevel();
								} catch (IOException var17) {
									SystemToast.onWorldDeleteFailure(this.minecraft, string);
									WorldSelectionList.LOGGER.error("Failed to delete world {}", string, var17);
								}

								WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
							}

							this.minecraft.setScreen(this.screen);
						},
						new TranslatableComponent("selectWorld.deleteQuestion"),
						new TranslatableComponent("selectWorld.deleteWarning", this.summary.getLevelName()),
						I18n.get("selectWorld.deleteButton"),
						I18n.get("gui.cancel")
					)
				);
		}

		public void editWorld() {
			String string = this.summary.getLevelId();

			try {
				LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(string);
				this.minecraft.setScreen(new EditWorldScreen(bl -> {
					try {
						levelStorageAccess.close();
					} catch (IOException var5) {
						WorldSelectionList.LOGGER.error("Failed to unlock level {}", string, var5);
					}

					if (bl) {
						WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
					}

					this.minecraft.setScreen(this.screen);
				}, levelStorageAccess));
			} catch (IOException var3) {
				SystemToast.onWorldAccessFailure(this.minecraft, string);
				WorldSelectionList.LOGGER.error("Failed to access level {}", string, var3);
				WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
			}
		}

		public void recreateWorld() {
			try {
				this.minecraft.setScreen(new ProgressScreen());
				CreateWorldScreen createWorldScreen = new CreateWorldScreen(this.screen);

				try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(this.summary.getLevelId())) {
					LevelData levelData = levelStorageAccess.selectLevel(null).prepareLevel();
					if (levelData != null) {
						createWorldScreen.copyFromWorld(levelData);
						if (this.summary.isOldCustomizedWorld()) {
							this.minecraft
								.setScreen(
									new ConfirmScreen(
										bl -> this.minecraft.setScreen((Screen)(bl ? createWorldScreen : this.screen)),
										new TranslatableComponent("selectWorld.recreate.customized.title"),
										new TranslatableComponent("selectWorld.recreate.customized.text"),
										I18n.get("gui.proceed"),
										I18n.get("gui.cancel")
									)
								);
						} else {
							this.minecraft.setScreen(createWorldScreen);
						}
					}
				}
			} catch (Exception var15) {
				WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)var15);
				this.minecraft
					.setScreen(
						new AlertScreen(
							() -> this.minecraft.setScreen(this.screen),
							new TranslatableComponent("selectWorld.recreate.error.title"),
							new TranslatableComponent("selectWorld.recreate.error.text")
						)
					);
			}
		}

		private void loadWorld() {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			if (this.minecraft.getLevelSource().levelExists(this.summary.getLevelId())) {
				this.minecraft.selectLevel(this.summary.getLevelId(), this.summary.getLevelName(), null);
			}
		}

		@Nullable
		private DynamicTexture loadServerIcon() {
			boolean bl = this.iconFile != null && this.iconFile.isFile();
			if (bl) {
				try {
					InputStream inputStream = new FileInputStream(this.iconFile);
					Throwable var3 = null;

					DynamicTexture var6;
					try {
						NativeImage nativeImage = NativeImage.read(inputStream);
						Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
						Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high");
						DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
						this.minecraft.getTextureManager().register(this.iconLocation, dynamicTexture);
						var6 = dynamicTexture;
					} catch (Throwable var16) {
						var3 = var16;
						throw var16;
					} finally {
						if (inputStream != null) {
							if (var3 != null) {
								try {
									inputStream.close();
								} catch (Throwable var15) {
									var3.addSuppressed(var15);
								}
							} else {
								inputStream.close();
							}
						}
					}

					return var6;
				} catch (Throwable var18) {
					WorldSelectionList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), var18);
					this.iconFile = null;
					return null;
				}
			} else {
				this.minecraft.getTextureManager().release(this.iconLocation);
				return null;
			}
		}

		public void close() {
			if (this.icon != null) {
				this.icon.close();
			}
		}
	}
}
