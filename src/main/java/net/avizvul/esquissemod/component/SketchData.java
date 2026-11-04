package net.avizvul.esquissemod.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SketchData {

    private final int[][] pixels;
    private final List<TextElement> textElements;
    private final int cachedHashCode;

    private static final int W = 126;
    private static final int H = 192;
    private static final int MAX_ENCODED = 2 * 1024 * 1024;
    private static final int MAX_DECODED_CACHE = 128;

    public static final SketchData EMPTY = new SketchData(new int[W][H], new ArrayList<>());

    // Разбор страницы из NBT дорог, а тултип и экран читают её каждый кадр. Массив байт
    // у тега неизменен, поэтому ключ сравнивается по ссылке (так работает byte[] в Map).
    private static final Map<byte[], SketchData> DECODED = new LinkedHashMap<>(MAX_DECODED_CACHE + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<byte[], SketchData> eldest) {
            return size() > MAX_DECODED_CACHE;
        }
    };

    // Сжатые пиксели: создаются один раз, чтобы не сжимать все 16 страниц при каждом сохранении
    private volatile byte[] encoded;

    private byte[] encodedPixels() {
        byte[] result = this.encoded;
        if (result != null) return result;

        int[][] pixels2D = toArray(W, H);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            DataOutputStream dos = new DataOutputStream(gzip);
            for (int x = 0; x < W; x++) {
                for (int y = 0; y < H; y++) {
                    dos.writeInt(pixels2D[x][y]);
                }
            }
            dos.flush();
            gzip.finish();
            result = baos.toByteArray();
        } catch (Exception e) {
            result = new byte[0];
            e.printStackTrace();
        }
        this.encoded = result;
        return result;
    }

    private static int[][] decodePixels(byte[] compressed) {
        int[][] pixels2D = new int[W][H];
        if (compressed.length == 0) return pixels2D;
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(compressed)))) {
            for (int x = 0; x < W; x++) {
                for (int y = 0; y < H; y++) {
                    pixels2D[x][y] = dis.readInt();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pixels2D;
    }

    // --- NBT: пустая страница занимает пустой тег, иначе GZIP-пиксели и текст ---

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        if (isEmpty()) return tag;
        tag.putByteArray("Pixels", encodedPixels());
        if (!this.textElements.isEmpty()) {
            ListTag texts = new ListTag();
            for (TextElement element : this.textElements) texts.add(element.toTag());
            tag.put("Texts", texts);
        }
        return tag;
    }

    public static SketchData fromTag(CompoundTag tag) {
        if (!tag.contains("Pixels")) return EMPTY;
        byte[] bytes = tag.getByteArray("Pixels");
        synchronized (DECODED) {
            SketchData cached = DECODED.get(bytes);
            if (cached != null) return cached;
        }
        List<TextElement> texts = new ArrayList<>();
        ListTag list = tag.getList("Texts", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) texts.add(TextElement.fromTag(list.getCompound(i)));

        SketchData data = new SketchData(decodePixels(bytes), texts);
        data.encoded = bytes;
        synchronized (DECODED) {
            DECODED.put(bytes, data);
        }
        return data;
    }

    // --- СЕТЬ ---

    public void write(FriendlyByteBuf buf) {
        buf.writeByteArray(encodedPixels());
        buf.writeVarInt(this.textElements.size());
        for (TextElement element : this.textElements) element.write(buf);
    }

    public static SketchData read(FriendlyByteBuf buf) {
        byte[] bytes = buf.readByteArray(MAX_ENCODED);
        int count = buf.readVarInt();
        List<TextElement> texts = new ArrayList<>();
        for (int i = 0; i < count; i++) texts.add(TextElement.read(buf));

        SketchData data = new SketchData(decodePixels(bytes), texts);
        data.encoded = bytes;
        return data;
    }

    // --- КОНСТРУКТОРЫ И ФАБРИКИ ---

    public SketchData(int[][] pixels, List<TextElement> textElements) {
        this.pixels = pixels != null ? pixels : new int[0][0];
        this.textElements = textElements != null ? new ArrayList<>(textElements) : new ArrayList<>();
        this.cachedHashCode = Objects.hash(Arrays.deepHashCode(this.pixels), this.textElements);
    }

    public SketchData(int[][] pixels) {
        this(pixels, new ArrayList<>());
    }

    public static SketchData fromArray(int[][] arr) {
        int w = arr.length;
        if (w == 0) return new SketchData(new int[0][0], new ArrayList<>());

        int h = arr[0].length;
        int[][] copy = new int[w][h];

        for (int x = 0; x < w; x++) {
            System.arraycopy(arr[x], 0, copy[x], 0, h);
        }
        return new SketchData(copy, new ArrayList<>());
    }

    public static SketchData fromArrayAndTexts(int[][] arr, List<TextElement> texts) {
        int w = arr.length;
        if (w == 0) return new SketchData(new int[0][0], texts);

        int h = arr[0].length;
        int[][] copy = new int[w][h];

        for (int x = 0; x < w; x++) {
            System.arraycopy(arr[x], 0, copy[x], 0, h);
        }
        return new SketchData(copy, texts);
    }

    // --- ГЕТТЕРЫ И ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    public int[][] getRawPixels() {
        return this.pixels;
    }

    public List<TextElement> getTextElements() {
        return this.textElements;
    }

    public int[][] toArray(int targetWidth, int targetHeight) {
        int[][] result = new int[targetWidth][targetHeight];
        int copyWidth = Math.min(targetWidth, this.pixels.length);

        if (copyWidth > 0 && this.pixels[0] != null) {
            int copyHeight = Math.min(targetHeight, this.pixels[0].length);

            for (int x = 0; x < copyWidth; x++) {
                System.arraycopy(this.pixels[x], 0, result[x], 0, copyHeight);
            }
        }
        return result;
    }

    public boolean isEmpty() {
        boolean noPixels = true;
        for (int[] row : this.pixels) {
            for (int p : row) {
                if (p != 0) {
                    noPixels = false;
                    break;
                }
            }
            if (!noPixels) break;
        }
        return noPixels && this.textElements.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SketchData that = (SketchData) obj;
        if (this.cachedHashCode != that.cachedHashCode) return false;
        return Arrays.deepEquals(this.pixels, that.pixels) && Objects.equals(this.textElements, that.textElements);
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }
}
