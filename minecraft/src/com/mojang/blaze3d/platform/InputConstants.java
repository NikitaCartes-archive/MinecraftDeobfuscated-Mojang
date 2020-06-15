package com.mojang.blaze3d.platform;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.LazyLoadedValue;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

@Environment(EnvType.CLIENT)
public class InputConstants {
	@Nullable
	private static final MethodHandle glfwRawMouseMotionSupported;
	private static final int GLFW_RAW_MOUSE_MOTION;
	public static final InputConstants.Key UNKNOWN;

	public static InputConstants.Key getKey(int i, int j) {
		return i == -1 ? InputConstants.Type.SCANCODE.getOrCreate(j) : InputConstants.Type.KEYSYM.getOrCreate(i);
	}

	public static InputConstants.Key getKey(String string) {
		if (InputConstants.Key.NAME_MAP.containsKey(string)) {
			return (InputConstants.Key)InputConstants.Key.NAME_MAP.get(string);
		} else {
			for (InputConstants.Type type : InputConstants.Type.values()) {
				if (string.startsWith(type.defaultPrefix)) {
					String string2 = string.substring(type.defaultPrefix.length() + 1);
					return type.getOrCreate(Integer.parseInt(string2));
				}
			}

			throw new IllegalArgumentException("Unknown key name: " + string);
		}
	}

	public static boolean isKeyDown(long l, int i) {
		return GLFW.glfwGetKey(l, i) == 1;
	}

	public static void setupKeyboardCallbacks(long l, GLFWKeyCallbackI gLFWKeyCallbackI, GLFWCharModsCallbackI gLFWCharModsCallbackI) {
		GLFW.glfwSetKeyCallback(l, gLFWKeyCallbackI);
		GLFW.glfwSetCharModsCallback(l, gLFWCharModsCallbackI);
	}

	public static void setupMouseCallbacks(
		long l,
		GLFWCursorPosCallbackI gLFWCursorPosCallbackI,
		GLFWMouseButtonCallbackI gLFWMouseButtonCallbackI,
		GLFWScrollCallbackI gLFWScrollCallbackI,
		GLFWDropCallbackI gLFWDropCallbackI
	) {
		GLFW.glfwSetCursorPosCallback(l, gLFWCursorPosCallbackI);
		GLFW.glfwSetMouseButtonCallback(l, gLFWMouseButtonCallbackI);
		GLFW.glfwSetScrollCallback(l, gLFWScrollCallbackI);
		GLFW.glfwSetDropCallback(l, gLFWDropCallbackI);
	}

	public static void grabOrReleaseMouse(long l, int i, double d, double e) {
		GLFW.glfwSetCursorPos(l, d, e);
		GLFW.glfwSetInputMode(l, 208897, i);
	}

	public static boolean isRawMouseInputSupported() {
		try {
			return glfwRawMouseMotionSupported != null && (boolean)glfwRawMouseMotionSupported.invokeExact();
		} catch (Throwable var1) {
			throw new RuntimeException(var1);
		}
	}

	public static void updateRawMouseInput(long l, boolean bl) {
		if (isRawMouseInputSupported()) {
			GLFW.glfwSetInputMode(l, GLFW_RAW_MOUSE_MOTION, bl ? 1 : 0);
		}
	}

	static {
		Lookup lookup = MethodHandles.lookup();
		MethodType methodType = MethodType.methodType(boolean.class);
		MethodHandle methodHandle = null;
		int i = 0;

		try {
			methodHandle = lookup.findStatic(GLFW.class, "glfwRawMouseMotionSupported", methodType);
			MethodHandle methodHandle2 = lookup.findStaticGetter(GLFW.class, "GLFW_RAW_MOUSE_MOTION", int.class);
			i = (int)methodHandle2.invokeExact();
		} catch (NoSuchFieldException | NoSuchMethodException var5) {
		} catch (Throwable var6) {
			throw new RuntimeException(var6);
		}

		glfwRawMouseMotionSupported = methodHandle;
		GLFW_RAW_MOUSE_MOTION = i;
		UNKNOWN = InputConstants.Type.KEYSYM.getOrCreate(-1);
	}

