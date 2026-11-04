package net.avizvul.esquissemod.component;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Содержимое пенала и альбома. Повторяет компонент из 1.21, чтобы код меню остался прежним. */
public final class ItemContainerContents {
    private final List<ItemStack> items;

    ItemContainerContents(List<ItemStack> items) {
        this.items = items;
    }

    public static ItemContainerContents fromItems(List<ItemStack> source) {
        NonNullList<ItemStack> copy = NonNullList.withSize(source.size(), ItemStack.EMPTY);
        for (int i = 0; i < source.size(); i++) copy.set(i, source.get(i).copy());
        return new ItemContainerContents(copy);
    }

    public static ItemContainerContents fromContainer(Container container) {
        NonNullList<ItemStack> items = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) items.set(i, container.getItem(i));
        return fromItems(items);
    }

    /** Начальное содержимое для SimpleContainer: setItem при загрузке сохранил бы предмет наполовину пустым. */
    public static ItemStack[] load(ItemStack stack, int size) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        ItemContainerContents contents = ModDataComponents.CONTAINER.get(stack);
        if (contents != null) contents.copyInto(items);
        return items.toArray(new ItemStack[0]);
    }

    public void copyInto(List<ItemStack> target) {
        for (int i = 0; i < target.size(); i++) {
            target.set(i, i < this.items.size() ? this.items.get(i).copy() : ItemStack.EMPTY);
        }
    }

    List<ItemStack> items() {
        return this.items;
    }
}
