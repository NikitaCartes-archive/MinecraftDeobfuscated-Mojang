package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.Util;

public interface StatFormatter {
	DecimalFormat DECIMAL_FORMAT = Util.make(
		new DecimalFormat("########0.00"), decimalFormat -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
	);
	StatFormatter DEFAULT = NumberFormat.getIntegerInstance(Locale.US)::format;
	StatFormatter DIVIDE_BY_TEN = i -> DECIMAL_FORMAT.format((double)i * 0.1);
	StatFormatter DISTANCE = i -> {
		double d = (double)i / 100.0;
		double e = d / 1000.0;
		if (e > 0.5) {
			return DECIMAL_FORMAT.format(e) + " km";
		} else {
			return d > 0.5 ? DECIMAL_FORMAT.format(d) + " m" : i + " cm";
		}
	};
	StatFormatter TIME = i -> {
		double d = (double)i / 20.0;
		double e = d / 60.0;
		double f = e / 60.0;
		double g = f / 24.0;
		double h = g / 365.0;
		if (h > 0.5) {
			return DECIMAL_FORMAT.format(h) + " y";
		} else if (g > 0.5) {
			return DECIMAL_FORMAT.format(g) + " d";
		} else if (f > 0.5) {
			return DECIMAL_FORMAT.format(f) + " h";
		} else {
			return e > 0.5 ? DECIMAL_FORMAT.format(e) + " m" : d + " s";
		}
	};

	String format(int i);
}
