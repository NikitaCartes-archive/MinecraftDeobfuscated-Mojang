package net.minecraft.client.multiplayer.chat.report;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public enum ReportReason {
	GENERIC("generic"),
	HATE_SPEECH("hate_speech"),
	HARASSMENT_OR_BULLYING("harassment_or_bullying"),
	SELF_HARM_OR_SUICIDE("self_harm_or_suicide"),
	IMMINENT_HARM("imminent_harm"),
	DEFAMATION_IMPERSONATION_FALSE_INFORMATION("defamation_impersonation_false_information"),
	ALCOHOL_TOBACCO_DRUGS("alcohol_tobacco_drugs"),
	CHILD_SEXUAL_EXPLOITATION_OR_ABUSE("child_sexual_exploitation_or_abuse"),
	TERRORISM_OR_VIOLENT_EXTREMISM("terrorism_or_violent_extremism"),
	NON_CONSENSUAL_INTIMATE_IMAGERY("non_consensual_intimate_imagery");

	private final String backendName;
	private final Component title;
	private final Component description;

	private ReportReason(final String string2) {
		this.backendName = string2.toUpperCase(Locale.ROOT);
		String string3 = "gui.abuseReport.reason." + string2;
		this.title = Component.translatable(string3);
		this.description = Component.translatable(string3 + ".description");
	}

	public String backendName() {
		return this.backendName;
	}

	public Component title() {
		return this.title;
	}

	public Component description() {
		return this.description;
	}
}