	@Environment(EnvType.CLIENT)
	public static final class Key {
		private final String name;
		private final InputConstants.Type type;
		private final int value;
		private final LazyLoadedValue<Component> displayName;
		private static final Map<String, InputConstants.Key> NAME_MAP = Maps.<String, InputConstants.Key>newHashMap();

		private Key(String string, InputConstants.Type type, int i) {
			this.name = string;
			this.type = type;
			this.value = i;
			this.displayName = new LazyLoadedValue<>(() -> (Component)type.displayTextSupplier.apply(i, string));
			NAME_MAP.put(string, this);
		}

		public InputConstants.Type getType() {
			return this.type;
		}

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}

		public Component getDisplayName() {
			return this.displayName.get();
		}

		public OptionalInt getNumericKeyValue() {
			if (this.value >= 48 && this.value <= 57) {
				return OptionalInt.of(this.value - 48);
			} else {
				return this.value >= 320 && this.value <= 329 ? OptionalInt.of(this.value - 320) : OptionalInt.empty();
			}
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				InputConstants.Key key = (InputConstants.Key)object;
				return this.value == key.value && this.type == key.type;
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.type, this.value});
		}

		public String toString() {
			return this.name;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		KEYSYM("key.keyboard", (integer, string) -> {
			String string2 = GLFW.glfwGetKeyName(integer, -1);
			return (Component)(string2 != null ? new TextComponent(string2) : new TranslatableComponent(string));
		}),
		SCANCODE("scancode", (integer, string) -> {
			String string2 = GLFW.glfwGetKeyName(-1, integer);
			return (Component)(string2 != null ? new TextComponent(string2) : new TranslatableComponent(string));
		}),
		MOUSE(
			"key.mouse",
			(integer, string) -> Language.getInstance().has(string) ? new TranslatableComponent(string) : new TranslatableComponent("key.mouse", integer + 1)
		);

		private final Int2ObjectMap<InputConstants.Key> map = new Int2ObjectOpenHashMap<>();
		private final String defaultPrefix;
		private final BiFunction<Integer, String, Component> displayTextSupplier;

		private static void addKey(InputConstants.Type type, String string, int i) {
			InputConstants.Key key = new InputConstants.Key(string, type, i);
			type.map.put(i, key);
		}

		private Type(String string2, BiFunction<Integer, String, Component> biFunction) {
			this.defaultPrefix = string2;
			this.displayTextSupplier = biFunction;
		}

		public InputConstants.Key getOrCreate(int i) {
			return this.map.computeIfAbsent(i, ix -> {
				int j = ix;
				if (this == MOUSE) {
					j = ix + 1;
				}

				String string = this.defaultPrefix + "." + j;
				return new InputConstants.Key(string, this, ix);
			});
		}

		static {
			addKey(KEYSYM, "key.keyboard.unknown", -1);
			addKey(MOUSE, "key.mouse.left", 0);
			addKey(MOUSE, "key.mouse.right", 1);
			addKey(MOUSE, "key.mouse.middle", 2);
			addKey(MOUSE, "key.mouse.4", 3);
			addKey(MOUSE, "key.mouse.5", 4);
			addKey(MOUSE, "key.mouse.6", 5);
			addKey(MOUSE, "key.mouse.7", 6);
			addKey(MOUSE, "key.mouse.8", 7);
			addKey(KEYSYM, "key.keyboard.0", 48);
			addKey(KEYSYM, "key.keyboard.1", 49);
			addKey(KEYSYM, "key.keyboard.2", 50);
			addKey(KEYSYM, "key.keyboard.3", 51);
			addKey(KEYSYM, "key.keyboard.4", 52);
			addKey(KEYSYM, "key.keyboard.5", 53);
			addKey(KEYSYM, "key.keyboard.6", 54);
			addKey(KEYSYM, "key.keyboard.7", 55);
			addKey(KEYSYM, "key.keyboard.8", 56);
			addKey(KEYSYM, "key.keyboard.9", 57);
			addKey(KEYSYM, "key.keyboard.a", 65);
			addKey(KEYSYM, "key.keyboard.b", 66);
			addKey(KEYSYM, "key.keyboard.c", 67);
			addKey(KEYSYM, "key.keyboard.d", 68);
			addKey(KEYSYM, "key.keyboard.e", 69);
			addKey(KEYSYM, "key.keyboard.f", 70);
			addKey(KEYSYM, "key.keyboard.g", 71);
			addKey(KEYSYM, "key.keyboard.h", 72);
			addKey(KEYSYM, "key.keyboard.i", 73);
			addKey(KEYSYM, "key.keyboard.j", 74);
			addKey(KEYSYM, "key.keyboard.k", 75);
			addKey(KEYSYM, "key.keyboard.l", 76);
			addKey(KEYSYM, "key.keyboard.m", 77);
			addKey(KEYSYM, "key.keyboard.n", 78);
			addKey(KEYSYM, "key.keyboard.o", 79);
			addKey(KEYSYM, "key.keyboard.p", 80);
			addKey(KEYSYM, "key.keyboard.q", 81);
			addKey(KEYSYM, "key.keyboard.r", 82);
			addKey(KEYSYM, "key.keyboard.s", 83);
			addKey(KEYSYM, "key.keyboard.t", 84);
			addKey(KEYSYM, "key.keyboard.u", 85);
			addKey(KEYSYM, "key.keyboard.v", 86);
			addKey(KEYSYM, "key.keyboard.w", 87);
			addKey(KEYSYM, "key.keyboard.x", 88);
			addKey(KEYSYM, "key.keyboard.y", 89);
			addKey(KEYSYM, "key.keyboard.z", 90);
			addKey(KEYSYM, "key.keyboard.f1", 290);
			addKey(KEYSYM, "key.keyboard.f2", 291);
			addKey(KEYSYM, "key.keyboard.f3", 292);
			addKey(KEYSYM, "key.keyboard.f4", 293);
			addKey(KEYSYM, "key.keyboard.f5", 294);
			addKey(KEYSYM, "key.keyboard.f6", 295);
			addKey(KEYSYM, "key.keyboard.f7", 296);
			addKey(KEYSYM, "key.keyboard.f8", 297);
			addKey(KEYSYM, "key.keyboard.f9", 298);
			addKey(KEYSYM, "key.keyboard.f10", 299);
			addKey(KEYSYM, "key.keyboard.f11", 300);
			addKey(KEYSYM, "key.keyboard.f12", 301);
			addKey(KEYSYM, "key.keyboard.f13", 302);
			addKey(KEYSYM, "key.keyboard.f14", 303);
			addKey(KEYSYM, "key.keyboard.f15", 304);
			addKey(KEYSYM, "key.keyboard.f16", 305);
			addKey(KEYSYM, "key.keyboard.f17", 306);
			addKey(KEYSYM, "key.keyboard.f18", 307);
			addKey(KEYSYM, "key.keyboard.f19", 308);
			addKey(KEYSYM, "key.keyboard.f20", 309);
			addKey(KEYSYM, "key.keyboard.f21", 310);
			addKey(KEYSYM, "key.keyboard.f22", 311);
			addKey(KEYSYM, "key.keyboard.f23", 312);
			addKey(KEYSYM, "key.keyboard.f24", 313);
			addKey(KEYSYM, "key.keyboard.f25", 314);
			addKey(KEYSYM, "key.keyboard.num.lock", 282);
			addKey(KEYSYM, "key.keyboard.keypad.0", 320);
			addKey(KEYSYM, "key.keyboard.keypad.1", 321);
			addKey(KEYSYM, "key.keyboard.keypad.2", 322);
			addKey(KEYSYM, "key.keyboard.keypad.3", 323);
			addKey(KEYSYM, "key.keyboard.keypad.4", 324);
			addKey(KEYSYM, "key.keyboard.keypad.5", 325);
			addKey(KEYSYM, "key.keyboard.keypad.6", 326);
			addKey(KEYSYM, "key.keyboard.keypad.7", 327);
			addKey(KEYSYM, "key.keyboard.keypad.8", 328);
			addKey(KEYSYM, "key.keyboard.keypad.9", 329);
			addKey(KEYSYM, "key.keyboard.keypad.add", 334);
			addKey(KEYSYM, "key.keyboard.keypad.decimal", 330);
			addKey(KEYSYM, "key.keyboard.keypad.enter", 335);
			addKey(KEYSYM, "key.keyboard.keypad.equal", 336);
			addKey(KEYSYM, "key.keyboard.keypad.multiply", 332);
			addKey(KEYSYM, "key.keyboard.keypad.divide", 331);
			addKey(KEYSYM, "key.keyboard.keypad.subtract", 333);
			addKey(KEYSYM, "key.keyboard.down", 264);
			addKey(KEYSYM, "key.keyboard.left", 263);
			addKey(KEYSYM, "key.keyboard.right", 262);
			addKey(KEYSYM, "key.keyboard.up", 265);
			addKey(KEYSYM, "key.keyboard.apostrophe", 39);
			addKey(KEYSYM, "key.keyboard.backslash", 92);
			addKey(KEYSYM, "key.keyboard.comma", 44);
			addKey(KEYSYM, "key.keyboard.equal", 61);
			addKey(KEYSYM, "key.keyboard.grave.accent", 96);
			addKey(KEYSYM, "key.keyboard.left.bracket", 91);
			addKey(KEYSYM, "key.keyboard.minus", 45);
			addKey(KEYSYM, "key.keyboard.period", 46);
			addKey(KEYSYM, "key.keyboard.right.bracket", 93);
			addKey(KEYSYM, "key.keyboard.semicolon", 59);
			addKey(KEYSYM, "key.keyboard.slash", 47);
			addKey(KEYSYM, "key.keyboard.space", 32);
			addKey(KEYSYM, "key.keyboard.tab", 258);
			addKey(KEYSYM, "key.keyboard.left.alt", 342);
			addKey(KEYSYM, "key.keyboard.left.control", 341);
			addKey(KEYSYM, "key.keyboard.left.shift", 340);
			addKey(KEYSYM, "key.keyboard.left.win", 343);
			addKey(KEYSYM, "key.keyboard.right.alt", 346);
			addKey(KEYSYM, "key.keyboard.right.control", 345);
			addKey(KEYSYM, "key.keyboard.right.shift", 344);
			addKey(KEYSYM, "key.keyboard.right.win", 347);
			addKey(KEYSYM, "key.keyboard.enter", 257);
			addKey(KEYSYM, "key.keyboard.escape", 256);
			addKey(KEYSYM, "key.keyboard.backspace", 259);
			addKey(KEYSYM, "key.keyboard.delete", 261);
			addKey(KEYSYM, "key.keyboard.end", 269);
			addKey(KEYSYM, "key.keyboard.home", 268);
			addKey(KEYSYM, "key.keyboard.insert", 260);
			addKey(KEYSYM, "key.keyboard.page.down", 267);
			addKey(KEYSYM, "key.keyboard.page.up", 266);
			addKey(KEYSYM, "key.keyboard.caps.lock", 280);
			addKey(KEYSYM, "key.keyboard.pause", 284);
			addKey(KEYSYM, "key.keyboard.scroll.lock", 281);
			addKey(KEYSYM, "key.keyboard.menu", 348);
			addKey(KEYSYM, "key.keyboard.print.screen", 283);
			addKey(KEYSYM, "key.keyboard.world.1", 161);
			addKey(KEYSYM, "key.keyboard.world.2", 162);
		}
	}
}
