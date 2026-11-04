package net.avizvul.esquissemod.item;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EsquisseMod.MOD_ID);


    public static final Supplier<CreativeModeTab> ART_SUPPLIES = CREATIVE_MODE_TAB.register("art_supplies",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SKETCHBOOK.get()))
                    .title(Component.translatable("creativetab.esquissemod.art_supplies"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.SKETCHBOOK.get());
                        output.accept(ModItems.ALBUM.get());
                        output.accept(ModItems.PENCIL_CASE.get());
                        output.accept(ModItems.PENCIL.get());
                        output.accept(ModItems.ERASER.get());
                        output.accept(ModItems.KNEADED_ERASER.get());
                        output.accept(ModItems.COLOR_PENCIL.get());
                        output.accept(ModItems.COLOR_MARKER.get());
                        output.accept(ModItems.RULER.get());
                        output.accept(ModItems.MAGNIFYING_GLASS.get());
                        output.accept(ModItems.SMUDGE.get());
                        output.accept(ModItems.DRAWING_COMPASS.get());
                        output.accept(ModItems.STAMP_SMALL.get());
                        output.accept(ModItems.STAMP_LARGE.get());
                        output.accept(ModItems.STAMP_PAD.get());
                        output.accept(ModItems.PRINTER.get());
                        output.accept(ModItems.WARP_ESSENCE.get());

                        // --- НОВОЕ: Полностью заполненный цветной карандаш ---
                        ItemStack fullColorPencil = new ItemStack(ModItems.COLOR_PENCIL.get());
                        java.util.List<Integer> allColors = new java.util.ArrayList<>();
                        // Заполняем список всеми 16 ID цветов (от 0 до 15)
                        for (int i = 0; i < 16; i++) {
                            allColors.add(i);
                        }
                        // Применяем список цветов к предмету
                        net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.set(fullColorPencil, allColors);
                        net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.set(fullColorPencil, 0);

                        // Добавляем готовый предмет во вкладку
                        output.accept(fullColorPencil);
                        // -----------------------------------------------------

                        ItemStack fullMarker = new ItemStack(ModItems.COLOR_MARKER.get());
                        net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.set(fullMarker, allColors);
                        net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.set(fullMarker, 0);
                        output.accept(fullMarker);

                        ItemStack fullPad = new ItemStack(ModItems.STAMP_PAD.get());
                        net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.set(fullPad, allColors);
                        net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.set(fullPad, 0);
                        output.accept(fullPad);

                    }).build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
