package net.avizvul.esquissemod.item;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.component.ModDataComponents; // Убедитесь, что импорт правильный для вашего компонента
import net.avizvul.esquissemod.item.custom.*;
import net.avizvul.esquissemod.item.custom.base.DrawingToolItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.avizvul.esquissemod.block.ModBlocks;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.ITEMS, EsquisseMod.MOD_ID);

    public static final RegistryObject<Item> SKETCHBOOK = ITEMS.register("sketchbook",
            () -> new SketchbookItem(new Item.Properties()
                    .stacksTo(1)
                    // 16 пустых страниц подставляет ModDataComponents, пока рисунка нет
            ));

    public static final RegistryObject<Item> ALBUM = ITEMS.register("album",
            () -> new AlbumItem(new Item.Properties().stacksTo(1)));

    // Предмет изрисованной страницы (использует кастомный класс, который мы напишем ниже)
    public static final RegistryObject<Item> SKETCHED_PAGE = ITEMS.register("sketched_page",
            () -> new SketchedPageItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PENCIL = ITEMS.register("pencil",
            () -> new DrawingToolItem(new Item.Properties().durability(256)) {
                @Override
                public net.minecraft.resources.ResourceLocation getGuiTexture() {
                    return new net.minecraft.resources.ResourceLocation(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/button_pencil.png");
                }

                // Если карандаш длиннее/шире, просто переопределяем методы:
                @Override
                public int getGuiWidth() {
                    return 16;
                } // Например, оставим 16

                @Override
                public int getPeekHeight() {
                    return 12;
                } // Пусть обычный карандаш выглядывает сильнее!
            });

    public static final RegistryObject<Item> ERASER = ITEMS.register("eraser",
            () -> new DrawingToolItem(new Item.Properties().durability(256)) {
                @Override
                public net.minecraft.resources.ResourceLocation getGuiTexture() {
                    return new net.minecraft.resources.ResourceLocation(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/button_eraser.png");
                }

                @Override
                public int getPeekHeight() {
                    return 6;
                } // А ластик пусть торчит поменьше
            });

    public static final RegistryObject<Item> KNEADED_ERASER = ITEMS.register("kneaded_eraser",
            () -> new DrawingToolItem(new Item.Properties().durability(256)) {
                @Override
                public net.minecraft.resources.ResourceLocation getGuiTexture() {
                    return new net.minecraft.resources.ResourceLocation(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/button_kneaded_eraser.png");
                }

                @Override
                public int getPeekHeight() {
                    return 6;
                }
            });

    public static final RegistryObject<Item> COLOR_PENCIL = ITEMS.register("color_pencil",
            () -> new ColorPencilItem(new Item.Properties()
                    .stacksTo(1))); // вместо .durability(256)

    public static final RegistryObject<Item> COLOR_MARKER = ITEMS.register("color_marker",
            () -> new ColorMarkerItem(new Item.Properties()
                    .stacksTo(1))); // вместо .durability(256)

    public static final RegistryObject<Item> SMUDGE = ITEMS.register("smudge",
            () -> new DrawingToolItem(new Item.Properties().durability(256)) {
                @Override
                public net.minecraft.resources.ResourceLocation getGuiTexture() {
                    return new net.minecraft.resources.ResourceLocation(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/button_blender.png");
                }

                @Override
                public int getPeekHeight() {
                    return 10;
                }
            });

    public static final RegistryObject<Item> EMPTY_PAGE = ITEMS.register("empty_page",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> RULER = ITEMS.register("ruler",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRAWING_COMPASS = ITEMS.register("drawing_compass",
            () -> new DrawingCompassItem(new Item.Properties()
                    .durability(256)
                    // По умолчанию игла не стоит (x=-1), и он не рисует
            ));

    public static final RegistryObject<Item> PENCIL_CASE = ITEMS.register("pencil_case",
            () -> new PencilCaseItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> MAGNIFYING_GLASS = ITEMS.register("magnifying_glass",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STAMP_SMALL = ITEMS.register("stamp_small",
            () -> new StampItem(new Item.Properties().stacksTo(1), 16));

    public static final RegistryObject<Item> STAMP_LARGE = ITEMS.register("stamp_large",
            () -> new StampItem(new Item.Properties().stacksTo(1), 32));

    public static final RegistryObject<Item> STAMP_PAD = ITEMS.register("stamp_pad",
            () -> new StampPadItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PRINTER = ITEMS.register("printer_block",
            () -> new net.minecraft.world.item.BlockItem(
                    ModBlocks.PRINTER_BLOCK.get(),
                    new Item.Properties()));

    public static final RegistryObject<Item> WARP_ESSENCE = ITEMS.register("warp_essence",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
