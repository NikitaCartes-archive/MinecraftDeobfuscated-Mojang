package com.mojang.realmsclient.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TextRenderingUtils {
	@VisibleForTesting
	protected static List<String> lineBreak(String string) {
		return Arrays.asList(string.split("\\n"));
	}

	public static List<TextRenderingUtils.Line> decompose(String string, TextRenderingUtils.LineSegment... lineSegments) {
		return decompose(string, Arrays.asList(lineSegments));
	}

	private static List<TextRenderingUtils.Line> decompose(String string, List<TextRenderingUtils.LineSegment> list) {
		List<String> list2 = lineBreak(string);
		return insertLinks(list2, list);
	}

	private static List<TextRenderingUtils.Line> insertLinks(List<String> list, List<TextRenderingUtils.LineSegment> list2) {
		int i = 0;
		List<TextRenderingUtils.Line> list3 = Lists.<TextRenderingUtils.Line>newArrayList();

		for (String string : list) {
			List<TextRenderingUtils.LineSegment> list4 = Lists.<TextRenderingUtils.LineSegment>newArrayList();

			for (String string2 : split(string, "%link")) {
				if ("%link".equals(string2)) {
					list4.add(list2.get(i++));
				} else {
					list4.add(TextRenderingUtils.LineSegment.text(string2));
				}
			}

			list3.add(new TextRenderingUtils.Line(list4));
		}

		return list3;
	}

	public static List<String> split(String string, String string2) {
		if (string2.isEmpty()) {
			throw new IllegalArgumentException("Delimiter cannot be the empty string");
		} else {
			List<String> list = Lists.<String>newArrayList();
			int i = 0;

			int j;
			while ((j = string.indexOf(string2, i)) != -1) {
				if (j > i) {
					list.add(string.substring(i, j));
				}

				list.add(string2);
				i = j + string2.length();
			}

			if (i < string.length()) {
				list.add(string.substring(i));
			}

			return list;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Line {
		public final List<TextRenderingUtils.LineSegment> segments;

		Line(List<TextRenderingUtils.LineSegment> list) {
			this.segments = list;
		}

		public String toString() {
			return "Line{segments=" + this.segments + '}';
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				TextRenderingUtils.Line line = (TextRenderingUtils.Line)object;
				return Objects.equals(this.segments, line.segments);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.segments});
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LineSegment {
		private final String fullText;
		private final String linkTitle;
		private final String linkUrl;

		private LineSegment(String string) {
			this.fullText = string;
			this.linkTitle = null;
			this.linkUrl = null;
		}

		private LineSegment(String string, String string2, String string3) {
			this.fullText = string;
			this.linkTitle = string2;
			this.linkUrl = string3;
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				TextRenderingUtils.LineSegment lineSegment = (TextRenderingUtils.LineSegment)object;
				return Objects.equals(this.fullText, lineSegment.fullText)
					&& Objects.equals(this.linkTitle, lineSegment.linkTitle)
					&& Objects.equals(this.linkUrl, lineSegment.linkUrl);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.fullText, this.linkTitle, this.linkUrl});
		}

		public String toString() {
			return "Segment{fullText='" + this.fullText + '\'' + ", linkTitle='" + this.linkTitle + '\'' + ", linkUrl='" + this.linkUrl + '\'' + '}';
		}

		public String renderedText() {
			return this.isLink() ? this.linkTitle : this.fullText;
		}

		public boolean isLink() {
			return this.linkTitle != null;
		}

		public String getLinkUrl() {
			if (!this.isLink()) {
				throw new IllegalStateException("Not a link: " + this);
			} else {
				return this.linkUrl;
			}
		}

		public static TextRenderingUtils.LineSegment link(String string, String string2) {
			return new TextRenderingUtils.LineSegment(null, string, string2);
		}

		@VisibleForTesting
		protected static TextRenderingUtils.LineSegment text(String string) {
			return new TextRenderingUtils.LineSegment(string);
		}
	}
}
