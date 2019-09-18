package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ChatComponent extends GuiComponent {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private final List<String> recentChat = Lists.<String>newArrayList();
	private final List<GuiMessage> allMessages = Lists.<GuiMessage>newArrayList();
	private final List<GuiMessage> trimmedMessages = Lists.<GuiMessage>newArrayList();
	private int chatScrollbarPos;
	private boolean newMessageSinceScroll;

	public ChatComponent(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render(int i) {
		if (this.minecraft.options.chatVisibility != ChatVisiblity.HIDDEN) {
			int j = this.getLinesPerPage();
			int k = this.trimmedMessages.size();
			if (k > 0) {
				boolean bl = false;
				if (this.isChatFocused()) {
					bl = true;
				}

				double d = this.getScale();
				int l = Mth.ceil((double)this.getWidth() / d);
				RenderSystem.pushMatrix();
				RenderSystem.translatef(2.0F, 8.0F, 0.0F);
				RenderSystem.scaled(d, d, 1.0);
				double e = this.minecraft.options.chatOpacity * 0.9F + 0.1F;
				double f = this.minecraft.options.textBackgroundOpacity;
				int m = 0;

				for (int n = 0; n + this.chatScrollbarPos < this.trimmedMessages.size() && n < j; n++) {
					GuiMessage guiMessage = (GuiMessage)this.trimmedMessages.get(n + this.chatScrollbarPos);
					if (guiMessage != null) {
						int o = i - guiMessage.getAddedTime();
						if (o < 200 || bl) {
							double g = bl ? 1.0 : getTimeFactor(o);
							int p = (int)(255.0 * g * e);
							int q = (int)(255.0 * g * f);
							m++;
							if (p > 3) {
								int r = 0;
								int s = -n * 9;
								fill(-2, s - 9, 0 + l + 4, s, q << 24);
								String string = guiMessage.getMessage().getColoredString();
								RenderSystem.enableBlend();
								this.minecraft.font.drawShadow(string, 0.0F, (float)(s - 8), 16777215 + (p << 24));
								RenderSystem.disableAlphaTest();
								RenderSystem.disableBlend();
							}
						}
					}
				}

				if (bl) {
					int nx = 9;
					RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
					int t = k * nx + k;
					int o = m * nx + m;
					int u = this.chatScrollbarPos * o / k;
					int v = o * o / t;
					if (t != o) {
						int p = u > 0 ? 170 : 96;
						int q = this.newMessageSinceScroll ? 13382451 : 3355562;
						fill(0, -u, 2, -u - v, q + (p << 24));
						fill(2, -u, 1, -u - v, 13421772 + (p << 24));
					}
				}

				RenderSystem.popMatrix();
			}
		}
	}

	private static double getTimeFactor(int i) {
		double d = (double)i / 200.0;
		d = 1.0 - d;
		d *= 10.0;
		d = Mth.clamp(d, 0.0, 1.0);
		return d * d;
	}

	public void clearMessages(boolean bl) {
		this.trimmedMessages.clear();
		this.allMessages.clear();
		if (bl) {
			this.recentChat.clear();
		}
	}

	public void addMessage(Component component) {
		this.addMessage(component, 0);
	}

	public void addMessage(Component component, int i) {
		this.addMessage(component, i, this.minecraft.gui.getGuiTicks(), false);
		LOGGER.info("[CHAT] {}", component.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
	}

	private void addMessage(Component component, int i, int j, boolean bl) {
		if (i != 0) {
			this.removeById(i);
		}

		int k = Mth.floor((double)this.getWidth() / this.getScale());
		List<Component> list = ComponentRenderUtils.wrapComponents(component, k, this.minecraft.font, false, false);
		boolean bl2 = this.isChatFocused();

		for (Component component2 : list) {
			if (bl2 && this.chatScrollbarPos > 0) {
				this.newMessageSinceScroll = true;
				this.scrollChat(1.0);
			}

			this.trimmedMessages.add(0, new GuiMessage(j, component2, i));
		}

		while (this.trimmedMessages.size() > 100) {
			this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
		}

		if (!bl) {
			this.allMessages.add(0, new GuiMessage(j, component, i));

			while (this.allMessages.size() > 100) {
				this.allMessages.remove(this.allMessages.size() - 1);
			}
		}
	}

	public void rescaleChat() {
		this.trimmedMessages.clear();
		this.resetChatScroll();

		for (int i = this.allMessages.size() - 1; i >= 0; i--) {
			GuiMessage guiMessage = (GuiMessage)this.allMessages.get(i);
			this.addMessage(guiMessage.getMessage(), guiMessage.getId(), guiMessage.getAddedTime(), true);
		}
	}

	public List<String> getRecentChat() {
		return this.recentChat;
	}

	public void addRecentChat(String string) {
		if (this.recentChat.isEmpty() || !((String)this.recentChat.get(this.recentChat.size() - 1)).equals(string)) {
			this.recentChat.add(string);
		}
	}

	public void resetChatScroll() {
		this.chatScrollbarPos = 0;
		this.newMessageSinceScroll = false;
	}

	public void scrollChat(double d) {
		this.chatScrollbarPos = (int)((double)this.chatScrollbarPos + d);
		int i = this.trimmedMessages.size();
		if (this.chatScrollbarPos > i - this.getLinesPerPage()) {
			this.chatScrollbarPos = i - this.getLinesPerPage();
		}

		if (this.chatScrollbarPos <= 0) {
			this.chatScrollbarPos = 0;
			this.newMessageSinceScroll = false;
		}
	}

	@Nullable
	public Component getClickedComponentAt(double d, double e) {
		if (!this.isChatFocused()) {
			return null;
		} else {
			double f = this.getScale();
			double g = d - 2.0;
			double h = (double)this.minecraft.getWindow().getGuiScaledHeight() - e - 40.0;
			g = (double)Mth.floor(g / f);
			h = (double)Mth.floor(h / f);
			if (!(g < 0.0) && !(h < 0.0)) {
				int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
				if (g <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && h < (double)(9 * i + i)) {
					int j = (int)(h / 9.0 + (double)this.chatScrollbarPos);
					if (j >= 0 && j < this.trimmedMessages.size()) {
						GuiMessage guiMessage = (GuiMessage)this.trimmedMessages.get(j);
						int k = 0;

						for (Component component : guiMessage.getMessage()) {
							if (component instanceof TextComponent) {
								k += this.minecraft.font.width(ComponentRenderUtils.stripColor(((TextComponent)component).getText(), false));
								if ((double)k > g) {
									return component;
								}
							}
						}
					}

					return null;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public boolean isChatFocused() {
		return this.minecraft.screen instanceof ChatScreen;
	}

	public void removeById(int i) {
		Iterator<GuiMessage> iterator = this.trimmedMessages.iterator();

		while (iterator.hasNext()) {
			GuiMessage guiMessage = (GuiMessage)iterator.next();
			if (guiMessage.getId() == i) {
				iterator.remove();
			}
		}

		iterator = this.allMessages.iterator();

		while (iterator.hasNext()) {
			GuiMessage guiMessage = (GuiMessage)iterator.next();
			if (guiMessage.getId() == i) {
				iterator.remove();
				break;
			}
		}
	}

	public int getWidth() {
		return getWidth(this.minecraft.options.chatWidth);
	}

	public int getHeight() {
		return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused : this.minecraft.options.chatHeightUnfocused);
	}

	public double getScale() {
		return this.minecraft.options.chatScale;
	}

	public static int getWidth(double d) {
		int i = 320;
		int j = 40;
		return Mth.floor(d * 280.0 + 40.0);
	}

	public static int getHeight(double d) {
		int i = 180;
		int j = 20;
		return Mth.floor(d * 160.0 + 20.0);
	}

	public int getLinesPerPage() {
		return this.getHeight() / 9;
	}
}
