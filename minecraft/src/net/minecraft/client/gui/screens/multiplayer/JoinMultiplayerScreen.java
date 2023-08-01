package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
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
	public static final int BUTTON_ROW_WIDTH = 308;
	public static final int TOP_ROW_BUTTON_WIDTH = 100;
	public static final int LOWER_ROW_BUTTON_WIDTH = 74;
	public static final int FOOTER_HEIGHT = 64;
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
			} catch (Exception var8) {
				LOGGER.warn("Unable to start LAN server detection: {}", var8.getMessage());
			}

			this.serverSelectionList = new ServerSelectionList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
			this.serverSelectionList.updateOnlineServers(this.servers);
		}

		this.addWidget(this.serverSelectionList);
		this.selectButton = this.addRenderableWidget(
			Button.builder(Component.translatable("selectServer.select"), buttonx -> this.joinSelectedServer()).width(100).build()
		);
		Button button = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.direct"), buttonx -> {
			this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", ServerData.Type.OTHER);
			this.minecraft.setScreen(new DirectJoinServerScreen(this, this::directJoinCallback, this.editingServer));
		}).width(100).build());
		Button button2 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.add"), buttonx -> {
			this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", ServerData.Type.OTHER);
			this.minecraft.setScreen(new EditServerScreen(this, this::addServerCallback, this.editingServer));
		}).width(100).build());
		this.editButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.edit"), buttonx -> {
			ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
			if (entry instanceof ServerSelectionList.OnlineServerEntry) {
				ServerData serverData = ((ServerSelectionList.OnlineServerEntry)entry).getServerData();
				this.editingServer = new ServerData(serverData.name, serverData.ip, ServerData.Type.OTHER);
				this.editingServer.copyFrom(serverData);
				this.minecraft.setScreen(new EditServerScreen(this, this::editServerCallback, this.editingServer));
			}
		}).width(74).build());
		this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.delete"), buttonx -> {
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
		}).width(74).build());
		Button button3 = this.addRenderableWidget(
			Button.builder(Component.translatable("selectServer.refresh"), buttonx -> this.refreshServerList()).width(74).build()
		);
		Button button4 = this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, buttonx -> this.minecraft.setScreen(this.lastScreen)).width(74).build());
		LinearLayout linearLayout = LinearLayout.vertical();
		EqualSpacingLayout equalSpacingLayout = linearLayout.addChild(new EqualSpacingLayout(308, 20, EqualSpacingLayout.Orientation.HORIZONTAL));
		equalSpacingLayout.addChild(this.selectButton);
		equalSpacingLayout.addChild(button);
		equalSpacingLayout.addChild(button2);
		linearLayout.addChild(SpacerElement.height(4));
		EqualSpacingLayout equalSpacingLayout2 = linearLayout.addChild(new EqualSpacingLayout(308, 20, EqualSpacingLayout.Orientation.HORIZONTAL));
		equalSpacingLayout2.addChild(this.editButton);
		equalSpacingLayout2.addChild(this.deleteButton);
		equalSpacingLayout2.addChild(button3);
		equalSpacingLayout2.addChild(button4);
		linearLayout.arrangeElements();
		FrameLayout.centerInRectangle(linearLayout, 0, this.height - 64, this.width, 64);
		this.onSelectedChange();
	}

	@Override
	public void tick() {
		super.tick();
		List<LanServer> list = this.lanServerList.takeDirtyServers();
		if (list != null) {
			this.serverSelectionList.updateNetworkServers(list);
		}

		this.pinger.tick();
	}

	@Override
	public void removed() {
		if (this.lanServerDetector != null) {
			this.lanServerDetector.interrupt();
			this.lanServerDetector = null;
		}

		this.pinger.removeAll();
		this.serverSelectionList.removed();
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
			ServerData serverData = this.servers.unhide(this.editingServer.ip);
			if (serverData != null) {
				serverData.copyNameIconFrom(this.editingServer);
				this.servers.save();
			} else {
				this.servers.add(this.editingServer, false);
				this.servers.save();
			}

			this.serverSelectionList.setSelected(null);
			this.serverSelectionList.updateOnlineServers(this.servers);
		}

		this.minecraft.setScreen(this);
	}

	private void directJoinCallback(boolean bl) {
		if (bl) {
			ServerData serverData = this.servers.get(this.editingServer.ip);
			if (serverData == null) {
				this.servers.add(this.editingServer, true);
				this.servers.save();
				this.join(this.editingServer);
			} else {
				this.join(serverData);
			}
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
			if (CommonInputs.selected(i)) {
				this.joinSelectedServer();
				return true;
			} else {
				return this.serverSelectionList.keyPressed(i, j, k);
			}
		} else {
			return false;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.toolTip = null;
		this.serverSelectionList.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
		if (this.toolTip != null) {
			guiGraphics.renderComponentTooltip(this.font, this.toolTip, i, j);
		}
	}

	public void joinSelectedServer() {
		ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
		if (entry instanceof ServerSelectionList.OnlineServerEntry) {
			this.join(((ServerSelectionList.OnlineServerEntry)entry).getServerData());
		} else if (entry instanceof ServerSelectionList.NetworkServerEntry) {
			LanServer lanServer = ((ServerSelectionList.NetworkServerEntry)entry).getServerData();
			this.join(new ServerData(lanServer.getMotd(), lanServer.getAddress(), ServerData.Type.LAN));
		}
	}

	private void join(ServerData serverData) {
		ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(serverData.ip), serverData, false);
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
