package net.minecraft.client.gui.components.debugchart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ResultField;

@Environment(EnvType.CLIENT)
public class ProfilerPieChart {
	private static final int RADIUS = 105;
	private static final int MARGIN = 5;
	private static final int CHART_Z_OFFSET = 10;
	private final Font font;
	@Nullable
	private ProfileResults profilerPieChartResults;
	private String profilerTreePath = "root";
	private int bottomOffset = 0;

	public ProfilerPieChart(Font font) {
		this.font = font;
	}

	public void setPieChartResults(@Nullable ProfileResults profileResults) {
		this.profilerPieChartResults = profileResults;
	}

	public void setBottomOffset(int i) {
		this.bottomOffset = i;
	}

	public void render(GuiGraphics guiGraphics) {
		if (this.profilerPieChartResults != null) {
			List<ResultField> list = this.profilerPieChartResults.getTimes(this.profilerTreePath);
			ResultField resultField = (ResultField)list.removeFirst();
			int i = guiGraphics.guiWidth() - 105 - 10;
			int j = i - 105;
			int k = i + 105;
			int l = list.size() * 9;
			int m = guiGraphics.guiHeight() - this.bottomOffset - 5;
			int n = m - l;
			int o = 62;
			int p = n - 62 - 5;
			guiGraphics.fill(j - 5, p - 62 - 5, k + 5, m + 5, -1873784752);
			guiGraphics.drawSpecial(multiBufferSource -> {
				double d = 0.0;

				for (ResultField resultFieldx : list) {
					int kx = Mth.floor(resultFieldx.percentage / 4.0) + 1;
					VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugTriangleFan());
					int lx = ARGB.opaque(resultFieldx.getColor());
					int mx = ARGB.multiply(lx, -8355712);
					PoseStack.Pose pose = guiGraphics.pose().last();
					vertexConsumer.addVertex(pose, (float)i, (float)p, 10.0F).setColor(lx);

					for (int nxx = kx; nxx >= 0; nxx--) {
						float f = (float)((d + resultFieldx.percentage * (double)nxx / (double)kx) * (float) (Math.PI * 2) / 100.0);
						float g = Mth.sin(f) * 105.0F;
						float h = Mth.cos(f) * 105.0F * 0.5F;
						vertexConsumer.addVertex(pose, (float)i + g, (float)p - h, 10.0F).setColor(lx);
					}

					vertexConsumer = multiBufferSource.getBuffer(RenderType.debugQuads());

					for (int nx = kx; nx > 0; nx--) {
						float f = (float)((d + resultFieldx.percentage * (double)nx / (double)kx) * (float) (Math.PI * 2) / 100.0);
						float g = Mth.sin(f) * 105.0F;
						float h = Mth.cos(f) * 105.0F * 0.5F;
						float ox = (float)((d + resultFieldx.percentage * (double)(nx - 1) / (double)kx) * (float) (Math.PI * 2) / 100.0);
						float px = Mth.sin(ox) * 105.0F;
						float qx = Mth.cos(ox) * 105.0F * 0.5F;
						if (!((h + qx) / 2.0F > 0.0F)) {
							vertexConsumer.addVertex(pose, (float)i + g, (float)p - h, 10.0F).setColor(mx);
							vertexConsumer.addVertex(pose, (float)i + g, (float)p - h + 10.0F, 10.0F).setColor(mx);
							vertexConsumer.addVertex(pose, (float)i + px, (float)p - qx + 10.0F, 10.0F).setColor(mx);
							vertexConsumer.addVertex(pose, (float)i + px, (float)p - qx, 10.0F).setColor(mx);
						}
					}

					d += resultFieldx.percentage;
				}
			});
			DecimalFormat decimalFormat = new DecimalFormat("##0.00");
			decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
			String string = ProfileResults.demanglePath(resultField.name);
			String string2 = "";
			if (!"unspecified".equals(string)) {
				string2 = string2 + "[0] ";
			}

			if (string.isEmpty()) {
				string2 = string2 + "ROOT ";
			} else {
				string2 = string2 + string + " ";
			}

			int q = 16777215;
			int r = p - 62;
			guiGraphics.drawString(this.font, string2, j, r, 16777215);
			string2 = decimalFormat.format(resultField.globalPercentage) + "%";
			guiGraphics.drawString(this.font, string2, k - this.font.width(string2), r, 16777215);

			for (int s = 0; s < list.size(); s++) {
				ResultField resultField2 = (ResultField)list.get(s);
				StringBuilder stringBuilder = new StringBuilder();
				if ("unspecified".equals(resultField2.name)) {
					stringBuilder.append("[?] ");
				} else {
					stringBuilder.append("[").append(s + 1).append("] ");
				}

				String string3 = stringBuilder.append(resultField2.name).toString();
				int t = n + s * 9;
				guiGraphics.drawString(this.font, string3, j, t, resultField2.getColor());
				string3 = decimalFormat.format(resultField2.percentage) + "%";
				guiGraphics.drawString(this.font, string3, k - 50 - this.font.width(string3), t, resultField2.getColor());
				string3 = decimalFormat.format(resultField2.globalPercentage) + "%";
				guiGraphics.drawString(this.font, string3, k - this.font.width(string3), t, resultField2.getColor());
			}
		}
	}

	public void profilerPieChartKeyPress(int i) {
		if (this.profilerPieChartResults != null) {
			List<ResultField> list = this.profilerPieChartResults.getTimes(this.profilerTreePath);
			if (!list.isEmpty()) {
				ResultField resultField = (ResultField)list.remove(0);
				if (i == 0) {
					if (!resultField.name.isEmpty()) {
						int j = this.profilerTreePath.lastIndexOf(30);
						if (j >= 0) {
							this.profilerTreePath = this.profilerTreePath.substring(0, j);
						}
					}
				} else {
					i--;
					if (i < list.size() && !"unspecified".equals(((ResultField)list.get(i)).name)) {
						if (!this.profilerTreePath.isEmpty()) {
							this.profilerTreePath = this.profilerTreePath + "\u001e";
						}

						this.profilerTreePath = this.profilerTreePath + ((ResultField)list.get(i)).name;
					}
				}
			}
		}
	}
}
