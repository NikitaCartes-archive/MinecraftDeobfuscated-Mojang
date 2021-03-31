/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public class KeyMapping
implements Comparable<KeyMapping> {
    private static final Map<String, KeyMapping> ALL = Maps.newHashMap();
    private static final Map<InputConstants.Key, KeyMapping> MAP = Maps.newHashMap();
    private static final Set<String> CATEGORIES = Sets.newHashSet();
    public static final String CATEGORY_MOVEMENT = "key.categories.movement";
    public static final String CATEGORY_MISC = "key.categories.misc";
    public static final String CATEGORY_MULTIPLAYER = "key.categories.multiplayer";
    public static final String CATEGORY_GAMEPLAY = "key.categories.gameplay";
    public static final String CATEGORY_INVENTORY = "key.categories.inventory";
    public static final String CATEGORY_INTERFACE = "key.categories.ui";
    public static final String CATEGORY_CREATIVE = "key.categories.creative";
    private static final Map<String, Integer> CATEGORY_SORT_ORDER = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(CATEGORY_MOVEMENT, 1);
        hashMap.put(CATEGORY_GAMEPLAY, 2);
        hashMap.put(CATEGORY_INVENTORY, 3);
        hashMap.put(CATEGORY_CREATIVE, 4);
        hashMap.put(CATEGORY_MULTIPLAYER, 5);
        hashMap.put(CATEGORY_INTERFACE, 6);
        hashMap.put(CATEGORY_MISC, 7);
    });
    private final String name;
    private final InputConstants.Key defaultKey;
    private final String category;
    private InputConstants.Key key;
    private boolean isDown;
    private int clickCount;

    public static void click(InputConstants.Key key) {
        KeyMapping keyMapping = MAP.get(key);
        if (keyMapping != null) {
            ++keyMapping.clickCount;
        }
    }

    public static void set(InputConstants.Key key, boolean bl) {
        KeyMapping keyMapping = MAP.get(key);
        if (keyMapping != null) {
            keyMapping.setDown(bl);
        }
    }

    public static void setAll() {
        for (KeyMapping keyMapping : ALL.values()) {
            if (keyMapping.key.getType() != InputConstants.Type.KEYSYM || keyMapping.key.getValue() == InputConstants.UNKNOWN.getValue()) continue;
            keyMapping.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyMapping.key.getValue()));
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
        this.defaultKey = this.key = type.getOrCreate(i);
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
        }
        --this.clickCount;
        return true;
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

    @Override
    public int compareTo(KeyMapping keyMapping) {
        if (this.category.equals(keyMapping.category)) {
            return I18n.get(this.name, new Object[0]).compareTo(I18n.get(keyMapping.name, new Object[0]));
        }
        return CATEGORY_SORT_ORDER.get(this.category).compareTo(CATEGORY_SORT_ORDER.get(keyMapping.category));
    }

    public static Supplier<Component> createNameSupplier(String string) {
        KeyMapping keyMapping = ALL.get(string);
        if (keyMapping == null) {
            return () -> new TranslatableComponent(string);
        }
        return keyMapping::getTranslatedKeyMessage;
    }

    public boolean same(KeyMapping keyMapping) {
        return this.key.equals(keyMapping.key);
    }

    public boolean isUnbound() {
        return this.key.equals(InputConstants.UNKNOWN);
    }

    public boolean matches(int i, int j) {
        if (i == InputConstants.UNKNOWN.getValue()) {
            return this.key.getType() == InputConstants.Type.SCANCODE && this.key.getValue() == j;
        }
        return this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() == i;
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

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((KeyMapping)object);
    }
}

