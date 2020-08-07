package net.minecraft.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class KeyMapping implements Comparable<KeyMapping> {
	private static final Map<String, KeyMapping> ALL = Maps.<String, KeyMapping>newHashMap();
	private static final Map<InputConstants.Key, KeyMapping> MAP = Maps.<InputConstants.Key, KeyMapping>newHashMap();
	private static final Set<String> CATEGORIES = Sets.<String>newHashSet();
	private static final Map<String, Integer> CATEGORY_SORT_ORDER = Util.make(Maps.<String, Integer>newHashMap(), hashMap -> {
		hashMap.put("key.categories.movement", 1);
		hashMap.put("key.categories.gameplay", 2);
		hashMap.put("key.categories.inventory", 3);
		hashMap.put("key.categories.creative", 4);
		hashMap.put("key.categories.multiplayer", 5);
		hashMap.put("key.categories.ui", 6);
		hashMap.put("key.categories.misc", 7);
	});
	private final String name;
	private final InputConstants.Key defaultKey;
	private final String category;
	private InputConstants.Key key;
	private boolean isDown;
	private int clickCount;

	public static void click(InputConstants.Key key) {
		KeyMapping keyMapping = (KeyMapping)MAP.get(key);
		if (keyMapping != null) {
			keyMapping.clickCount++;
		}
	}

	public static void set(InputConstants.Key key, boolean bl) {
		KeyMapping keyMapping = (KeyMapping)MAP.get(key);
		if (keyMapping != null) {
			keyMapping.setDown(bl);
		}
	}

	public static void setAll() {
		for (KeyMapping keyMapping : ALL.values()) {
			if (keyMapping.key.getType() == InputConstants.Type.KEYSYM && keyMapping.key.getValue() != InputConstants.UNKNOWN.getValue()) {
				keyMapping.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyMapping.key.getValue()));
			}
		}
	}

	public static void releaseAll() {
		for (KeyMapping keyMapping : ALL.values()) {
			keyMapping.release();
		}
	}

	public static void resetMapping() {
		MAP.clear();

		for (KeyMapping keyMapping : ALL.values()) {
			MAP.put(keyMapping.key, keyMapping);
		}
	}

	public KeyMapping(String string, int i, String string2) {
		this(string, InputConstants.Type.KEYSYM, i, string2);
	}

	public KeyMapping(String string, InputConstants.Type type, int i, String string2) {
		this.name = string;
		this.key = type.getOrCreate(i);
		this.defaultKey = this.key;
		this.category = string2;
		ALL.put(string, this);
		MAP.put(this.key, this);
		CATEGORIES.add(string2);
	}

	public boolean isDown() {
		return this.isDown;
	}

	public String getCategory() {
		return this.category;
	}

	public boolean consumeClick() {
		if (this.clickCount == 0) {
			return false;
		} else {
			this.clickCount--;
			return true;
		}
	}

	private void release() {
		this.clickCount = 0;
		this.setDown(false);
	}

	public String getName() {
		return this.name;
	}

	public InputConstants.Key getDefaultKey() {
		return this.defaultKey;
	}

	public void setKey(InputConstants.Key key) {
		this.key = key;
	}

	public int compareTo(KeyMapping keyMapping) {
		return this.category.equals(keyMapping.category)
			? I18n.get(this.name).compareTo(I18n.get(keyMapping.name))
			: ((Integer)CATEGORY_SORT_ORDER.get(this.category)).compareTo((Integer)CATEGORY_SORT_ORDER.get(keyMapping.category));
	}

	public static Supplier<Component> createNameSupplier(String string) {
		KeyMapping keyMapping = (KeyMapping)ALL.get(string);
		return keyMapping == null ? () -> new TranslatableComponent(string) : keyMapping::getTranslatedKeyMessage;
	}

	public boolean same(KeyMapping keyMapping) {
		return this.key.equals(keyMapping.key);
	}

	public boolean isUnbound() {
		return this.key.equals(InputConstants.UNKNOWN);
	}

	public boolean matches(int i, int j) {
		return i == InputConstants.UNKNOWN.getValue()
			? this.key.getType() == InputConstants.Type.SCANCODE && this.key.getValue() == j
			: this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() == i;
	}

	public boolean matchesMouse(int i) {
		return this.key.getType() == InputConstants.Type.MOUSE && this.key.getValue() == i;
	}

	public Component getTranslatedKeyMessage() {
		return this.key.getDisplayName();
	}

	public boolean isDefault() {
		return this.key.equals(this.defaultKey);
	}

	public String saveString() {
		return this.key.getName();
	}

	public void setDown(boolean bl) {
		this.isDown = bl;
	}
}
