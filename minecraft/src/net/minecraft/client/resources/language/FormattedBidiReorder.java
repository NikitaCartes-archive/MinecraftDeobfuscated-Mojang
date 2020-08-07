package net.minecraft.client.resources.language;

import com.google.common.collect.Lists;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.BidiRun;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.SubStringSource;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class FormattedBidiReorder {
	public static FormattedCharSequence reorder(FormattedText formattedText, boolean bl) {
		SubStringSource subStringSource = SubStringSource.create(formattedText, UCharacter::getMirror, FormattedBidiReorder::shape);
		Bidi bidi = new Bidi(subStringSource.getPlainText(), bl ? 127 : 126);
		bidi.setReorderingMode(0);
		List<FormattedCharSequence> list = Lists.<FormattedCharSequence>newArrayList();
		int i = bidi.countRuns();

		for (int j = 0; j < i; j++) {
			BidiRun bidiRun = bidi.getVisualRun(j);
			list.addAll(subStringSource.substring(bidiRun.getStart(), bidiRun.getLength(), bidiRun.isOddRun()));
		}

		return FormattedCharSequence.composite(list);
	}

	private static String shape(String string) {
		try {
			return new ArabicShaping(8).shape(string);
		} catch (Exception var2) {
			return string;
		}
	}
}
