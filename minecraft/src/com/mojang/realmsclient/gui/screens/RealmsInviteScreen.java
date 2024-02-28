package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsInviteScreen extends RealmsScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component TITLE = Component.translatable("mco.configure.world.buttons.invite");
	private static final Component NAME_LABEL = Component.translatable("mco.configure.world.invite.profile.name").withColor(-6250336);
	private static final Component INVITING_PLAYER_TEXT = Component.translatable("mco.configure.world.players.inviting").withColor(-6250336);
	private static final Component NO_SUCH_PLAYER_ERROR_TEXT = Component.translatable("mco.configure.world.players.error").withColor(-65536);
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private EditBox profileName;
	private Button inviteButton;
	private final RealmsServer serverData;
	private final RealmsConfigureWorldScreen configureScreen;
	private final Screen lastScreen;
	@Nullable
	private Component message;

	public RealmsInviteScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, Screen screen, RealmsServer realmsServer) {
		super(TITLE);
		this.configureScreen = realmsConfigureWorldScreen;
		this.lastScreen = screen;
		this.serverData = realmsServer;
	}

	@Override
	public void init() {
		this.layout.addTitleHeader(TITLE, this.font);
		LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical().spacing(8));
		this.profileName = new EditBox(this.minecraft.font, 200, 20, Component.translatable("mco.configure.world.invite.profile.name"));
		linearLayout.addChild(CommonLayouts.labeledElement(this.font, this.profileName, NAME_LABEL));
		this.inviteButton = linearLayout.addChild(Button.builder(TITLE, button -> this.onInvite()).width(200).build());
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(200).build());
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.profileName);
	}

	private void onInvite() {
		if (StringUtil.isBlank(this.profileName.getValue())) {
			this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
		} else {
			long l = this.serverData.id;
			String string = this.profileName.getValue().trim();
			this.inviteButton.active = false;
			this.profileName.setEditable(false);
			this.showMessage(INVITING_PLAYER_TEXT);
			CompletableFuture.supplyAsync(() -> {
				try {
					return RealmsClient.create().invite(l, string);
				} catch (Exception var4) {
					LOGGER.error("Couldn't invite user");
					return null;
				}
			}, Util.ioPool()).thenAcceptAsync(realmsServer -> {
				if (realmsServer != null) {
					this.serverData.players = realmsServer.players;
					this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
				} else {
					this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
				}

				this.profileName.setEditable(true);
				this.inviteButton.active = true;
			}, this.screenExecutor);
		}
	}

	private void showMessage(Component component) {
		this.message = component;
		this.minecraft.getNarrator().sayNow(component);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		if (this.message != null) {
			guiGraphics.drawCenteredString(this.font, this.message, this.width / 2, this.inviteButton.getY() + this.inviteButton.getHeight() + 8, -1);
		}
	}
}
