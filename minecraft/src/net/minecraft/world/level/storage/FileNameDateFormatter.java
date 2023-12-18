package net.minecraft.world.level.storage;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;

public class FileNameDateFormatter {
	public static DateTimeFormatter create() {
		return new DateTimeFormatterBuilder()
			.appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
			.appendLiteral('-')
			.appendValue(ChronoField.MONTH_OF_YEAR, 2)
			.appendLiteral('-')
			.appendValue(ChronoField.DAY_OF_MONTH, 2)
			.appendLiteral('_')
			.appendValue(ChronoField.HOUR_OF_DAY, 2)
			.appendLiteral('-')
			.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
			.appendLiteral('-')
			.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
			.toFormatter();
	}
}
