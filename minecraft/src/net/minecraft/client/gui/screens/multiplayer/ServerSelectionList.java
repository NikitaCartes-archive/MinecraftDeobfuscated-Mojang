package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerSelectionList extends ObjectSelectionList<ServerSelectionList.Entry> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(
		5,
		new ThreadFactoryBuilder()
			.setNameFormat("Server Pinger #%d")
			.setDaemon(true)
			.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
			.build()
	);
	private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
	private final JoinMultiplayerScreen screen;
	private final List<ServerSelectionList.OnlineServerEntry> onlineServers = Lists.<ServerSelectionList.OnlineServerEntry>newArrayList();
	private final ServerSelectionList.Entry lanHeader = new ServerSelectionList.LANHeader();
	private final List<ServerSelectionList.NetworkServerEntry> networkServers = Lists.<ServerSelectionList.NetworkServerEntry>newArrayList();

	public ServerSelectionList(JoinMultiplayerScreen joinMultiplayerScreen, Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
		this.screen = joinMultiplayerScreen;
	}

	private void refreshEntries() {
		this.clearEntries();
		this.onlineServers.forEach(this::addEntry);
		this.addEntry(this.lanHeader);
		this.networkServers.forEach(this::addEntry);
	}

	public void setSelected(ServerSelectionList.Entry entry) {
		super.setSelected(entry);
		if (this.getSelected() instanceof ServerSelectionList.OnlineServerEntry) {
			NarratorChatListener.INSTANCE
				.sayNow(new TranslatableComponent("narrator.select", ((ServerSelectionList.OnlineServerEntry)this.getSelected()).serverData.name).getString());
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		ServerSelectionList.Entry entry = this.getSelected();
		return entry != null && entry.keyPressed(i, j, k) || super.keyPressed(i, j, k);
	}

	@Override
	protected void moveSelection(int i) {
		int j = this.children().indexOf(this.getSelected());
		int k = Mth.clamp(j + i, 0, this.getItemCount() - 1);
		ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.children().get(k);
		super.setSelected(entry);
		if (entry instanceof ServerSelectionList.LANHeader) {
			if (i <= 0 || k != this.getItemCount() - 1) {
				if (i >= 0 || k != 0) {
					this.moveSelection(i);
				}
			}
		} else {
			this.ensureVisible(entry);
			this.screen.onSelectedChange();
		}
	}

	public void updateOnlineServers(ServerList serverList) {
		this.onlineServers.clear();

		for (int i = 0; i < serverList.size(); i++) {
			this.onlineServers.add(new ServerSelectionList.OnlineServerEntry(this.screen, serverList.get(i)));
		}

		this.refreshEntries();
	}

	public void updateNetworkServers(List<LanServer> list) {
		this.networkServers.clear();

		for (LanServer lanServer : list) {
			this.networkServers.add(new ServerSelectionList.NetworkServerEntry(this.screen, lanServer));
		}

		this.refreshEntries();
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 30;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 85;
	}

	@Override
	protected boolean isFocused() {
		return this.screen.getFocused() == this;
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ObjectSelectionList.Entry<ServerSelectionList.Entry> {
	}

	@Environment(EnvType.CLIENT)
	public static class LANHeader extends ServerSelectionList.Entry {
		private final Minecraft minecraft = Minecraft.getInstance();

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			int p = j + m / 2 - 9 / 2;
			this.minecraft
				.font
				.draw(
					I18n.get("lanServer.scanning"),
					(float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(I18n.get("lanServer.scanning")) / 2),
					(float)p,
					16777215
				);
			String string;
			switch ((int)(Util.getMillis() / 300L % 4L)) {
				case 0:
				default:
					string = "O o o";
					break;
				case 1:
				case 3:
					string = "o O o";
					break;
				case 2:
					string = "o o O";
			}

			this.minecraft.font.draw(string, (float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(string) / 2), (float)(p + 9), 8421504);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class NetworkServerEntry extends ServerSelectionList.Entry {
		private final JoinMultiplayerScreen screen;
		protected final Minecraft minecraft;
		protected final LanServer serverData;
		private long lastClickTime;

		protected NetworkServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, LanServer lanServer) {
			this.screen = joinMultiplayerScreen;
			this.serverData = lanServer;
			this.minecraft = Minecraft.getInstance();
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.minecraft.font.draw(I18n.get("lanServer.title"), (float)(k + 32 + 3), (float)(j + 1), 16777215);
			this.minecraft.font.draw(this.serverData.getMotd(), (float)(k + 32 + 3), (float)(j + 12), 8421504);
			if (this.minecraft.options.hideServerAddress) {
				this.minecraft.font.draw(I18n.get("selectServer.hiddenAddress"), (float)(k + 32 + 3), (float)(j + 12 + 11), 3158064);
			} else {
				this.minecraft.font.draw(this.serverData.getAddress(), (float)(k + 32 + 3), (float)(j + 12 + 11), 3158064);
			}
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			this.screen.setSelected(this);
			if (Util.getMillis() - this.lastClickTime < 250L) {
				this.screen.joinSelectedServer();
			}

			this.lastClickTime = Util.getMillis();
			return false;
		}

		public LanServer getServerData() {
			return this.serverData;
		}
	}

	@Environment(EnvType.CLIENT)
	public class OnlineServerEntry extends ServerSelectionList.Entry {
		private final JoinMultiplayerScreen screen;
		private final Minecraft minecraft;
		private final ServerData serverData;
		private final ResourceLocation iconLocation;
		private String lastIconB64;
		private DynamicTexture icon;
		private long lastClickTime;

		protected OnlineServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, ServerData serverData) {
			this.screen = joinMultiplayerScreen;
			this.serverData = serverData;
			this.minecraft = Minecraft.getInstance();
			this.iconLocation = new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(serverData.ip) + "/icon");
			this.icon = (DynamicTexture)this.minecraft.getTextureManager().getTexture(this.iconLocation);
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			if (!this.serverData.pinged) {
				this.serverData.pinged = true;
				this.serverData.ping = -2L;
				this.serverData.motd = "";
				this.serverData.status = "";
				ServerSelectionList.THREAD_POOL.submit(() -> {
					try {
						this.screen.getPinger().pingServer(this.serverData);
					} catch (UnknownHostException var2) {
						this.serverData.ping = -1L;
						this.serverData.motd = ChatFormatting.DARK_RED + I18n.get("multiplayer.status.cannot_resolve");
					} catch (Exception var3) {
						this.serverData.ping = -1L;
						this.serverData.motd = ChatFormatting.DARK_RED + I18n.get("multiplayer.status.cannot_connect");
					}
				});
			}

			boolean bl2 = this.serverData.protocol > SharedConstants.getCurrentVersion().getProtocolVersion();
			boolean bl3 = this.serverData.protocol < SharedConstants.getCurrentVersion().getProtocolVersion();
			boolean bl4 = bl2 || bl3;
			this.minecraft.font.draw(this.serverData.name, (float)(k + 32 + 3), (float)(j + 1), 16777215);
			List<String> list = this.minecraft.font.split(this.serverData.motd, l - 32 - 2);

			for (int p = 0; p < Math.min(list.size(), 2); p++) {
				this.minecraft.font.draw((String)list.get(p), (float)(k + 32 + 3), (float)(j + 12 + 9 * p), 8421504);
			}

			String string = bl4 ? ChatFormatting.DARK_RED + this.serverData.version : this.serverData.status;
			int q = this.minecraft.font.width(string);
			this.minecraft.font.draw(string, (float)(k + l - q - 15 - 2), (float)(j + 1), 8421504);
			int r = 0;
			String string2 = null;
			int s;
			String string3;
			if (bl4) {
				s = 5;
				string3 = I18n.get(bl2 ? "multiplayer.status.client_out_of_date" : "multiplayer.status.server_out_of_date");
				string2 = this.serverData.playerList;
			} else if (this.serverData.pinged && this.serverData.ping != -2L) {
				if (this.serverData.ping < 0L) {
					s = 5;
				} else if (this.serverData.ping < 150L) {
					s = 0;
				} else if (this.serverData.ping < 300L) {
					s = 1;
				} else if (this.serverData.ping < 600L) {
					s = 2;
				} else if (this.serverData.ping < 1000L) {
					s = 3;
				} else {
					s = 4;
				}

				if (this.serverData.ping < 0L) {
					string3 = I18n.get("multiplayer.status.no_connection");
				} else {
					string3 = this.serverData.ping + "ms";
					string2 = this.serverData.playerList;
				}
			} else {
				r = 1;
				s = (int)(Util.getMillis() / 100L + (long)(i * 2) & 7L);
				if (s > 4) {
					s = 8 - s;
				}

				string3 = I18n.get("multiplayer.status.pinging");
			}

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
			GuiComponent.blit(k + l - 15, j, (float)(r * 10), (float)(176 + s * 8), 10, 8, 256, 256);
			if (this.serverData.getIconB64() != null && !this.serverData.getIconB64().equals(this.lastIconB64)) {
				this.lastIconB64 = this.serverData.getIconB64();
				this.loadServerIcon();
				this.screen.getServers().save();
			}

			if (this.icon != null) {
				this.drawIcon(k, j, this.iconLocation);
			} else {
				this.drawIcon(k, j, ServerSelectionList.ICON_MISSING);
			}

			int t = n - k;
			int u = o - j;
			if (t >= l - 15 && t <= l - 5 && u >= 0 && u <= 8) {
				this.screen.setToolTip(string3);
			} else if (t >= l - q - 15 - 2 && t <= l - 15 - 2 && u >= 0 && u <= 8) {
				this.screen.setToolTip(string2);
			}

			if (this.minecraft.options.touchscreen || bl) {
				this.minecraft.getTextureManager().bind(ServerSelectionList.ICON_OVERLAY_LOCATION);
				GuiComponent.fill(k, j, k + 32, j + 32, -1601138544);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				int v = n - k;
				int w = o - j;
				if (this.canJoin()) {
					if (v < 32 && v > 16) {
						GuiComponent.blit(k, j, 0.0F, 32.0F, 32, 32, 256, 256);
					} else {
						GuiComponent.blit(k, j, 0.0F, 0.0F, 32, 32, 256, 256);
					}
				}

				if (i > 0) {
					if (v < 16 && w < 16) {
						GuiComponent.blit(k, j, 96.0F, 32.0F, 32, 32, 256, 256);
					} else {
						GuiComponent.blit(k, j, 96.0F, 0.0F, 32, 32, 256, 256);
					}
				}

				if (i < this.screen.getServers().size() - 1) {
					if (v < 16 && w > 16) {
						GuiComponent.blit(k, j, 64.0F, 32.0F, 32, 32, 256, 256);
					} else {
						GuiComponent.blit(k, j, 64.0F, 0.0F, 32, 32, 256, 256);
					}
				}
			}
		}

		protected void drawIcon(int i, int j, ResourceLocation resourceLocation) {
			this.minecraft.getTextureManager().bind(resourceLocation);
			RenderSystem.enableBlend();
			GuiComponent.blit(i, j, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
		}

		private boolean canJoin() {
			return true;
		}

		private void loadServerIcon() {
			String string = this.serverData.getIconB64();
			if (string == null) {
				this.minecraft.getTextureManager().release(this.iconLocation);
				if (this.icon != null && this.icon.getPixels() != null) {
					this.icon.getPixels().close();
				}

				this.icon = null;
			} else {
				try {
					NativeImage nativeImage = NativeImage.fromBase64(string);
					Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
					Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high");
					if (this.icon == null) {
						this.icon = new DynamicTexture(nativeImage);
					} else {
						this.icon.setPixels(nativeImage);
						this.icon.upload();
					}

					this.minecraft.getTextureManager().register(this.iconLocation, this.icon);
				} catch (Throwable var3) {
					ServerSelectionList.LOGGER.error("Invalid icon for server {} ({})", this.serverData.name, this.serverData.ip, var3);
					this.serverData.setIconB64(null);
				}
			}
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (Screen.hasShiftDown()) {
				ServerSelectionList serverSelectionList = this.screen.serverSelectionList;
				int l = serverSelectionList.children().indexOf(this);
				if (i == 264 && l < this.screen.getServers().size() - 1 || i == 265 && l > 0) {
					this.swap(l, i == 264 ? l + 1 : l - 1);
					return true;
				}
			}

			return super.keyPressed(i, j, k);
		}

		private void swap(int i, int j) {
			this.screen.getServers().swap(i, j);
			this.screen.serverSelectionList.updateOnlineServers(this.screen.getServers());
			ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.screen.serverSelectionList.children().get(j);
			this.screen.serverSelectionList.setSelected(entry);
			ServerSelectionList.this.ensureVisible(entry);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			double f = d - (double)ServerSelectionList.this.getRowLeft();
			double g = e - (double)ServerSelectionList.this.getRowTop(ServerSelectionList.this.children().indexOf(this));
			if (f <= 32.0) {
				if (f < 32.0 && f > 16.0 && this.canJoin()) {
					this.screen.setSelected(this);
					this.screen.joinSelectedServer();
					return true;
				}

				int j = this.screen.serverSelectionList.children().indexOf(this);
				if (f < 16.0 && g < 16.0 && j > 0) {
					this.swap(j, j - 1);
					return true;
				}

				if (f < 16.0 && g > 16.0 && j < this.screen.getServers().size() - 1) {
					this.swap(j, j + 1);
					return true;
				}
			}

			this.screen.setSelected(this);
			if (Util.getMillis() - this.lastClickTime < 250L) {
				this.screen.joinSelectedServer();
			}

			this.lastClickTime = Util.getMillis();
			return false;
		}

		public ServerData getServerData() {
			return this.serverData;
		}
	}
}
