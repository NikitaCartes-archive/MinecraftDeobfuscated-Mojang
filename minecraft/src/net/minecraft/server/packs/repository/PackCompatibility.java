package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.InclusiveRange;

public enum PackCompatibility {
	TOO_OLD("old"),
	TOO_NEW("new"),
	COMPATIBLE("compatible");

	private final Component description;
	private final Component confirmation;

	private PackCompatibility(final String string2) {
		this.description = Component.translatable("pack.incompatible." + string2).withStyle(ChatFormatting.GRAY);
		this.confirmation = Component.translatable("pack.incompatible.confirm." + string2);
	}

	public boolean isCompatible() {
		return this == COMPATIBLE;
	}

	public static PackCompatibility forVersion(InclusiveRange<Integer> inclusiveRange, int i) {
		if ((Integer)inclusiveRange.maxInclusive() < i) {
			return TOO_OLD;
		} else {
			return i < inclusiveRange.minInclusive() ? TOO_NEW : COMPATIBLE;
		}
	}

	public Component getDescription() {
		return this.description;
	}

	public Component getConfirmation() {
		return this.confirmation;
	}
}
