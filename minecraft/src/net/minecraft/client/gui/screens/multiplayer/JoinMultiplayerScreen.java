package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class JoinMultiplayerScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ServerStatusPinger pinger = new ServerStatusPinger();
	private final Screen lastScreen;
	protected ServerSelectionList serverSelectionList;
	private ServerList servers;
	private Button editButton;
	private Button selectButton;
	private Button deleteButton;
	@Nullable
	private List<Component> toolTip;
	private ServerData editingServer;
	private LanServerDetection.LanServerList lanServerList;
	@Nullable
	private LanServerDetection.LanServerDetector lanServerDetector;
	private boolean initedOnce;

	public JoinMultiplayerScreen(Screen screen) {
		super(Component.translatable("multiplayer.title"));
		this.lastScreen = screen;
	}

	@Override
	protected void init() {
		super.init();
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		if (this.initedOnce) {
			this.serverSelectionList.updateSize(this.width, this.height, 32, this.height - 64);
		} else {
			this.initedOnce = true;
			this.servers = new ServerList(this.minecraft);
			this.servers.load();
			this.lanServerList = new LanServerDetection.LanServerList();

			try {
				this.lanServerDetector = new LanServerDetection.LanServerDetector(this.lanServerList);
				this.lanServerDetector.start();
			} catch (Exception var2) {
				LOGGER.warn("Unable to start LAN server detection: {}", var2.getMessage());
			}

			this.serverSelectionList = new ServerSelectionList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
			this.serverSelectionList.updateOnlineServers(this.servers);
		}

		this.addWidget(this.serverSelectionList);
		this.selectButton = this.addRenderableWidget(
			new Button(this.width / 2 - 154, this.height - 52, 100, 20, Component.translatable("selectServer.select"), button -> this.joinSelectedServer())
		);
		this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 52, 100, 20, Component.translatable("selectServer.direct"), button -> {
			this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
			this.minecraft.setScreen(new DirectJoinServerScreen(this, this::directJoinCallback, this.editingServer));
		}));
		this.addRenderableWidget(new Button(this.width / 2 + 4 + 50, this.height - 52, 100, 20, Component.translatable("selectServer.add"), button -> {
			this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
			this.minecraft.setScreen(new EditServerScreen(this, this::addServerCallback, this.editingServer));
		}));
		this.editButton = this.addRenderableWidget(
			new Button(this.width / 2 - 154, this.height - 28, 70, 20, Component.translatable("selectServer.edit"), button -> {
				ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
				if (entry instanceof ServerSelectionList.OnlineServerEntry) {
					ServerData serverData = ((ServerSelectionList.OnlineServerEntry)entry).getServerData();
					this.editingServer = new ServerData(serverData.name, serverData.ip, false);
					this.editingServer.copyFrom(serverData);
					this.minecraft.setScreen(new EditServerScreen(this, this::editServerCallback, this.editingServer));
				}
			})
		);
		this.deleteButton = this.addRenderableWidget(
			new Button(this.width / 2 - 74, this.height - 28, 70, 20, Component.translatable("selectServer.delete"), button -> {
				ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
				if (entry instanceof ServerSelectionList.OnlineServerEntry) {
					String string = ((ServerSelectionList.OnlineServerEntry)entry).getServerData().name;
					if (string != null) {
						Component component = Component.translatable("selectServer.deleteQuestion");
						Component component2 = Component.translatable("selectServer.deleteWarning", string);
						Component component3 = Component.translatable("selectServer.deleteButton");
						Component component4 = CommonComponents.GUI_CANCEL;
						this.minecraft.setScreen(new ConfirmScreen(this::deleteCallback, component, component2, component3, component4));
					}
				}
			})
		);
		this.addRenderableWidget(
			new Button(this.width / 2 + 4, this.height - 28, 70, 20, Component.translatable("selectServer.refresh"), button -> this.refreshServerList())
		);
		this.addRenderableWidget(
			new Button(this.width / 2 + 4 + 76, this.height - 28, 75, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen))
		);
		this.onSelectedChange();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.lanServerList.isDirty()) {
			List<LanServer> list = this.lanServerList.getServers();
			this.lanServerList.markClean();
			this.serverSelectionList.updateNetworkServers(list);
		}

		this.pinger.tick();
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		if (this.lanServerDetector != null) {
			this.lanServerDetector.interrupt();
			this.lanServerDetector = null;
		}

		this.pinger.removeAll();
	}

	private void refreshServerList() {
		this.minecraft.setScreen(new JoinMultiplayerScreen(this.lastScreen));
	}

	private void deleteCallback(boolean bl) {
		ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
		if (bl && entry instanceof ServerSelectionList.OnlineServerEntry) {
			this.servers.remove(((ServerSelectionList.OnlineServerEntry)entry).getServerData());
			this.servers.save();
			this.serverSelectionList.setSelected(null);
			this.serverSelectionList.updateOnlineServers(this.servers);
		}

		this.minecraft.setScreen(this);
	}

	private void editServerCallback(boolean bl) {
		ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
		if (bl && entry instanceof ServerSelectionList.OnlineServerEntry) {
			ServerData serverData = ((ServerSelectionList.OnlineServerEntry)entry).getServerData();
			serverData.name = this.editingServer.name;
			serverData.ip = this.editingServer.ip;
			serverData.copyFrom(this.editingServer);
			this.servers.save();
			this.serverSelectionList.updateOnlineServers(this.servers);
		}

		this.minecraft.setScreen(this);
	}

	private void addServerCallback(boolean bl) {
		if (bl) {
			this.servers.add(this.editingServer);
			this.servers.save();
			this.serverSelectionList.setSelected(null);
			this.serverSelectionList.updateOnlineServers(this.servers);
		}

		this.minecraft.setScreen(this);
	}

	private void directJoinCallback(boolean bl) {
		if (bl) {
			this.join(this.editingServer);
		} else {
			this.minecraft.setScreen(this);
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else if (i == 294) {
			this.refreshServerList();
			return true;
		} else if (this.serverSelectionList.getSelected() != null) {
			if (i != 257 && i != 335) {
				return this.serverSelectionList.keyPressed(i, j, k);
			} else {
				this.joinSelectedServer();
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.toolTip = null;
		this.renderBackground(poseStack);
		this.serverSelectionList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215);
		super.render(poseStack, i, j, f);
		if (this.toolTip != null) {
			this.renderComponentTooltip(poseStack, this.toolTip, i, j);
		}
	}

	public void joinSelectedServer() {
		ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
		if (entry instanceof ServerSelectionList.OnlineServerEntry) {
			this.join(((ServerSelectionList.OnlineServerEntry)entry).getServerData());
		} else if (entry instanceof ServerSelectionList.NetworkServerEntry) {
			LanServer lanServer = ((ServerSelectionList.NetworkServerEntry)entry).getServerData();
			this.join(new ServerData(lanServer.getMotd(), lanServer.getAddress(), true));
		}
	}

	private void join(ServerData serverData) {
		ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(serverData.ip), serverData);
	}

	public void setSelected(ServerSelectionList.Entry entry) {
		this.serverSelectionList.setSelected(entry);
		this.onSelectedChange();
	}

	protected void onSelectedChange() {
		this.selectButton.active = false;
		this.editButton.active = false;
		this.deleteButton.active = false;
		ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
		if (entry != null && !(entry instanceof ServerSelectionList.LANHeader)) {
			this.selectButton.active = true;
			if (entry instanceof ServerSelectionList.OnlineServerEntry) {
				this.editButton.active = true;
				this.deleteButton.active = true;
			}
		}
	}

	public ServerStatusPinger getPinger() {
		return this.pinger;
	}

	public void setToolTip(List<Component> list) {
		this.toolTip = list;
	}

	public ServerList getServers() {
		return this.servers;
	}
}
