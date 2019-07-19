/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class TextRenderingUtils {
    static List<String> lineBreak(String string) {
        return Arrays.asList(string.split("\\n"));
    }

    public static List<Line> decompose(String string, LineSegment ... lineSegments) {
        return TextRenderingUtils.decompose(string, Arrays.asList(lineSegments));
    }

    private static List<Line> decompose(String string, List<LineSegment> list) {
        List<String> list2 = TextRenderingUtils.lineBreak(string);
        return TextRenderingUtils.insertLinks(list2, list);
    }

    private static List<Line> insertLinks(List<String> list, List<LineSegment> list2) {
        int i = 0;
        ArrayList<Line> arrayList = new ArrayList<Line>();
        for (String string : list) {
            ArrayList<LineSegment> list3 = new ArrayList<LineSegment>();
            List<String> list4 = TextRenderingUtils.split(string, "%link");
            for (String string2 : list4) {
                if (string2.equals("%link")) {
                    list3.add(list2.get(i++));
                    continue;
                }
                list3.add(LineSegment.text(string2));
            }
            arrayList.add(new Line(list3));
        }
        return arrayList;
    }

    public static List<String> split(String string, String string2) {
        int j;
        if (string2.isEmpty()) {
            throw new IllegalArgumentException("Delimiter cannot be the empty string");
        }
        ArrayList<String> list = new ArrayList<String>();
        int i = 0;
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

    @Environment(value=EnvType.CLIENT)
    public static class LineSegment {
        final String fullText;
        final String linkTitle;
        final String linkUrl;

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
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            LineSegment lineSegment = (LineSegment)object;
            return Objects.equals(this.fullText, lineSegment.fullText) && Objects.equals(this.linkTitle, lineSegment.linkTitle) && Objects.equals(this.linkUrl, lineSegment.linkUrl);
        }

        public int hashCode() {
            return Objects.hash(this.fullText, this.linkTitle, this.linkUrl);
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
            }
            return this.linkUrl;
        }

        public static LineSegment link(String string, String string2) {
            return new LineSegment(null, string, string2);
        }

        static LineSegment text(String string) {
            return new LineSegment(string);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Line {
        public final List<LineSegment> segments;

        Line(List<LineSegment> list) {
            this.segments = list;
        }

        public String toString() {
            return "Line{segments=" + this.segments + '}';
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            Line line = (Line)object;
            return Objects.equals(this.segments, line.segments);
        }

        public int hashCode() {
            return Objects.hash(this.segments);
        }
    }
}

