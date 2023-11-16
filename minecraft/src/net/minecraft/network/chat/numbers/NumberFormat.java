package net.minecraft.network.chat.numbers;

import net.minecraft.network.chat.MutableComponent;

public interface NumberFormat {
	MutableComponent format(int i);

	NumberFormatType<? extends NumberFormat> type();
}
