package net.minecraft.client.multiplayer.chat.report;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public enum BanReason {
	GENERIC_VIOLATION("generic_violation"),
	FALSE_REPORTING("false_reporting"),
	HATE_SPEECH("hate_speech"),
	HATE_TERRORISM_NOTORIOUS_FIGURE("hate_terrorism_notorious_figure"),
	HARASSMENT_OR_BULLYING("harassment_or_bullying"),
	DEFAMATION_IMPERSONATION_FALSE_INFORMATION("defamation_impersonation_false_information"),
	DRUGS("drugs"),
	FRAUD("fraud"),
	SPAM_OR_ADVERTISING("spam_or_advertising"),
	NUDITY_OR_PORNOGRAPHY("nudity_or_pornography"),
	SEXUALLY_INAPPROPRIATE("sexually_inappropriate"),
	EXTREME_VIOLENCE_OR_GORE("extreme_violence_or_gore"),
	IMMINENT_HARM_TO_PERSON_OR_PROPERTY("imminent_harm_to_person_or_property");

	private final Component title;

	private BanReason(final String string2) {
		this.title = Component.translatable("gui.banned.reason." + string2);
	}

	public Component title() {
		return this.title;
	}

	@Nullable
	public static BanReason byId(int i) {
		return switch (i) {
			case 2 -> FALSE_REPORTING;
			default -> null;
			case 5 -> HATE_SPEECH;
			case 16, 25 -> HATE_TERRORISM_NOTORIOUS_FIGURE;
			case 17, 19, 23, 31 -> GENERIC_VIOLATION;
			case 21 -> HARASSMENT_OR_BULLYING;
			case 27 -> DEFAMATION_IMPERSONATION_FALSE_INFORMATION;
			case 28 -> DRUGS;
			case 29 -> FRAUD;
			case 30 -> SPAM_OR_ADVERTISING;
			case 32 -> NUDITY_OR_PORNOGRAPHY;
			case 33 -> SEXUALLY_INAPPROPRIATE;
			case 34 -> EXTREME_VIOLENCE_OR_GORE;
			case 53 -> IMMINENT_HARM_TO_PERSON_OR_PROPERTY;
		};
	}
}
