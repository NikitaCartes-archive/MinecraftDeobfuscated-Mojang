package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsNotification {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String NOTIFICATION_UUID = "notificationUuid";
	private static final String DISMISSABLE = "dismissable";
	private static final String SEEN = "seen";
	private static final String TYPE = "type";
	private static final String VISIT_URL = "visitUrl";
	final UUID uuid;
	final boolean dismissable;
	final boolean seen;
	final String type;

	RealmsNotification(UUID uUID, boolean bl, boolean bl2, String string) {
		this.uuid = uUID;
		this.dismissable = bl;
		this.seen = bl2;
		this.type = string;
	}

	public boolean seen() {
		return this.seen;
	}

	public boolean dismissable() {
		return this.dismissable;
	}

	public UUID uuid() {
		return this.uuid;
	}

	public static List<RealmsNotification> parseList(String string) {
		List<RealmsNotification> list = new ArrayList();

		try {
			for (JsonElement jsonElement : JsonParser.parseString(string).getAsJsonObject().get("notifications").getAsJsonArray()) {
				list.add(parse(jsonElement.getAsJsonObject()));
			}
		} catch (Exception var5) {
			LOGGER.error("Could not parse list of RealmsNotifications", (Throwable)var5);
		}

		return list;
	}

	private static RealmsNotification parse(JsonObject jsonObject) {
		UUID uUID = JsonUtils.getUuidOr("notificationUuid", jsonObject, null);
		if (uUID == null) {
			throw new IllegalStateException("Missing required property notificationUuid");
		} else {
			boolean bl = JsonUtils.getBooleanOr("dismissable", jsonObject, true);
			boolean bl2 = JsonUtils.getBooleanOr("seen", jsonObject, false);
			String string = JsonUtils.getRequiredString("type", jsonObject);
			RealmsNotification realmsNotification = new RealmsNotification(uUID, bl, bl2, string);
			return (RealmsNotification)("visitUrl".equals(string) ? RealmsNotification.VisitUrl.parse(realmsNotification, jsonObject) : realmsNotification);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class VisitUrl extends RealmsNotification {
		private static final String URL = "url";
		private static final String BUTTON_TEXT = "buttonText";
		private static final String MESSAGE = "message";
		private final String url;
		private final RealmsText buttonText;
		private final RealmsText message;

		private VisitUrl(RealmsNotification realmsNotification, String string, RealmsText realmsText, RealmsText realmsText2) {
			super(realmsNotification.uuid, realmsNotification.dismissable, realmsNotification.seen, realmsNotification.type);
			this.url = string;
			this.buttonText = realmsText;
			this.message = realmsText2;
		}

		public static RealmsNotification.VisitUrl parse(RealmsNotification realmsNotification, JsonObject jsonObject) {
			String string = JsonUtils.getRequiredString("url", jsonObject);
			RealmsText realmsText = JsonUtils.getRequired("buttonText", jsonObject, RealmsText::parse);
			RealmsText realmsText2 = JsonUtils.getRequired("message", jsonObject, RealmsText::parse);
			return new RealmsNotification.VisitUrl(realmsNotification, string, realmsText, realmsText2);
		}

		public Component getMessage() {
			return this.message.createComponent(Component.translatable("mco.notification.visitUrl.message.default"));
		}

		public Button buildOpenLinkButton(Screen screen) {
			Component component = this.buttonText.createComponent(Component.translatable("mco.notification.visitUrl.buttonText.default"));
			return Button.builder(component, ConfirmLinkScreen.confirmLink(this.url, screen, true)).build();
		}
	}
}
