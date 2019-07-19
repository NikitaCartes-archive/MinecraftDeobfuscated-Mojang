package com.mojang.realmsclient.gui;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum ChatFormatting {
	BLACK('0'),
	DARK_BLUE('1'),
	DARK_GREEN('2'),
	DARK_AQUA('3'),
	DARK_RED('4'),
	DARK_PURPLE('5'),
	GOLD('6'),
	GRAY('7'),
	DARK_GRAY('8'),
	BLUE('9'),
	GREEN('a'),
	AQUA('b'),
	RED('c'),
	LIGHT_PURPLE('d'),
	YELLOW('e'),
	WHITE('f'),
	OBFUSCATED('k', true),
	BOLD('l', true),
	STRIKETHROUGH('m', true),
	UNDERLINE('n', true),
	ITALIC('o', true),
	RESET('r');

	private static final Map<Character, ChatFormatting> FORMATTING_BY_CHAR = (Map<Character, ChatFormatting>)Arrays.stream(values())
		.collect(Collectors.toMap(ChatFormatting::getChar, chatFormatting -> chatFormatting));
	private static final Map<String, ChatFormatting> FORMATTING_BY_NAME = (Map<String, ChatFormatting>)Arrays.stream(values())
		.collect(Collectors.toMap(ChatFormatting::getName, chatFormatting -> chatFormatting));
	private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
	private final char code;
	private final boolean isFormat;
	private final String toString;

	private ChatFormatting(char c) {
		this(c, false);
	}

	private ChatFormatting(char c, boolean bl) {
		this.code = c;
		this.isFormat = bl;
		this.toString = "§" + c;
	}

	public char getChar() {
		return this.code;
	}

	public String getName() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	public String toString() {
		return this.toString;
	}
}
