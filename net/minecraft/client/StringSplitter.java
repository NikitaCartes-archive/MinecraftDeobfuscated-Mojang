/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.StringDecomposer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StringSplitter {
    private final WidthProvider widthProvider;

    public StringSplitter(WidthProvider widthProvider) {
        this.widthProvider = widthProvider;
    }

    public float stringWidth(@Nullable String string) {
        if (string == null) {
            return 0.0f;
        }
        MutableFloat mutableFloat = new MutableFloat();
        StringDecomposer.iterateFormatted(string, Style.EMPTY, (i, style, j) -> {
            mutableFloat.add(this.widthProvider.getWidth(j, style));
            return true;
        });
        return mutableFloat.floatValue();
    }

    public float stringWidth(FormattedText formattedText) {
        MutableFloat mutableFloat = new MutableFloat();
        StringDecomposer.iterateFormatted(formattedText, Style.EMPTY, (i, style, j) -> {
            mutableFloat.add(this.widthProvider.getWidth(j, style));
            return true;
        });
        return mutableFloat.floatValue();
    }

    public int plainIndexAtWidth(String string, int i, Style style) {
        WidthLimitedCharSink widthLimitedCharSink = new WidthLimitedCharSink(i);
        StringDecomposer.iterate(string, style, widthLimitedCharSink);
        return widthLimitedCharSink.getPosition();
    }

    public String plainHeadByWidth(String string, int i, Style style) {
        return string.substring(0, this.plainIndexAtWidth(string, i, style));
    }

    public String plainTailByWidth(String string, int i, Style style2) {
        MutableFloat mutableFloat = new MutableFloat();
        MutableInt mutableInt = new MutableInt(string.length());
        StringDecomposer.iterateBackwards(string, style2, (j, style, k) -> {
            float f = mutableFloat.addAndGet(this.widthProvider.getWidth(k, style));
            if (f > (float)i) {
                return false;
            }
            mutableInt.setValue(j);
            return true;
        });
        return string.substring(mutableInt.intValue());
    }

    @Nullable
    public Style componentStyleAtWidth(FormattedText formattedText, int i) {
        WidthLimitedCharSink widthLimitedCharSink = new WidthLimitedCharSink(i);
        return formattedText.visit((style, string) -> StringDecomposer.iterateFormatted(string, style, (StringDecomposer.Output)widthLimitedCharSink) ? Optional.empty() : Optional.of(style), Style.EMPTY).orElse(null);
    }

    public FormattedText headByWidth(FormattedText formattedText, int i, Style style) {
        final WidthLimitedCharSink widthLimitedCharSink = new WidthLimitedCharSink(i);
        return formattedText.visit(new FormattedText.StyledContentConsumer<FormattedText>(){
            private final ComponentCollector collector = new ComponentCollector();

            @Override
            public Optional<FormattedText> accept(Style style, String string) {
                widthLimitedCharSink.resetPosition();
                if (!StringDecomposer.iterateFormatted(string, style, (StringDecomposer.Output)widthLimitedCharSink)) {
                    String string2 = string.substring(0, widthLimitedCharSink.getPosition());
                    if (!string2.isEmpty()) {
                        this.collector.append(FormattedText.of(string2, style));
                    }
                    return Optional.of(this.collector.getResultOrEmpty());
                }
                if (!string.isEmpty()) {
                    this.collector.append(FormattedText.of(string, style));
                }
                return Optional.empty();
            }
        }, style).orElse(formattedText);
    }

    public static int getWordPosition(String string, int i, int j, boolean bl) {
        int k = j;
        boolean bl2 = i < 0;
        int l = Math.abs(i);
        for (int m = 0; m < l; ++m) {
            if (bl2) {
                while (bl && k > 0 && (string.charAt(k - 1) == ' ' || string.charAt(k - 1) == '\n')) {
                    --k;
                }
                while (k > 0 && string.charAt(k - 1) != ' ' && string.charAt(k - 1) != '\n') {
                    --k;
                }
                continue;
            }
            int n = string.length();
            int o = string.indexOf(32, k);
            int p = string.indexOf(10, k);
            k = o == -1 && p == -1 ? -1 : (o != -1 && p != -1 ? Math.min(o, p) : (o != -1 ? o : p));
            if (k == -1) {
                k = n;
                continue;
            }
            while (bl && k < n && (string.charAt(k) == ' ' || string.charAt(k) == '\n')) {
                ++k;
            }
        }
        return k;
    }

    public void splitLines(String string, int i, Style style, boolean bl, LinePosConsumer linePosConsumer) {
        int j = 0;
        int k = string.length();
        Style style2 = style;
        while (j < k) {
            LineBreakFinder lineBreakFinder = new LineBreakFinder(i);
            boolean bl2 = StringDecomposer.iterateFormatted(string, j, style2, style, lineBreakFinder);
            if (bl2) {
                linePosConsumer.accept(style2, j, k);
                break;
            }
            int l = lineBreakFinder.getSplitPosition();
            char c = string.charAt(l);
            int m = c == '\n' || c == ' ' ? l + 1 : l;
            linePosConsumer.accept(style2, j, bl ? m : l);
            j = m;
            style2 = lineBreakFinder.getSplitStyle();
        }
    }

    public List<FormattedText> splitLines(String string, int i2, Style style2) {
        ArrayList<FormattedText> list = Lists.newArrayList();
        this.splitLines(string, i2, style2, false, (style, i, j) -> list.add(FormattedText.of(string.substring(i, j), style)));
        return list;
    }

    public List<FormattedText> splitLines(FormattedText formattedText, int i, Style style) {
        return this.splitLines(formattedText, i, style, null);
    }

    public List<FormattedText> splitLines(FormattedText formattedText, int i, Style style2, @Nullable FormattedText formattedText2) {
        ArrayList<FormattedText> list = Lists.newArrayList();
        ArrayList<LineComponent> list2 = Lists.newArrayList();
        formattedText.visit((style, string) -> {
            if (!string.isEmpty()) {
                list2.add(new LineComponent(string, style));
            }
            return Optional.empty();
        }, style2);
        FlatComponents flatComponents = new FlatComponents(list2);
        boolean bl = true;
        boolean bl2 = false;
        boolean bl3 = false;
        block0: while (bl) {
            bl = false;
            LineBreakFinder lineBreakFinder = new LineBreakFinder(i);
            for (LineComponent lineComponent : flatComponents.parts) {
                boolean bl4 = StringDecomposer.iterateFormatted(lineComponent.contents, 0, lineComponent.style, style2, lineBreakFinder);
                if (!bl4) {
                    int j = lineBreakFinder.getSplitPosition();
                    Style style22 = lineBreakFinder.getSplitStyle();
                    char c = flatComponents.charAt(j);
                    boolean bl5 = c == '\n';
                    boolean bl6 = bl5 || c == ' ';
                    bl2 = bl5;
                    FormattedText formattedText3 = flatComponents.splitAt(j, bl6 ? 1 : 0, style22);
                    list.add(this.formattedLine(formattedText3, bl3, formattedText2));
                    bl3 = !bl5;
                    bl = true;
                    continue block0;
                }
                lineBreakFinder.addToOffset(lineComponent.contents.length());
            }
        }
        FormattedText formattedText4 = flatComponents.getRemainder();
        if (formattedText4 != null) {
            list.add(this.formattedLine(formattedText4, bl3, formattedText2));
        } else if (bl2) {
            list.add(FormattedText.EMPTY);
        }
        return list;
    }

    private FormattedText formattedLine(FormattedText formattedText, boolean bl, FormattedText formattedText2) {
        if (bl && formattedText2 != null) {
            return FormattedText.composite(formattedText2, formattedText);
        }
        return formattedText;
    }

    @Environment(value=EnvType.CLIENT)
    static class FlatComponents {
        private final List<LineComponent> parts;
        private String flatParts;

        public FlatComponents(List<LineComponent> list) {
            this.parts = list;
            this.flatParts = list.stream().map(lineComponent -> ((LineComponent)lineComponent).contents).collect(Collectors.joining());
        }

        public char charAt(int i) {
            return this.flatParts.charAt(i);
        }

        public FormattedText splitAt(int i, int j, Style style) {
            ComponentCollector componentCollector = new ComponentCollector();
            ListIterator<LineComponent> listIterator = this.parts.listIterator();
            int k = i;
            boolean bl = false;
            while (listIterator.hasNext()) {
                String string2;
                LineComponent lineComponent = listIterator.next();
                String string = lineComponent.contents;
                int l = string.length();
                if (!bl) {
                    if (k > l) {
                        componentCollector.append(lineComponent);
                        listIterator.remove();
                        k -= l;
                    } else {
                        string2 = string.substring(0, k);
                        if (!string2.isEmpty()) {
                            componentCollector.append(FormattedText.of(string2, lineComponent.style));
                        }
                        k += j;
                        bl = true;
                    }
                }
                if (!bl) continue;
                if (k > l) {
                    listIterator.remove();
                    k -= l;
                    continue;
                }
                string2 = string.substring(k);
                if (string2.isEmpty()) {
                    listIterator.remove();
                    break;
                }
                listIterator.set(new LineComponent(string2, style));
                break;
            }
            this.flatParts = this.flatParts.substring(i + j);
            return componentCollector.getResultOrEmpty();
        }

        @Nullable
        public FormattedText getRemainder() {
            ComponentCollector componentCollector = new ComponentCollector();
            this.parts.forEach(componentCollector::append);
            this.parts.clear();
            return componentCollector.getResult();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class LineComponent
    implements FormattedText {
        private final String contents;
        private final Style style;

        public LineComponent(String string, Style style) {
            this.contents = string;
            this.style = style;
        }

        @Override
        public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
            return contentConsumer.accept(this.contents);
        }

        @Override
        public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
            return styledContentConsumer.accept(this.style.applyTo(style), this.contents);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface LinePosConsumer {
        public void accept(Style var1, int var2, int var3);
    }

    @Environment(value=EnvType.CLIENT)
    class LineBreakFinder
    implements StringDecomposer.Output {
        private final float maxWidth;
        private int lineBreak = -1;
        private Style lineBreakStyle = Style.EMPTY;
        private boolean hadNonZeroWidthChar;
        private float width;
        private int lastSpace = -1;
        private Style lastSpaceStyle = Style.EMPTY;
        private int nextChar;
        private int offset;

        public LineBreakFinder(float f) {
            this.maxWidth = Math.max(f, 1.0f);
        }

        @Override
        public boolean onChar(int i, Style style, int j) {
            int k = i + this.offset;
            switch (j) {
                case 10: {
                    return this.finishIteration(k, style);
                }
                case 32: {
                    this.lastSpace = k;
                    this.lastSpaceStyle = style;
                }
            }
            float f = StringSplitter.this.widthProvider.getWidth(j, style);
            this.width += f;
            if (this.hadNonZeroWidthChar && this.width > this.maxWidth) {
                if (this.lastSpace != -1) {
                    return this.finishIteration(this.lastSpace, this.lastSpaceStyle);
                }
                return this.finishIteration(k, style);
            }
            this.hadNonZeroWidthChar |= f != 0.0f;
            this.nextChar = k + Character.charCount(j);
            return true;
        }

        private boolean finishIteration(int i, Style style) {
            this.lineBreak = i;
            this.lineBreakStyle = style;
            return false;
        }

        private boolean lineBreakFound() {
            return this.lineBreak != -1;
        }

        public int getSplitPosition() {
            return this.lineBreakFound() ? this.lineBreak : this.nextChar;
        }

        public Style getSplitStyle() {
            return this.lineBreakStyle;
        }

        public void addToOffset(int i) {
            this.offset += i;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class WidthLimitedCharSink
    implements StringDecomposer.Output {
        private float maxWidth;
        private int position;

        public WidthLimitedCharSink(float f) {
            this.maxWidth = f;
        }

        @Override
        public boolean onChar(int i, Style style, int j) {
            this.maxWidth -= StringSplitter.this.widthProvider.getWidth(j, style);
            if (this.maxWidth >= 0.0f) {
                this.position = i + Character.charCount(j);
                return true;
            }
            return false;
        }

        public int getPosition() {
            return this.position;
        }

        public void resetPosition() {
            this.position = 0;
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface WidthProvider {
        public float getWidth(int var1, Style var2);
    }
}

