package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerSelectionList extends ObjectSelectionList<ServerSelectionList.Entry> {
	static final Logger LOGGER = LogUtils.getLogger();
	static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(
		5,
		new ThreadFactoryBuilder()
			.setNameFormat("Server Pinger #%d")
			.setDaemon(true)
			.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
			.build()
	);
	private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
	static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
	static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
	static final Component SCANNING_LABEL = Component.translatable("lanServer.scanning");
	static final Component CANT_RESOLVE_TEXT = Component.translatable("multiplayer.status.cannot_resolve").withStyle(style -> style.withColor(-65536));
	static final Component CANT_CONNECT_TEXT = Component.translatable("multiplayer.status.cannot_connect").withStyle(style -> style.withColor(-65536));
	static final Component INCOMPATIBLE_STATUS = Component.translatable("multiplayer.status.incompatible");
	static final Component NO_CONNECTION_STATUS = Component.translatable("multiplayer.status.no_connection");
	static final Component PINGING_STATUS = Component.translatable("multiplayer.status.pinging");
	static final Component ONLINE_STATUS = Component.translatable("multiplayer.status.online");
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
		this.onlineServers.forEach(entry -> this.addEntry(entry));
		this.addEntry(this.lanHeader);
		this.networkServers.forEach(entry -> this.addEntry(entry));
	}

	public void setSelected(@Nullable ServerSelectionList.Entry entry) {
		super.setSelected(entry);
		this.screen.onSelectedChange();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		ServerSelectionList.Entry entry = this.getSelected();
		return entry != null && entry.keyPressed(i, j, k) || super.keyPressed(i, j, k);
	}

	public void updateOnlineServers(ServerList serverList) {
		this.onlineServers.clear();

		for (int i = 0; i < serverList.size(); i++) {
			this.onlineServers.add(new ServerSelectionList.OnlineServerEntry(this.screen, serverList.get(i)));
		}

		this.refreshEntries();
	}

	public void updateNetworkServers(List<LanServer> list) {
		int i = list.size() - this.networkServers.size();
		this.networkServers.clear();

		for (LanServer lanServer : list) {
			this.networkServers.add(new ServerSelectionList.NetworkServerEntry(this.screen, lanServer));
		}

		this.refreshEntries();

		for (int j = this.networkServers.size() - i; j < this.networkServers.size(); j++) {
			ServerSelectionList.NetworkServerEntry networkServerEntry = (ServerSelectionList.NetworkServerEntry)this.networkServers.get(j);
			int k = j - this.networkServers.size() + this.children().size();
			int l = this.getRowTop(k);
			int m = this.getRowBottom(k);
			if (m >= this.y0 && l <= this.y1) {
				this.minecraft.getNarrator().say(Component.translatable("multiplayer.lan.server_found", networkServerEntry.getServerNarration()));
			}
		}
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 30;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 85;
	}

	public void removed() {
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ObjectSelectionList.Entry<ServerSelectionList.Entry> implements AutoCloseable {
		public void close() {
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LANHeader extends ServerSelectionList.Entry {
		private final Minecraft minecraft = Minecraft.getInstance();

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			int p = j + m / 2 - 9 / 2;
			guiGraphics.drawString(
				this.minecraft.font,
				ServerSelectionList.SCANNING_LABEL,
				this.minecraft.screen.width / 2 - this.minecraft.font.width(ServerSelectionList.SCANNING_LABEL) / 2,
				p,
				16777215,
				false
			);
			String string = LoadingDotsText.get(Util.getMillis());
			guiGraphics.drawString(this.minecraft.font, string, this.minecraft.screen.width / 2 - this.minecraft.font.width(string) / 2, p + 9, 8421504, false);
		}

		@Override
		public Component getNarration() {
			return ServerSelectionList.SCANNING_LABEL;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class NetworkServerEntry extends ServerSelectionList.Entry {
		private static final int ICON_WIDTH = 32;
		private static final Component LAN_SERVER_HEADER = Component.translatable("lanServer.title");
		private static final Component HIDDEN_ADDRESS_TEXT = Component.translatable("selectServer.hiddenAddress");
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
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			guiGraphics.drawString(this.minecraft.font, LAN_SERVER_HEADER, k + 32 + 3, j + 1, 16777215, false);
			guiGraphics.drawString(this.minecraft.font, this.serverData.getMotd(), k + 32 + 3, j + 12, 8421504, false);
			if (this.minecraft.options.hideServerAddress) {
				guiGraphics.drawString(this.minecraft.font, HIDDEN_ADDRESS_TEXT, k + 32 + 3, j + 12 + 11, 3158064, false);
			} else {
				guiGraphics.drawString(this.minecraft.font, this.serverData.getAddress(), k + 32 + 3, j + 12 + 11, 3158064, false);
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

		@Override
		public Component getNarration() {
			return Component.translatable("narrator.select", this.getServerNarration());
		}

		public Component getServerNarration() {
			return Component.empty().append(LAN_SERVER_HEADER).append(CommonComponents.SPACE).append(this.serverData.getMotd());
		}
	}

	@Environment(EnvType.CLIENT)
	public class OnlineServerEntry extends ServerSelectionList.Entry {
		private static final int ICON_WIDTH = 32;
		private static final int ICON_HEIGHT = 32;
		private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
		private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
		private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
		private static final int ICON_OVERLAY_X_MOVE_UP = 96;
		private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
		private static final int ICON_OVERLAY_Y_SELECTED = 32;
		private final JoinMultiplayerScreen screen;
		private final Minecraft minecraft;
		private final ServerData serverData;
		private final FaviconTexture icon;
		@Nullable
		private byte[] lastIconBytes;
		private long lastClickTime;

		protected OnlineServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, ServerData serverData) {
			this.screen = joinMultiplayerScreen;
			this.serverData = serverData;
			this.minecraft = Minecraft.getInstance();
			this.icon = FaviconTexture.forServer(this.minecraft.getTextureManager(), serverData.ip);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			if (!this.serverData.pinged) {
				this.serverData.pinged = true;
				this.serverData.ping = -2L;
				this.serverData.motd = CommonComponents.EMPTY;
				this.serverData.status = CommonComponents.EMPTY;
				ServerSelectionList.THREAD_POOL.submit(() -> {
					try {
						this.screen.getPinger().pingServer(this.serverData, () -> this.minecraft.execute(this::updateServerList));
					} catch (UnknownHostException var2) {
						this.serverData.ping = -1L;
						this.serverData.motd = ServerSelectionList.CANT_RESOLVE_TEXT;
					} catch (Exception var3) {
						this.serverData.ping = -1L;
						this.serverData.motd = ServerSelectionList.CANT_CONNECT_TEXT;
					}
				});
			}

			boolean bl2 = !this.isCompatible();
			guiGraphics.drawString(this.minecraft.font, this.serverData.name, k + 32 + 3, j + 1, 16777215, false);
			List<FormattedCharSequence> list = this.minecraft.font.split(this.serverData.motd, l - 32 - 2);

			for (int p = 0; p < Math.min(list.size(), 2); p++) {
				guiGraphics.drawString(this.minecraft.font, (FormattedCharSequence)list.get(p), k + 32 + 3, j + 12 + 9 * p, 8421504, false);
			}

			Component component = (Component)(bl2 ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status);
			int q = this.minecraft.font.width(component);
			guiGraphics.drawString(this.minecraft.font, component, k + l - q - 15 - 2, j + 1, 8421504, false);
			int r = 0;
			int s;
			List<Component> list2;
			Component component2;
			if (bl2) {
				s = 5;
				component2 = ServerSelectionList.INCOMPATIBLE_STATUS;
				list2 = this.serverData.playerList;
			} else if (this.pingCompleted()) {
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
					component2 = ServerSelectionList.NO_CONNECTION_STATUS;
					list2 = Collections.emptyList();
				} else {
					component2 = Component.translatable("multiplayer.status.ping", this.serverData.ping);
					list2 = this.serverData.playerList;
				}
			} else {
				r = 1;
				s = (int)(Util.getMillis() / 100L + (long)(i * 2) & 7L);
				if (s > 4) {
					s = 8 - s;
				}

				component2 = ServerSelectionList.PINGING_STATUS;
				list2 = Collections.emptyList();
			}

			guiGraphics.blit(ServerSelectionList.GUI_ICONS_LOCATION, k + l - 15, j, (float)(r * 10), (float)(176 + s * 8), 10, 8, 256, 256);
			byte[] bs = this.serverData.getIconBytes();
			if (!Arrays.equals(bs, this.lastIconBytes)) {
				if (this.uploadServerIcon(bs)) {
					this.lastIconBytes = bs;
				} else {
					this.serverData.setIconBytes(null);
					this.updateServerList();
				}
			}

			this.drawIcon(guiGraphics, k, j, this.icon.textureLocation());
			int t = n - k;
			int u = o - j;
			if (t >= l - 15 && t <= l - 5 && u >= 0 && u <= 8) {
				this.screen.setToolTip(Collections.singletonList(component2));
			} else if (t >= l - q - 15 - 2 && t <= l - 15 - 2 && u >= 0 && u <= 8) {
				this.screen.setToolTip(list2);
			}

			if (this.minecraft.options.touchscreen().get() || bl) {
				guiGraphics.fill(k, j, k + 32, j + 32, -1601138544);
				int v = n - k;
				int w = o - j;
				if (this.canJoin()) {
					if (v < 32 && v > 16) {
						guiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 0.0F, 32.0F, 32, 32, 256, 256);
					} else {
						guiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 0.0F, 0.0F, 32, 32, 256, 256);
					}
				}

				if (i > 0) {
					if (v < 16 && w < 16) {
						guiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, 32.0F, 32, 32, 256, 256);
					} else {
						guiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, 0.0F, 32, 32, 256, 256);
					}
				}

				if (i < this.screen.getServers().size() - 1) {
					if (v < 16 && w > 16) {
						guiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 64.0F, 32.0F, 32, 32, 256, 256);
					} else {
						guiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 64.0F, 0.0F, 32, 32, 256, 256);
					}
				}
			}
		}

		private boolean pingCompleted() {
			return this.serverData.pinged && this.serverData.ping != -2L;
		}

		private boolean isCompatible() {
			return this.serverData.protocol == SharedConstants.getCurrentVersion().getProtocolVersion();
		}

		public void updateServerList() {
			this.screen.getServers().save();
		}

		protected void drawIcon(GuiGraphics guiGraphics, int i, int j, ResourceLocation resourceLocation) {
			RenderSystem.enableBlend();
			guiGraphics.blit(resourceLocation, i, j, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
		}

		private boolean canJoin() {
			return true;
		}

		private boolean uploadServerIcon(@Nullable byte[] bs) {
			if (bs == null) {
				this.icon.clear();
			} else {
				try {
					this.icon.upload(NativeImage.read(bs));
				} catch (Throwable var3) {
					ServerSelectionList.LOGGER.error("Invalid icon for server {} ({})", this.serverData.name, this.serverData.ip, var3);
					return false;
				}
			}

			return true;
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (Screen.hasShiftDown()) {
				ServerSelectionList serverSelectionList = this.screen.serverSelectionList;
				int l = serverSelectionList.children().indexOf(this);
				if (l == -1) {
					return true;
				}

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
			return true;
		}

		public ServerData getServerData() {
			return this.serverData;
		}

		@Override
		public Component getNarration() {
			MutableComponent mutableComponent = Component.empty();
			mutableComponent.append(Component.translatable("narrator.select", this.serverData.name));
			mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
			if (!this.isCompatible()) {
				mutableComponent.append(ServerSelectionList.INCOMPATIBLE_STATUS);
				mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
				mutableComponent.append(Component.translatable("multiplayer.status.version.narration", this.serverData.version));
				mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
				mutableComponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
			} else if (this.serverData.ping < 0L) {
				mutableComponent.append(ServerSelectionList.NO_CONNECTION_STATUS);
			} else if (!this.pingCompleted()) {
				mutableComponent.append(ServerSelectionList.PINGING_STATUS);
			} else {
				mutableComponent.append(ServerSelectionList.ONLINE_STATUS);
				mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
				mutableComponent.append(Component.translatable("multiplayer.status.ping.narration", this.serverData.ping));
				mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
				mutableComponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
				if (this.serverData.players != null) {
					mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
					mutableComponent.append(
						Component.translatable("multiplayer.status.player_count.narration", this.serverData.players.online(), this.serverData.players.max())
					);
					mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
					mutableComponent.append(ComponentUtils.formatList(this.serverData.playerList, Component.literal(", ")));
				}
			}

			return mutableComponent;
		}

		@Override
		public void close() {
			this.icon.close();
		}
	}
}
