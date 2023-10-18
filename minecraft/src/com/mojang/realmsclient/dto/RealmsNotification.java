package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsNotification {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final String NOTIFICATION_UUID = "notificationUuid";
	private static final String DISMISSABLE = "dismissable";
	private static final String SEEN = "seen";
	private static final String TYPE = "type";
	private static final String VISIT_URL = "visitUrl";
	private static final String INFO_POPUP = "infoPopup";
	static final Component BUTTON_TEXT_FALLBACK = Component.translatable("mco.notification.visitUrl.buttonText.default");
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

			return (RealmsNotification)(switch (string) {
				case "visitUrl" -> RealmsNotification.VisitUrl.parse(realmsNotification, jsonObject);
				case "infoPopup" -> RealmsNotification.InfoPopup.parse(realmsNotification, jsonObject);
				default -> realmsNotification;
			});
		}
	}

	@Environment(EnvType.CLIENT)
	public static class InfoPopup extends RealmsNotification {
		private static final String TITLE = "title";
		private static final String MESSAGE = "message";
		private static final String IMAGE = "image";
		private static final String URL_BUTTON = "urlButton";
		private final RealmsText title;
		private final RealmsText message;
		private final ResourceLocation image;
		@Nullable
		private final RealmsNotification.UrlButton urlButton;

		private InfoPopup(
			RealmsNotification realmsNotification,
			RealmsText realmsText,
			RealmsText realmsText2,
			ResourceLocation resourceLocation,
			@Nullable RealmsNotification.UrlButton urlButton
		) {
			super(realmsNotification.uuid, realmsNotification.dismissable, realmsNotification.seen, realmsNotification.type);
			this.title = realmsText;
			this.message = realmsText2;
			this.image = resourceLocation;
			this.urlButton = urlButton;
		}

		public static RealmsNotification.InfoPopup parse(RealmsNotification realmsNotification, JsonObject jsonObject) {
			RealmsText realmsText = JsonUtils.getRequired("title", jsonObject, RealmsText::parse);
			RealmsText realmsText2 = JsonUtils.getRequired("message", jsonObject, RealmsText::parse);
			ResourceLocation resourceLocation = new ResourceLocation(JsonUtils.getRequiredString("image", jsonObject));
			RealmsNotification.UrlButton urlButton = JsonUtils.getOptional("urlButton", jsonObject, RealmsNotification.UrlButton::parse);
			return new RealmsNotification.InfoPopup(realmsNotification, realmsText, realmsText2, resourceLocation, urlButton);
		}

		@Nullable
		public PopupScreen buildScreen(Screen screen, Consumer<UUID> consumer) {
			Component component = this.title.createComponent();
			if (component == null) {
				RealmsNotification.LOGGER.warn("Realms info popup had title with no available translation: {}", this.title);
				return null;
			} else {
				PopupScreen.Builder builder = new PopupScreen.Builder(screen, component)
					.setImage(this.image)
					.setMessage(this.message.createComponent(CommonComponents.EMPTY));
				if (this.urlButton != null) {
					builder.addButton(this.urlButton.urlText.createComponent(RealmsNotification.BUTTON_TEXT_FALLBACK), popupScreen -> {
						Minecraft minecraft = Minecraft.getInstance();
						minecraft.setScreen(new ConfirmLinkScreen(bl -> {
							if (bl) {
								Util.getPlatform().openUri(this.urlButton.url);
								minecraft.setScreen(screen);
							} else {
								minecraft.setScreen(popupScreen);
							}
						}, this.urlButton.url, true));
						consumer.accept(this.uuid());
					});
				}

				builder.addButton(CommonComponents.GUI_OK, popupScreen -> {
					popupScreen.onClose();
					consumer.accept(this.uuid());
				});
				builder.onClose(() -> consumer.accept(this.uuid()));
				return builder.build();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record UrlButton(String url, RealmsText urlText) {
		private static final String URL = "url";
		private static final String URL_TEXT = "urlText";

		public static RealmsNotification.UrlButton parse(JsonObject jsonObject) {
			String string = JsonUtils.getRequiredString("url", jsonObject);
			RealmsText realmsText = JsonUtils.getRequired("urlText", jsonObject, RealmsText::parse);
			return new RealmsNotification.UrlButton(string, realmsText);
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
			Component component = this.buttonText.createComponent(RealmsNotification.BUTTON_TEXT_FALLBACK);
			return Button.builder(component, ConfirmLinkScreen.confirmLink(screen, this.url)).build();
		}
	}
}
