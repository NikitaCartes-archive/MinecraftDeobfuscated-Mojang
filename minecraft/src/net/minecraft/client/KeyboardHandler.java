package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class KeyboardHandler {
	public static final int DEBUG_CRASH_TIME = 10000;
	private final Minecraft minecraft;
	private final ClipboardManager clipboardManager = new ClipboardManager();
	private long debugCrashKeyTime = -1L;
	private long debugCrashKeyReportedTime = -1L;
	private long debugCrashKeyReportedCount = -1L;
	private boolean handledDebugKey;

	public KeyboardHandler(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	private boolean handleChunkDebugKeys(int i) {
		switch (i) {
			case 69:
				this.minecraft.sectionPath = !this.minecraft.sectionPath;
				this.debugFeedback("SectionPath: {0}", this.minecraft.sectionPath ? "shown" : "hidden");
				return true;
			case 70:
			case 71:
			case 72:
			case 73:
			case 74:
			case 75:
			case 77:
			case 78:
			case 80:
			case 81:
			case 82:
			case 83:
			case 84:
			default:
				return false;
			case 76:
				this.minecraft.smartCull = !this.minecraft.smartCull;
				this.debugFeedback("SmartCull: {0}", this.minecraft.smartCull ? "enabled" : "disabled");
				return true;
			case 79:
				boolean bl = this.minecraft.debugRenderer.toggleRenderOctree();
				this.debugFeedback("Frustum culling Octree: {0}", bl ? "enabled" : "disabled");
				return true;
			case 85:
				if (Screen.hasShiftDown()) {
					this.minecraft.levelRenderer.killFrustum();
					this.debugFeedback("Killed frustum");
				} else {
					this.minecraft.levelRenderer.captureFrustum();
					this.debugFeedback("Captured frustum");
				}

				return true;
			case 86:
				this.minecraft.sectionVisibility = !this.minecraft.sectionVisibility;
				this.debugFeedback("SectionVisibility: {0}", this.minecraft.sectionVisibility ? "enabled" : "disabled");
				return true;
			case 87:
				this.minecraft.wireframe = !this.minecraft.wireframe;
				this.debugFeedback("WireFrame: {0}", this.minecraft.wireframe ? "enabled" : "disabled");
				return true;
		}
	}

	private void debugComponent(ChatFormatting chatFormatting, Component component) {
		this.minecraft
			.gui
			.getChat()
			.addMessage(
				Component.empty()
					.append(Component.translatable("debug.prefix").withStyle(chatFormatting, ChatFormatting.BOLD))
					.append(CommonComponents.SPACE)
					.append(component)
			);
	}

	private void debugFeedbackComponent(Component component) {
		this.debugComponent(ChatFormatting.YELLOW, component);
	}

	private void debugFeedbackTranslated(String string, Object... objects) {
		this.debugFeedbackComponent(Component.translatableEscape(string, objects));
	}

	private void debugWarningTranslated(String string, Object... objects) {
		this.debugComponent(ChatFormatting.RED, Component.translatableEscape(string, objects));
	}

	private void debugFeedback(String string, Object... objects) {
		this.debugFeedbackComponent(Component.literal(MessageFormat.format(string, objects)));
	}

	private boolean handleDebugKeys(int i) {
		if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
			return true;
		} else {
			switch (i) {
				case 49:
					this.minecraft.getDebugOverlay().toggleProfilerChart();
					return true;
				case 50:
					this.minecraft.getDebugOverlay().toggleFpsCharts();
					return true;
				case 51:
					this.minecraft.getDebugOverlay().toggleNetworkCharts();
					return true;
				case 65:
					this.minecraft.levelRenderer.allChanged();
					this.debugFeedbackTranslated("debug.reload_chunks.message");
					return true;
				case 66:
					boolean bl = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
					this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(bl);
					this.debugFeedbackTranslated(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
					return true;
				case 67:
					if (this.minecraft.player.isReducedDebugInfo()) {
						return false;
					} else {
						ClientPacketListener clientPacketListener = this.minecraft.player.connection;
						if (clientPacketListener == null) {
							return false;
						}

						this.debugFeedbackTranslated("debug.copy_location.message");
						this.setClipboard(
							String.format(
								Locale.ROOT,
								"/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f",
								this.minecraft.player.level().dimension().location(),
								this.minecraft.player.getX(),
								this.minecraft.player.getY(),
								this.minecraft.player.getZ(),
								this.minecraft.player.getYRot(),
								this.minecraft.player.getXRot()
							)
						);
						return true;
					}
				case 68:
					if (this.minecraft.gui != null) {
						this.minecraft.gui.getChat().clearMessages(false);
					}

					return true;
				case 71:
					boolean bl2 = this.minecraft.debugRenderer.switchRenderChunkborder();
					this.debugFeedbackTranslated(bl2 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
					return true;
				case 72:
					this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
					this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
					this.minecraft.options.save();
					return true;
				case 73:
					if (!this.minecraft.player.isReducedDebugInfo()) {
						this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
					}

					return true;
				case 76:
					if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent)) {
						this.debugFeedbackTranslated("debug.profiling.start", 10);
					}

					return true;
				case 78:
					if (!this.minecraft.player.hasPermissions(2)) {
						this.debugFeedbackTranslated("debug.creative_spectator.error");
					} else if (!this.minecraft.player.isSpectator()) {
						this.minecraft.player.connection.sendUnsignedCommand("gamemode spectator");
					} else {
						this.minecraft
							.player
							.connection
							.sendUnsignedCommand("gamemode " + MoreObjects.firstNonNull(this.minecraft.gameMode.getPreviousPlayerMode(), GameType.CREATIVE).getName());
					}

					return true;
				case 80:
					this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
					this.minecraft.options.save();
					this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
					return true;
				case 81:
					this.debugFeedbackTranslated("debug.help.message");
					ChatComponent chatComponent = this.minecraft.gui.getChat();
					chatComponent.addMessage(Component.translatable("debug.reload_chunks.help"));
					chatComponent.addMessage(Component.translatable("debug.show_hitboxes.help"));
					chatComponent.addMessage(Component.translatable("debug.copy_location.help"));
					chatComponent.addMessage(Component.translatable("debug.clear_chat.help"));
					chatComponent.addMessage(Component.translatable("debug.chunk_boundaries.help"));
					chatComponent.addMessage(Component.translatable("debug.advanced_tooltips.help"));
					chatComponent.addMessage(Component.translatable("debug.inspect.help"));
					chatComponent.addMessage(Component.translatable("debug.profiling.help"));
					chatComponent.addMessage(Component.translatable("debug.creative_spectator.help"));
					chatComponent.addMessage(Component.translatable("debug.pause_focus.help"));
					chatComponent.addMessage(Component.translatable("debug.help.help"));
					chatComponent.addMessage(Component.translatable("debug.dump_dynamic_textures.help"));
					chatComponent.addMessage(Component.translatable("debug.reload_resourcepacks.help"));
					chatComponent.addMessage(Component.translatable("debug.pause.help"));
					chatComponent.addMessage(Component.translatable("debug.gamemodes.help"));
					return true;
				case 83:
					Path path = this.minecraft.gameDirectory.toPath().toAbsolutePath();
					Path path2 = TextureUtil.getDebugTexturePath(path);
					this.minecraft.getTextureManager().dumpAllSheets(path2);
					Component component = Component.literal(path.relativize(path2).toString())
						.withStyle(ChatFormatting.UNDERLINE)
						.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path2.toFile().toString())));
					this.debugFeedbackTranslated("debug.dump_dynamic_textures", component);
					return true;
				case 84:
					this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
					this.minecraft.reloadResourcePacks();
					return true;
				case 293:
					if (!this.minecraft.player.hasPermissions(2)) {
						this.debugFeedbackTranslated("debug.gamemodes.error");
					} else {
						this.minecraft.setScreen(new GameModeSwitcherScreen());
					}

					return true;
				default:
					return false;
			}
		}
	}

	private void copyRecreateCommand(boolean bl, boolean bl2) {
		HitResult hitResult = this.minecraft.hitResult;
		if (hitResult != null) {
			switch (hitResult.getType()) {
				case BLOCK:
					BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
					Level level = this.minecraft.player.level();
					BlockState blockState = level.getBlockState(blockPos);
					if (bl) {
						if (bl2) {
							this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(blockPos, compoundTagx -> {
								this.copyCreateBlockCommand(blockState, blockPos, compoundTagx);
								this.debugFeedbackTranslated("debug.inspect.server.block");
							});
						} else {
							BlockEntity blockEntity = level.getBlockEntity(blockPos);
							CompoundTag compoundTag = blockEntity != null ? blockEntity.saveWithoutMetadata(level.registryAccess()) : null;
							this.copyCreateBlockCommand(blockState, blockPos, compoundTag);
							this.debugFeedbackTranslated("debug.inspect.client.block");
						}
					} else {
						this.copyCreateBlockCommand(blockState, blockPos, null);
						this.debugFeedbackTranslated("debug.inspect.client.block");
					}
					break;
				case ENTITY:
					Entity entity = ((EntityHitResult)hitResult).getEntity();
					ResourceLocation resourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
					if (bl) {
						if (bl2) {
							this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), compoundTagx -> {
								this.copyCreateEntityCommand(resourceLocation, entity.position(), compoundTagx);
								this.debugFeedbackTranslated("debug.inspect.server.entity");
							});
						} else {
							CompoundTag compoundTag2 = entity.saveWithoutId(new CompoundTag());
							this.copyCreateEntityCommand(resourceLocation, entity.position(), compoundTag2);
							this.debugFeedbackTranslated("debug.inspect.client.entity");
						}
					} else {
						this.copyCreateEntityCommand(resourceLocation, entity.position(), null);
						this.debugFeedbackTranslated("debug.inspect.client.entity");
					}
			}
		}
	}

	private void copyCreateBlockCommand(BlockState blockState, BlockPos blockPos, @Nullable CompoundTag compoundTag) {
		StringBuilder stringBuilder = new StringBuilder(BlockStateParser.serialize(blockState));
		if (compoundTag != null) {
			stringBuilder.append(compoundTag);
		}

		String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", blockPos.getX(), blockPos.getY(), blockPos.getZ(), stringBuilder);
		this.setClipboard(string);
	}

	private void copyCreateEntityCommand(ResourceLocation resourceLocation, Vec3 vec3, @Nullable CompoundTag compoundTag) {
		String string2;
		if (compoundTag != null) {
			compoundTag.remove("UUID");
			compoundTag.remove("Pos");
			compoundTag.remove("Dimension");
			String string = NbtUtils.toPrettyComponent(compoundTag).getString();
			string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", resourceLocation, vec3.x, vec3.y, vec3.z, string);
		} else {
			string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", resourceLocation, vec3.x, vec3.y, vec3.z);
		}

		this.setClipboard(string2);
	}

	public void keyPress(long l, int i, int j, int k, int m) {
		if (l == this.minecraft.getWindow().getWindow()) {
			this.minecraft.getFramerateLimitTracker().onInputReceived();
			boolean bl = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
			if (this.debugCrashKeyTime > 0L) {
				if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !bl) {
					this.debugCrashKeyTime = -1L;
				}
			} else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && bl) {
				this.handledDebugKey = true;
				this.debugCrashKeyTime = Util.getMillis();
				this.debugCrashKeyReportedTime = Util.getMillis();
				this.debugCrashKeyReportedCount = 0L;
			}

			Screen screen = this.minecraft.screen;
			if (screen != null) {
				switch (i) {
					case 258:
						this.minecraft.setLastInputType(InputType.KEYBOARD_TAB);
					case 259:
					case 260:
					case 261:
					default:
						break;
					case 262:
					case 263:
					case 264:
					case 265:
						this.minecraft.setLastInputType(InputType.KEYBOARD_ARROW);
				}
			}

			if (k == 1 && (!(this.minecraft.screen instanceof KeyBindsScreen) || ((KeyBindsScreen)screen).lastKeySelection <= Util.getMillis() - 20L)) {
				if (this.minecraft.options.keyFullscreen.matches(i, j)) {
					this.minecraft.getWindow().toggleFullScreen();
					this.minecraft.options.fullscreen().set(this.minecraft.getWindow().isFullscreen());
					return;
				}

				if (this.minecraft.options.keyScreenshot.matches(i, j)) {
					if (Screen.hasControlDown()) {
					}

					Screenshot.grab(
						this.minecraft.gameDirectory,
						this.minecraft.getMainRenderTarget(),
						component -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(component))
					);
					return;
				}
			}

			if (k != 0) {
				boolean bl2 = screen == null || !(screen.getFocused() instanceof EditBox) || !((EditBox)screen.getFocused()).canConsumeInput();
				if (bl2) {
					if (Screen.hasControlDown() && i == 66 && this.minecraft.getNarrator().isActive() && this.minecraft.options.narratorHotkey().get()) {
						boolean bl3 = this.minecraft.options.narrator().get() == NarratorStatus.OFF;
						this.minecraft.options.narrator().set(NarratorStatus.byId(this.minecraft.options.narrator().get().getId() + 1));
						this.minecraft.options.save();
						if (screen != null) {
							screen.updateNarratorStatus(bl3);
						}
					}

					LocalPlayer var16 = this.minecraft.player;
				}
			}

			if (screen != null) {
				boolean[] bls = new boolean[]{false};
				Screen.wrapScreenError(() -> {
					if (k == 1 || k == 2) {
						screen.afterKeyboardAction();
						bls[0] = screen.keyPressed(i, j, m);
					} else if (k == 0) {
						bls[0] = screen.keyReleased(i, j, m);
					}
				}, "keyPressed event handler", screen.getClass().getCanonicalName());
				if (bls[0]) {
					return;
				}
			}

			InputConstants.Key key;
			boolean bl3;
			boolean var10000;
			label180: {
				key = InputConstants.getKey(i, j);
				bl3 = this.minecraft.screen == null;
				label141:
				if (!bl3) {
					if (this.minecraft.screen instanceof PauseScreen pauseScreen && !pauseScreen.showsPauseMenu()) {
						break label141;
					}

					var10000 = false;
					break label180;
				}

				var10000 = true;
			}

			boolean bl4 = var10000;
			if (k == 0) {
				KeyMapping.set(key, false);
				if (bl4 && i == 292) {
					if (this.handledDebugKey) {
						this.handledDebugKey = false;
					} else {
						this.minecraft.getDebugOverlay().toggleOverlay();
					}
				}
			} else {
				boolean bl5 = false;
				if (bl4) {
					if (i == 293 && this.minecraft.gameRenderer != null) {
						this.minecraft.gameRenderer.togglePostEffect();
					}

					if (i == 256) {
						this.minecraft.pauseGame(bl);
						bl5 |= bl;
					}

					bl5 |= bl && this.handleDebugKeys(i);
					this.handledDebugKey |= bl5;
					if (i == 290) {
						this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
					}

					if (this.minecraft.getDebugOverlay().showProfilerChart() && !bl && i >= 48 && i <= 57) {
						this.minecraft.getDebugOverlay().getProfilerPieChart().profilerPieChartKeyPress(i - 48);
					}
				}

				if (bl3) {
					if (bl5) {
						KeyMapping.set(key, false);
					} else {
						KeyMapping.set(key, true);
						KeyMapping.click(key);
					}
				}
			}
		}
	}

	private void charTyped(long l, int i, int j) {
		if (l == this.minecraft.getWindow().getWindow()) {
			GuiEventListener guiEventListener = this.minecraft.screen;
			if (guiEventListener != null && this.minecraft.getOverlay() == null) {
				if (Character.charCount(i) == 1) {
					Screen.wrapScreenError(() -> guiEventListener.charTyped((char)i, j), "charTyped event handler", guiEventListener.getClass().getCanonicalName());
				} else {
					for (char c : Character.toChars(i)) {
						Screen.wrapScreenError(() -> guiEventListener.charTyped(c, j), "charTyped event handler", guiEventListener.getClass().getCanonicalName());
					}
				}
			}
		}
	}

	public void setup(long l) {
		InputConstants.setupKeyboardCallbacks(
			l, (lx, i, j, k, m) -> this.minecraft.execute(() -> this.keyPress(lx, i, j, k, m)), (lx, i, j) -> this.minecraft.execute(() -> this.charTyped(lx, i, j))
		);
	}

	public String getClipboard() {
		return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (i, l) -> {
			if (i != 65545) {
				this.minecraft.getWindow().defaultErrorCallback(i, l);
			}
		});
	}

	public void setClipboard(String string) {
		if (!string.isEmpty()) {
			this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), string);
		}
	}

	public void tick() {
		if (this.debugCrashKeyTime > 0L) {
			long l = Util.getMillis();
			long m = 10000L - (l - this.debugCrashKeyTime);
			long n = l - this.debugCrashKeyReportedTime;
			if (m < 0L) {
				if (Screen.hasControlDown()) {
					Blaze3D.youJustLostTheGame();
				}

				String string = "Manually triggered debug crash";
				CrashReport crashReport = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
				CrashReportCategory crashReportCategory = crashReport.addCategory("Manual crash details");
				NativeModuleLister.addCrashSection(crashReportCategory);
				throw new ReportedException(crashReport);
			}

			if (n >= 1000L) {
				if (this.debugCrashKeyReportedCount == 0L) {
					this.debugFeedbackTranslated("debug.crash.message");
				} else {
					this.debugWarningTranslated("debug.crash.warning", Mth.ceil((float)m / 1000.0F));
				}

				this.debugCrashKeyReportedTime = l;
				this.debugCrashKeyReportedCount++;
			}
		}
	}
}
