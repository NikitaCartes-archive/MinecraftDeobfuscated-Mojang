package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Arrays;
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
import net.minecraft.client.renderer.RenderType;
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
	static final ResourceLocation INCOMPATIBLE_SPRITE = ResourceLocation.withDefaultNamespace("server_list/incompatible");
	static final ResourceLocation UNREACHABLE_SPRITE = ResourceLocation.withDefaultNamespace("server_list/unreachable");
	static final ResourceLocation PING_1_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_1");
	static final ResourceLocation PING_2_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_2");
	static final ResourceLocation PING_3_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_3");
	static final ResourceLocation PING_4_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_4");
	static final ResourceLocation PING_5_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_5");
	static final ResourceLocation PINGING_1_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_1");
	static final ResourceLocation PINGING_2_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_2");
	static final ResourceLocation PINGING_3_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_3");
	static final ResourceLocation PINGING_4_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_4");
	static final ResourceLocation PINGING_5_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_5");
	static final ResourceLocation JOIN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("server_list/join_highlighted");
	static final ResourceLocation JOIN_SPRITE = ResourceLocation.withDefaultNamespace("server_list/join");
	static final ResourceLocation MOVE_UP_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("server_list/move_up_highlighted");
	static final ResourceLocation MOVE_UP_SPRITE = ResourceLocation.withDefaultNamespace("server_list/move_up");
	static final ResourceLocation MOVE_DOWN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("server_list/move_down_highlighted");
	static final ResourceLocation MOVE_DOWN_SPRITE = ResourceLocation.withDefaultNamespace("server_list/move_down");
	static final Logger LOGGER = LogUtils.getLogger();
	static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(
		5,
		new ThreadFactoryBuilder()
			.setNameFormat("Server Pinger #%d")
			.setDaemon(true)
			.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
			.build()
	);
	static final Component SCANNING_LABEL = Component.translatable("lanServer.scanning");
	static final Component CANT_RESOLVE_TEXT = Component.translatable("multiplayer.status.cannot_resolve").withColor(-65536);
	static final Component CANT_CONNECT_TEXT = Component.translatable("multiplayer.status.cannot_connect").withColor(-65536);
	static final Component INCOMPATIBLE_STATUS = Component.translatable("multiplayer.status.incompatible");
	static final Component NO_CONNECTION_STATUS = Component.translatable("multiplayer.status.no_connection");
	static final Component PINGING_STATUS = Component.translatable("multiplayer.status.pinging");
	static final Component ONLINE_STATUS = Component.translatable("multiplayer.status.online");
	private final JoinMultiplayerScreen screen;
	private final List<ServerSelectionList.OnlineServerEntry> onlineServers = Lists.<ServerSelectionList.OnlineServerEntry>newArrayList();
	private final ServerSelectionList.Entry lanHeader = new ServerSelectionList.LANHeader();
	private final List<ServerSelectionList.NetworkServerEntry> networkServers = Lists.<ServerSelectionList.NetworkServerEntry>newArrayList();

	public ServerSelectionList(JoinMultiplayerScreen joinMultiplayerScreen, Minecraft minecraft, int i, int j, int k, int l) {
		super(minecraft, i, j, k, l);
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
			if (m >= this.getY() && l <= this.getBottom()) {
				this.minecraft.getNarrator().say(Component.translatable("multiplayer.lan.server_found", networkServerEntry.getServerNarration()));
			}
		}
	}

	@Override
	public int getRowWidth() {
		return 305;
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
			guiGraphics.drawString(this.minecraft.font, string, this.minecraft.screen.width / 2 - this.minecraft.font.width(string) / 2, p + 9, -8355712, false);
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
			guiGraphics.drawString(this.minecraft.font, this.serverData.getMotd(), k + 32 + 3, j + 12, -8355712, false);
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
			return super.mouseClicked(d, e, i);
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
		private static final int SPACING = 5;
		private static final int STATUS_ICON_WIDTH = 10;
		private static final int STATUS_ICON_HEIGHT = 8;
		private final JoinMultiplayerScreen screen;
		private final Minecraft minecraft;
		private final ServerData serverData;
		private final FaviconTexture icon;
		@Nullable
		private byte[] lastIconBytes;
		private long lastClickTime;
		@Nullable
		private List<Component> onlinePlayersTooltip;
		@Nullable
		private ResourceLocation statusIcon;
		@Nullable
		private Component statusIconTooltip;

		protected OnlineServerEntry(final JoinMultiplayerScreen joinMultiplayerScreen, final ServerData serverData) {
			this.screen = joinMultiplayerScreen;
			this.serverData = serverData;
			this.minecraft = Minecraft.getInstance();
			this.icon = FaviconTexture.forServer(this.minecraft.getTextureManager(), serverData.ip);
			this.refreshStatus();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			if (this.serverData.state() == ServerData.State.INITIAL) {
				this.serverData.setState(ServerData.State.PINGING);
				this.serverData.motd = CommonComponents.EMPTY;
				this.serverData.status = CommonComponents.EMPTY;
				ServerSelectionList.THREAD_POOL
					.submit(
						() -> {
							try {
								this.screen
									.getPinger()
									.pingServer(
										this.serverData,
										() -> this.minecraft.execute(this::updateServerList),
										() -> {
											this.serverData
												.setState(
													this.serverData.protocol == SharedConstants.getCurrentVersion().getProtocolVersion() ? ServerData.State.SUCCESSFUL : ServerData.State.INCOMPATIBLE
												);
											this.minecraft.execute(this::refreshStatus);
										}
									);
							} catch (UnknownHostException var2) {
								this.serverData.setState(ServerData.State.UNREACHABLE);
								this.serverData.motd = ServerSelectionList.CANT_RESOLVE_TEXT;
								this.minecraft.execute(this::refreshStatus);
							} catch (Exception var3) {
								this.serverData.setState(ServerData.State.UNREACHABLE);
								this.serverData.motd = ServerSelectionList.CANT_CONNECT_TEXT;
								this.minecraft.execute(this::refreshStatus);
							}
						}
					);
			}

			guiGraphics.drawString(this.minecraft.font, this.serverData.name, k + 32 + 3, j + 1, 16777215, false);
			List<FormattedCharSequence> list = this.minecraft.font.split(this.serverData.motd, l - 32 - 2);

			for (int p = 0; p < Math.min(list.size(), 2); p++) {
				guiGraphics.drawString(this.minecraft.font, (FormattedCharSequence)list.get(p), k + 32 + 3, j + 12 + 9 * p, -8355712, false);
			}

			this.drawIcon(guiGraphics, k, j, this.icon.textureLocation());
			if (this.serverData.state() == ServerData.State.PINGING) {
				int p = (int)(Util.getMillis() / 100L + (long)(i * 2) & 7L);
				if (p > 4) {
					p = 8 - p;
				}
				this.statusIcon = switch (p) {
					case 1 -> ServerSelectionList.PINGING_2_SPRITE;
					case 2 -> ServerSelectionList.PINGING_3_SPRITE;
					case 3 -> ServerSelectionList.PINGING_4_SPRITE;
					case 4 -> ServerSelectionList.PINGING_5_SPRITE;
					default -> ServerSelectionList.PINGING_1_SPRITE;
				};
			}

			int p = k + l - 10 - 5;
			if (this.statusIcon != null) {
				guiGraphics.blitSprite(RenderType::guiTextured, this.statusIcon, p, j, 10, 8);
			}

			byte[] bs = this.serverData.getIconBytes();
			if (!Arrays.equals(bs, this.lastIconBytes)) {
				if (this.uploadServerIcon(bs)) {
					this.lastIconBytes = bs;
				} else {
					this.serverData.setIconBytes(null);
					this.updateServerList();
				}
			}

			Component component = (Component)(this.serverData.state() == ServerData.State.INCOMPATIBLE
				? this.serverData.version.copy().withStyle(ChatFormatting.RED)
				: this.serverData.status);
			int q = this.minecraft.font.width(component);
			int r = p - q - 5;
			guiGraphics.drawString(this.minecraft.font, component, r, j + 1, -8355712, false);
			if (this.statusIconTooltip != null && n >= p && n <= p + 10 && o >= j && o <= j + 8) {
				this.screen.setTooltipForNextRenderPass(this.statusIconTooltip);
			} else if (this.onlinePlayersTooltip != null && n >= r && n <= r + q && o >= j && o <= j - 1 + 9) {
				this.screen.setTooltipForNextRenderPass(Lists.transform(this.onlinePlayersTooltip, Component::getVisualOrderText));
			}

			if (this.minecraft.options.touchscreen().get() || bl) {
				guiGraphics.fill(k, j, k + 32, j + 32, -1601138544);
				int s = n - k;
				int t = o - j;
				if (this.canJoin()) {
					if (s < 32 && s > 16) {
						guiGraphics.blitSprite(RenderType::guiTextured, ServerSelectionList.JOIN_HIGHLIGHTED_SPRITE, k, j, 32, 32);
					} else {
						guiGraphics.blitSprite(RenderType::guiTextured, ServerSelectionList.JOIN_SPRITE, k, j, 32, 32);
					}
				}

				if (i > 0) {
					if (s < 16 && t < 16) {
						guiGraphics.blitSprite(RenderType::guiTextured, ServerSelectionList.MOVE_UP_HIGHLIGHTED_SPRITE, k, j, 32, 32);
					} else {
						guiGraphics.blitSprite(RenderType::guiTextured, ServerSelectionList.MOVE_UP_SPRITE, k, j, 32, 32);
					}
				}

				if (i < this.screen.getServers().size() - 1) {
					if (s < 16 && t > 16) {
						guiGraphics.blitSprite(RenderType::guiTextured, ServerSelectionList.MOVE_DOWN_HIGHLIGHTED_SPRITE, k, j, 32, 32);
					} else {
						guiGraphics.blitSprite(RenderType::guiTextured, ServerSelectionList.MOVE_DOWN_SPRITE, k, j, 32, 32);
					}
				}
			}
		}

		private void refreshStatus() {
			this.onlinePlayersTooltip = null;
			switch (this.serverData.state()) {
				case INITIAL:
				case PINGING:
					this.statusIcon = ServerSelectionList.PING_1_SPRITE;
					this.statusIconTooltip = ServerSelectionList.PINGING_STATUS;
					break;
				case INCOMPATIBLE:
					this.statusIcon = ServerSelectionList.INCOMPATIBLE_SPRITE;
					this.statusIconTooltip = ServerSelectionList.INCOMPATIBLE_STATUS;
					this.onlinePlayersTooltip = this.serverData.playerList;
					break;
				case UNREACHABLE:
					this.statusIcon = ServerSelectionList.UNREACHABLE_SPRITE;
					this.statusIconTooltip = ServerSelectionList.NO_CONNECTION_STATUS;
					break;
				case SUCCESSFUL:
					if (this.serverData.ping < 150L) {
						this.statusIcon = ServerSelectionList.PING_5_SPRITE;
					} else if (this.serverData.ping < 300L) {
						this.statusIcon = ServerSelectionList.PING_4_SPRITE;
					} else if (this.serverData.ping < 600L) {
						this.statusIcon = ServerSelectionList.PING_3_SPRITE;
					} else if (this.serverData.ping < 1000L) {
						this.statusIcon = ServerSelectionList.PING_2_SPRITE;
					} else {
						this.statusIcon = ServerSelectionList.PING_1_SPRITE;
					}

					this.statusIconTooltip = Component.translatable("multiplayer.status.ping", this.serverData.ping);
					this.onlinePlayersTooltip = this.serverData.playerList;
			}
		}

		public void updateServerList() {
			this.screen.getServers().save();
		}

		protected void drawIcon(GuiGraphics guiGraphics, int i, int j, ResourceLocation resourceLocation) {
			guiGraphics.blit(RenderType::guiTextured, resourceLocation, i, j, 0.0F, 0.0F, 32, 32, 32, 32);
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
			return super.mouseClicked(d, e, i);
		}

		public ServerData getServerData() {
			return this.serverData;
		}

		@Override
		public Component getNarration() {
			MutableComponent mutableComponent = Component.empty();
			mutableComponent.append(Component.translatable("narrator.select", this.serverData.name));
			mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
			switch (this.serverData.state()) {
				case PINGING:
					mutableComponent.append(ServerSelectionList.PINGING_STATUS);
					break;
				case INCOMPATIBLE:
					mutableComponent.append(ServerSelectionList.INCOMPATIBLE_STATUS);
					mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
					mutableComponent.append(Component.translatable("multiplayer.status.version.narration", this.serverData.version));
					mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
					mutableComponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
					break;
				case UNREACHABLE:
					mutableComponent.append(ServerSelectionList.NO_CONNECTION_STATUS);
					break;
				default:
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
