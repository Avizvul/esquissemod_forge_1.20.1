package net.avizvul.esquissemod.component;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * В 1.20.1 компонентов нет: те же данные лежат в NBT предмета. Значение по умолчанию заменяет
 * компоненты, которые в 1.21 задавались в Item.Properties, поэтому NBT пустых предметов не растёт.
 */
public final class ModDataComponents {
    private ModDataComponents() {}

    public static final Key<List<SketchData>> SKETCHBOOK_PAGES = new Key<>("sketchbook_pages", ModDataComponents::blankPages) {
        @Override
        List<SketchData> read(CompoundTag tag) {
            ListTag list = tag.getList(this.name, Tag.TAG_COMPOUND);
            List<SketchData> pages = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) pages.add(SketchData.fromTag(list.getCompound(i)));
            return pages;
        }

        @Override
        void write(CompoundTag tag, List<SketchData> pages) {
            ListTag list = new ListTag();
            for (SketchData page : pages) list.add(page.toTag());
            tag.put(this.name, list);
        }
    };

    public static final Key<SketchData> PAGE_DATA = new Key<>("page_data", () -> null) {
        @Override
        SketchData read(CompoundTag tag) {
            return SketchData.fromTag(tag.getCompound(this.name));
        }

        @Override
        void write(CompoundTag tag, SketchData data) {
            tag.put(this.name, data.toTag());
        }
    };

    public static final Key<ItemContainerContents> CONTAINER = new Key<>("container", () -> null) {
        @Override
        ItemContainerContents read(CompoundTag tag) {
            ListTag list = tag.getList(this.name, Tag.TAG_COMPOUND);
            NonNullList<ItemStack> items = NonNullList.withSize(tag.getInt(this.name + "_size"), ItemStack.EMPTY);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                int slot = entry.getInt("Slot");
                if (slot >= 0 && slot < items.size()) items.set(slot, ItemStack.of(entry));
            }
            return new ItemContainerContents(items);
        }

        @Override
        void write(CompoundTag tag, ItemContainerContents contents) {
            ListTag list = new ListTag();
            List<ItemStack> items = contents.items();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isEmpty()) continue;
                CompoundTag entry = new CompoundTag();
                entry.putInt("Slot", i);
                items.get(i).save(entry);
                list.add(entry);
            }
            tag.put(this.name, list);
            tag.putInt(this.name + "_size", items.size());
        }
    };

    // Имя предмета хранится самой игрой в display.Name
    public static final Key<Component> CUSTOM_NAME = new Key<>("display", () -> null) {
        @Override
        public boolean has(ItemStack stack) {
            return stack.hasCustomHoverName();
        }

        @Override
        public Component get(ItemStack stack) {
            return stack.hasCustomHoverName() ? stack.getHoverName() : null;
        }

        @Override
        public void set(ItemStack stack, Component value) {
            if (value == null) stack.resetHoverName();
            else stack.setHoverName(value);
        }

        @Override
        public void remove(ItemStack stack) {
            stack.resetHoverName();
        }

        @Override
        Component read(CompoundTag tag) {
            throw new UnsupportedOperationException();
        }

        @Override
        void write(CompoundTag tag, Component value) {
            throw new UnsupportedOperationException();
        }
    };

    public static final Key<Integer> UNSPENT_PIXELS = intKey("unspent_pixels", null);
    public static final Key<Integer> BRUSH_SIZE = intKey("brush_size", 1);
    public static final Key<Integer> BRUSH_HARDNESS = intKey("brush_hardness", 3);
    public static final Key<List<Integer>> STORED_COLORS = intListKey("stored_colors", ArrayList::new);
    public static final Key<Integer> ACTIVE_COLOR_INDEX = intKey("active_color_index", 0);
    public static final Key<Integer> MARKER_ROTATION = intKey("marker_rotation", 0);
    public static final Key<Integer> COMPASS_ANCHOR_X = intKey("compass_anchor_x", -1);
    public static final Key<Integer> COMPASS_ANCHOR_Y = intKey("compass_anchor_y", -1);
    public static final Key<Boolean> COMPASS_IS_DRAWING = boolKey("compass_is_drawing", false);
    public static final Key<Integer> LAST_PAGE = intKey("last_page", null);
    // Остаток краски по цветам: индекс списка = ID красителя (0..15), значение 0..256
    public static final Key<List<Integer>> PAINT_LEVELS = intListKey("paint_levels", () -> null);
    public static final Key<String> PAGE_AUTHOR = new Key<>("page_author", () -> null) {
        @Override
        String read(CompoundTag tag) {
            return tag.getString(this.name);
        }

        @Override
        void write(CompoundTag tag, String value) {
            tag.putString(this.name, value);
        }
    };
    public static final Key<Boolean> PAGE_SIGNED = boolKey("page_signed", null);
    public static final Key<Boolean> PAGE_COPIED = boolKey("page_copied", null);
    // Маска штампа: строка y = int, бит x = «выступает» (печатает). Наличие ключа = штамп запечатан
    public static final Key<List<Integer>> STAMP_BITS = intListKey("stamp_bits", () -> null);

    private static List<SketchData> blankPages() {
        List<SketchData> list = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) list.add(SketchData.EMPTY);
        return list;
    }

    private static Key<Integer> intKey(String name, Integer fallback) {
        return new Key<>(name, () -> fallback) {
            @Override
            Integer read(CompoundTag tag) {
                return tag.getInt(this.name);
            }

            @Override
            void write(CompoundTag tag, Integer value) {
                tag.putInt(this.name, value);
            }
        };
    }

    private static Key<Boolean> boolKey(String name, Boolean fallback) {
        return new Key<>(name, () -> fallback) {
            @Override
            Boolean read(CompoundTag tag) {
                return tag.getBoolean(this.name);
            }

            @Override
            void write(CompoundTag tag, Boolean value) {
                tag.putBoolean(this.name, value);
            }
        };
    }

    private static Key<List<Integer>> intListKey(String name, Supplier<List<Integer>> fallback) {
        return new Key<>(name, fallback) {
            @Override
            List<Integer> read(CompoundTag tag) {
                return new ArrayList<>(Arrays.stream(tag.getIntArray(this.name)).boxed().toList());
            }

            @Override
            void write(CompoundTag tag, List<Integer> value) {
                tag.putIntArray(this.name, value.stream().mapToInt(Integer::intValue).toArray());
            }
        };
    }

    public abstract static class Key<T> {
        protected final String name;
        private final Supplier<T> fallback;

        Key(String name, Supplier<T> fallback) {
            this.name = name;
            this.fallback = fallback;
        }

        abstract T read(CompoundTag tag);

        abstract void write(CompoundTag tag, T value);

        public boolean has(ItemStack stack) {
            return stack.hasTag() && stack.getTag().contains(this.name);
        }

        public T get(ItemStack stack) {
            return has(stack) ? read(stack.getTag()) : this.fallback.get();
        }

        public T getOrDefault(ItemStack stack, T defaultValue) {
            T value = get(stack);
            return value != null ? value : defaultValue;
        }

        public void set(ItemStack stack, T value) {
            if (value == null) remove(stack);
            else write(stack.getOrCreateTag(), value);
        }

        public void remove(ItemStack stack) {
            if (stack.hasTag()) {
                stack.getTag().remove(this.name);
                if (stack.getTag().isEmpty()) stack.setTag(null);
            }
        }
    }
}
