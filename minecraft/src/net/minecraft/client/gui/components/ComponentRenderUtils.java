package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

@Environment(EnvType.CLIENT)
public class ComponentRenderUtils {
	public static String stripColor(String string, boolean bl) {
		return !bl && !Minecraft.getInstance().options.chatColors ? ChatFormatting.stripFormatting(string) : string;
	}

	public static List<Component> wrapComponents(Component component, int i, Font font, boolean bl, boolean bl2) {
		int j = 0;
		Component component2 = new TextComponent("");
		List<Component> list = Lists.<Component>newArrayList();
		List<Component> list2 = Lists.<Component>newArrayList(component);

		for (int k = 0; k < list2.size(); k++) {
			Component component3 = (Component)list2.get(k);
			String string = component3.getContents();
			boolean bl3 = false;
			if (string.contains("\n")) {
				int l = string.indexOf(10);
				String string2 = string.substring(l + 1);
				string = string.substring(0, l + 1);
				Component component4 = new TextComponent(string2).setStyle(component3.getStyle().copy());
				list2.add(k + 1, component4);
				bl3 = true;
			}

			String string3 = stripColor(component3.getStyle().getLegacyFormatCodes() + string, bl2);
			String string2 = string3.endsWith("\n") ? string3.substring(0, string3.length() - 1) : string3;
			int m = font.width(string2);
			Component component5 = new TextComponent(string2).setStyle(component3.getStyle().copy());
			if (j + m > i) {
				String string4 = font.substrByWidth(string3, i - j, false);
				String string5 = string4.length() < string3.length() ? string3.substring(string4.length()) : null;
				if (string5 != null && !string5.isEmpty()) {
					int n = string5.charAt(0) != ' ' ? string4.lastIndexOf(32) : string4.length();
					if (n >= 0 && font.width(string3.substring(0, n)) > 0) {
						string4 = string3.substring(0, n);
						if (bl) {
							n++;
						}

						string5 = string3.substring(n);
					} else if (j > 0 && !string3.contains(" ")) {
						string4 = "";
						string5 = string3;
					}

					Component component6 = new TextComponent(string5).setStyle(component3.getStyle().copy());
					list2.add(k + 1, component6);
				}

				m = font.width(string4);
				component5 = new TextComponent(string4);
				component5.setStyle(component3.getStyle().copy());
				bl3 = true;
			}

			if (j + m <= i) {
				j += m;
				component2.append(component5);
			} else {
				bl3 = true;
			}

			if (bl3) {
				list.add(component2);
				j = 0;
				component2 = new TextComponent("");
			}
		}

		list.add(component2);
		return list;
	}
}
