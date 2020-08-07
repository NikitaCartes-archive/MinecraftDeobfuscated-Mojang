package net.minecraft.client;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

@Environment(EnvType.CLIENT)
public class ComponentCollector {
	private final List<FormattedText> parts = Lists.<FormattedText>newArrayList();

	public void append(FormattedText formattedText) {
		this.parts.add(formattedText);
	}

	@Nullable
	public FormattedText getResult() {
		if (this.parts.isEmpty()) {
			return null;
		} else {
			return this.parts.size() == 1 ? (FormattedText)this.parts.get(0) : FormattedText.composite(this.parts);
		}
	}

	public FormattedText getResultOrEmpty() {
		FormattedText formattedText = this.getResult();
		return formattedText != null ? formattedText : FormattedText.EMPTY;
	}
}
