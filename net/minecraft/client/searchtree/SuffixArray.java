/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SuffixArray<T> {
    private static final boolean DEBUG_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
    private static final boolean DEBUG_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int END_OF_TEXT_MARKER = -1;
    private static final int END_OF_DATA = -2;
    protected final List<T> list = Lists.newArrayList();
    private final IntList chars = new IntArrayList();
    private final IntList wordStarts = new IntArrayList();
    private IntList suffixToT = new IntArrayList();
    private IntList offsets = new IntArrayList();
    private int maxStringLength;

    public void add(T object, String string) {
        this.maxStringLength = Math.max(this.maxStringLength, string.length());
        int i = this.list.size();
        this.list.add(object);
        this.wordStarts.add(this.chars.size());
        for (int j = 0; j < string.length(); ++j) {
            this.suffixToT.add(i);
            this.offsets.add(j);
            this.chars.add(string.charAt(j));
        }
        this.suffixToT.add(i);
        this.offsets.add(string.length());
        this.chars.add(-1);
    }

    public void generate() {
        int j2;
        int i2 = this.chars.size();
        int[] is = new int[i2];
        int[] js = new int[i2];
        int[] ks = new int[i2];
        int[] ls = new int[i2];
        IntComparator intComparator = (i, j) -> {
            if (js[i] == js[j]) {
                return Integer.compare(ks[i], ks[j]);
            }
            return Integer.compare(js[i], js[j]);
        };
        Swapper swapper = (i, j) -> {
            if (i != j) {
                int k = js[i];
                is[i] = js[j];
                is[j] = k;
                k = ks[i];
                js[i] = ks[j];
                js[j] = k;
                k = ls[i];
                ks[i] = ls[j];
                ks[j] = k;
            }
        };
        for (j2 = 0; j2 < i2; ++j2) {
            is[j2] = this.chars.getInt(j2);
        }
        j2 = 1;
        int k = Math.min(i2, this.maxStringLength);
        while (j2 * 2 < k) {
            int l;
            for (l = 0; l < i2; ++l) {
                js[l] = is[l];
                ks[l] = l + j2 < i2 ? is[l + j2] : -2;
                ls[l] = l;
            }
            it.unimi.dsi.fastutil.Arrays.quickSort(0, i2, intComparator, swapper);
            for (l = 0; l < i2; ++l) {
                is[ls[l]] = l > 0 && js[l] == js[l - 1] && ks[l] == ks[l - 1] ? is[ls[l - 1]] : l;
            }
            j2 *= 2;
        }
        IntList intList = this.suffixToT;
        IntList intList2 = this.offsets;
        this.suffixToT = new IntArrayList(intList.size());
        this.offsets = new IntArrayList(intList2.size());
        for (int m = 0; m < i2; ++m) {
            int n = ls[m];
            this.suffixToT.add(intList.getInt(n));
            this.offsets.add(intList2.getInt(n));
        }
        if (DEBUG_ARRAY) {
            this.print();
        }
    }

    private void print() {
        for (int i = 0; i < this.suffixToT.size(); ++i) {
            LOGGER.debug("{} {}", (Object)i, (Object)this.getString(i));
        }
        LOGGER.debug("");
    }

    private String getString(int i) {
        int j = this.offsets.getInt(i);
        int k = this.wordStarts.getInt(this.suffixToT.getInt(i));
        StringBuilder stringBuilder = new StringBuilder();
        int l = 0;
        while (k + l < this.chars.size()) {
            int m;
            if (l == j) {
                stringBuilder.append('^');
            }
            if ((m = this.chars.getInt(k + l)) == -1) break;
            stringBuilder.append((char)m);
            ++l;
        }
        return stringBuilder.toString();
    }

    private int compare(String string, int i) {
        int j = this.wordStarts.getInt(this.suffixToT.getInt(i));
        int k = this.offsets.getInt(i);
        for (int l = 0; l < string.length(); ++l) {
            char d;
            int m = this.chars.getInt(j + k + l);
            if (m == -1) {
                return 1;
            }
            char c = string.charAt(l);
            if (c < (d = (char)m)) {
                return -1;
            }
            if (c <= d) continue;
            return 1;
        }
        return 0;
    }

    public List<T> search(String string) {
        int m;
        int l;
        int i = this.suffixToT.size();
        int j = 0;
        int k = i;
        while (j < k) {
            l = j + (k - j) / 2;
            m = this.compare(string, l);
            if (DEBUG_COMPARISONS) {
                LOGGER.debug("comparing lower \"{}\" with {} \"{}\": {}", (Object)string, (Object)l, (Object)this.getString(l), (Object)m);
            }
            if (m > 0) {
                j = l + 1;
                continue;
            }
            k = l;
        }
        if (j < 0 || j >= i) {
            return Collections.emptyList();
        }
        l = j;
        k = i;
        while (j < k) {
            m = j + (k - j) / 2;
            int n = this.compare(string, m);
            if (DEBUG_COMPARISONS) {
                LOGGER.debug("comparing upper \"{}\" with {} \"{}\": {}", (Object)string, (Object)m, (Object)this.getString(m), (Object)n);
            }
            if (n >= 0) {
                j = m + 1;
                continue;
            }
            k = m;
        }
        m = j;
        IntOpenHashSet intSet = new IntOpenHashSet();
        for (int o = l; o < m; ++o) {
            intSet.add(this.suffixToT.getInt(o));
        }
        int[] is = intSet.toIntArray();
        Arrays.sort(is);
        LinkedHashSet<T> set = Sets.newLinkedHashSet();
        for (int p : is) {
            set.add(this.list.get(p));
        }
        return Lists.newArrayList(set);
    }
}

