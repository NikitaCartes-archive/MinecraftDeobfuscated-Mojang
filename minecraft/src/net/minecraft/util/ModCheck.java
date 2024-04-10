package net.minecraft.util;

import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;

public record ModCheck(ModCheck.Confidence confidence, String description) {
	public static ModCheck identify(String string, Supplier<String> supplier, String string2, Class<?> class_) {
		String string3 = (String)supplier.get();
		if (!string.equals(string3)) {
			return new ModCheck(ModCheck.Confidence.DEFINITELY, string2 + " brand changed to '" + string3 + "'");
		} else {
			return class_.getSigners() == null
				? new ModCheck(ModCheck.Confidence.VERY_LIKELY, string2 + " jar signature invalidated")
				: new ModCheck(ModCheck.Confidence.PROBABLY_NOT, string2 + " jar signature and brand is untouched");
		}
	}

	public boolean shouldReportAsModified() {
		return this.confidence.shouldReportAsModified;
	}

	public ModCheck merge(ModCheck modCheck) {
		return new ModCheck(ObjectUtils.max(this.confidence, modCheck.confidence), this.description + "; " + modCheck.description);
	}

	public String fullDescription() {
		return this.confidence.description + " " + this.description;
	}

	public static enum Confidence {
		PROBABLY_NOT("Probably not.", false),
		VERY_LIKELY("Very likely;", true),
		DEFINITELY("Definitely;", true);

		final String description;
		final boolean shouldReportAsModified;

		private Confidence(final String string2, final boolean bl) {
			this.description = string2;
			this.shouldReportAsModified = bl;
		}
	}
}
