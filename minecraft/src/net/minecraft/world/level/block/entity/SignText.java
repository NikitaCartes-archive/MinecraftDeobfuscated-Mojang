package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public class SignText {
	private static final Codec<Component[]> LINES_CODEC = ExtraCodecs.FLAT_COMPONENT
		.listOf()
		.comapFlatMap(
			list -> Util.fixedSize(list, 4)
					.map(listx -> new Component[]{(Component)listx.get(0), (Component)listx.get(1), (Component)listx.get(2), (Component)listx.get(3)}),
			components -> List.of(components[0], components[1], components[2], components[3])
		);
	public static final Codec<SignText> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					LINES_CODEC.fieldOf("messages").forGetter(signText -> signText.messages),
					LINES_CODEC.optionalFieldOf("filtered_messages").forGetter(SignText::filteredMessages),
					DyeColor.CODEC.fieldOf("color").orElse(DyeColor.BLACK).forGetter(signText -> signText.color),
					Codec.BOOL.fieldOf("has_glowing_text").orElse(false).forGetter(signText -> signText.hasGlowingText)
				)
				.apply(instance, SignText::load)
	);
	public static final int LINES = 4;
	private final Component[] messages;
	private final Component[] filteredMessages;
	private final DyeColor color;
	private final boolean hasGlowingText;
	@Nullable
	private FormattedCharSequence[] renderMessages;
	private boolean renderMessagedFiltered;

	public SignText() {
		this(emptyMessages(), emptyMessages(), DyeColor.BLACK, false);
	}

	public SignText(Component[] components, Component[] components2, DyeColor dyeColor, boolean bl) {
		this.messages = components;
		this.filteredMessages = components2;
		this.color = dyeColor;
		this.hasGlowingText = bl;
	}

	private static Component[] emptyMessages() {
		return new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
	}

	private static SignText load(Component[] components, Optional<Component[]> optional, DyeColor dyeColor, boolean bl) {
		return new SignText(components, (Component[])optional.orElse((Component[])Arrays.copyOf(components, components.length)), dyeColor, bl);
	}

	public boolean hasGlowingText() {
		return this.hasGlowingText;
	}

	public SignText setHasGlowingText(boolean bl) {
		return bl == this.hasGlowingText ? this : new SignText(this.messages, this.filteredMessages, this.color, bl);
	}

	public DyeColor getColor() {
		return this.color;
	}

	public SignText setColor(DyeColor dyeColor) {
		return dyeColor == this.getColor() ? this : new SignText(this.messages, this.filteredMessages, dyeColor, this.hasGlowingText);
	}

	public Component getMessage(int i, boolean bl) {
		return this.getMessages(bl)[i];
	}

	public SignText setMessage(int i, Component component) {
		return this.setMessage(i, component, component);
	}

	public SignText setMessage(int i, Component component, Component component2) {
		Component[] components = (Component[])Arrays.copyOf(this.messages, this.messages.length);
		Component[] components2 = (Component[])Arrays.copyOf(this.filteredMessages, this.filteredMessages.length);
		components[i] = component;
		components2[i] = component2;
		return new SignText(components, components2, this.color, this.hasGlowingText);
	}

	public boolean hasMessage(Player player) {
		return Arrays.stream(this.getMessages(player.isTextFilteringEnabled())).anyMatch(component -> !component.getString().isEmpty());
	}

	public Component[] getMessages(boolean bl) {
		return bl ? this.filteredMessages : this.messages;
	}

	public FormattedCharSequence[] getRenderMessages(boolean bl, Function<Component, FormattedCharSequence> function) {
		if (this.renderMessages == null || this.renderMessagedFiltered != bl) {
			this.renderMessagedFiltered = bl;
			this.renderMessages = new FormattedCharSequence[4];

			for (int i = 0; i < 4; i++) {
				this.renderMessages[i] = (FormattedCharSequence)function.apply(this.getMessage(i, bl));
			}
		}

		return this.renderMessages;
	}

	private Optional<Component[]> filteredMessages() {
		for (int i = 0; i < 4; i++) {
			if (!this.filteredMessages[i].equals(this.messages[i])) {
				return Optional.of(this.filteredMessages);
			}
		}

		return Optional.empty();
	}

	public boolean hasAnyClickCommands(Player player) {
		for (Component component : this.getMessages(player.isTextFilteringEnabled())) {
			Style style = component.getStyle();
			ClickEvent clickEvent = style.getClickEvent();
			if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
				return true;
			}
		}

		return false;
	}
}
