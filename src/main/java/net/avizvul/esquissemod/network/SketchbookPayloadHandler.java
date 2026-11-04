package net.avizvul.esquissemod.network;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.component.SketchData;
import net.avizvul.esquissemod.item.ModItems;
import net.avizvul.esquissemod.menu.PrinterMenu;
import net.avizvul.esquissemod.util.ToolWear;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.avizvul.esquissemod.component.ItemContainerContents;
import net.minecraftforge.items.IItemHandler;
import net.avizvul.esquissemod.network.PayloadContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.avizvul.esquissemod.item.custom.StampItem;

import java.util.ArrayList;
import java.util.List;

public class SketchbookPayloadHandler {

    // Helper для создания 16 полных страниц (126x192)
    private static List<SketchData> createDefaultPages() {
        List<SketchData> list = new ArrayList<>(16);
        SketchData emptyData = SketchData.fromArray(new int[126][192]);
        for (int i = 0; i < 16; i++) {
            list.add(emptyData);
        }
        return list;
    }

    public void handleData(final SketchbookSavePayload payload, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.SKETCHBOOK.get())) {
                stack = player.getOffhandItem();
            }

            if (stack.is(ModItems.SKETCHBOOK.get())) {
                List<SketchData> pages = new ArrayList<>(ModDataComponents.SKETCHBOOK_PAGES.getOrDefault(stack, new ArrayList<>()));
                if (pages.isEmpty()) {
                    pages = createDefaultPages();
                }

                if (payload.pageIndex() >= 0 && payload.pageIndex() < pages.size()) {
                    pages.set(payload.pageIndex(), payload.sketchData());
                    ModDataComponents.SKETCHBOOK_PAGES.set(stack, pages);
                    ModDataComponents.LAST_PAGE.set(stack, payload.pageIndex());
                }
            }

            if (player.level() instanceof ServerLevel) {
                applyWear(player, payload.pencilPixels(), payload.eraserPixels(),
                        payload.kneadedPixels(), payload.smudgePixels());
            }
        });
    }

    public void handlePaintUsage(final PaintUsagePayload payload, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.getAbilities().instabuild) return;
            Item target = paintToolItem(payload.toolId());
            if (target == null) return;
            modifyTool(player, target, stack -> ToolWear.consumePaint(stack, payload.units()));
        });
    }

    private static void applyWear(Player player, int pencil, int eraser, int kneaded, int smudge) {
        damageTool(player, ModItems.PENCIL.get(), pencil / ToolWear.pixelsPerDamage(ModItems.PENCIL.get()));
        damageTool(player, ModItems.ERASER.get(), eraser / ToolWear.pixelsPerDamage(ModItems.ERASER.get()));
        damageTool(player, ModItems.KNEADED_ERASER.get(), kneaded / ToolWear.pixelsPerDamage(ModItems.KNEADED_ERASER.get()));
        damageTool(player, ModItems.SMUDGE.get(), smudge / ToolWear.pixelsPerDamage(ModItems.SMUDGE.get()));
    }

    public void handleToolUsage(final ToolUsagePayload payload, final PayloadContext context) {
        context.enqueueWork(() -> applyWear(context.player(),
                payload.pencilPixels(), payload.eraserPixels(), payload.kneadedPixels(), payload.smudgePixels()));
    }

    private static void damageTool(Player player, Item toolItem, int amount) {
        if (amount <= 0) return;
        if (!ToolWear.WEAR_IN_CREATIVE && player.getAbilities().instabuild) return;

        boolean found = modifyTool(player, toolItem, stack -> {
            int newDamage = stack.getDamageValue() + amount;
            if (newDamage >= stack.getMaxDamage()) {
                stack.shrink(1); // инструмент сломался
                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8f, 1.0f);
            } else {
                stack.setDamageValue(newDamage);
            }
        });
        EsquisseMod.LOGGER.info("Wear {} x{} applied={}", toolItem, amount, found);
    }

    /** Ищет первый инструмент в инвентаре (включая левую руку) и внутри пенала и применяет к нему действие. */
    private static boolean modifyTool(Player player, Item target, java.util.function.Consumer<ItemStack> action) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(target)) {
                action.accept(stack);
                return true;
            }
            if (stack.is(ModItems.PENCIL_CASE.get()) && net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.has(stack)) {
                ItemContainerContents contents = net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.get(stack);
                if (contents == null) continue;
                NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
                contents.copyInto(items);
                for (ItemStack inner : items) {
                    if (inner.is(target)) {
                        action.accept(inner); // inner — копия, поэтому ниже перезаписываем контейнер
                        net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.set(stack, ItemContainerContents.fromItems(items));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void handleTearPage(final TearPagePayload payload, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.SKETCHBOOK.get())) {
                stack = player.getOffhandItem();
            }

            if (stack.is(ModItems.SKETCHBOOK.get())) {
                List<SketchData> pages = new ArrayList<>(ModDataComponents.SKETCHBOOK_PAGES.getOrDefault(stack, new ArrayList<>()));
                if (pages.isEmpty()) {
                    pages = createDefaultPages();
                }

                if (payload.pageIndex() >= 0 && payload.pageIndex() < pages.size()) {
                    pages.remove(payload.pageIndex());

                    SketchData tornData = payload.sketchData();
                    ItemStack tornPage;

                    if (tornData == null || tornData.isEmpty()) {
                        tornPage = new ItemStack(ModItems.EMPTY_PAGE.get());
                    } else {
                        tornPage = new ItemStack(ModItems.SKETCHED_PAGE.get());
                        ModDataComponents.PAGE_DATA.set(tornPage, tornData);
                        ModDataComponents.PAGE_AUTHOR.set(tornPage, player.getGameProfile().getName());
                    }

                    if (!player.getInventory().add(tornPage)) {
                        player.drop(tornPage, false);
                    }

                    if (pages.isEmpty()) {
                        stack.shrink(1);
                    } else {
                        ModDataComponents.SKETCHBOOK_PAGES.set(stack, pages);
                        int lastPage = ModDataComponents.LAST_PAGE.getOrDefault(stack, 0);
                        if (lastPage >= pages.size()) {
                            ModDataComponents.LAST_PAGE.set(stack, pages.size() - 1);
                        }
                    }
                }
            }
        });
    }

    public void handleChangeColor(final ChangeColorPayload payload, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Item targetItem = paintToolItem(payload.toolId());
            if (targetItem == null) return;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(targetItem)) {
                    ModDataComponents.ACTIVE_COLOR_INDEX.set(stack, payload.colorIndex());
                    return;
                }
                if (stack.is(ModItems.PENCIL_CASE.get()) && net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.has(stack)) {
                    ItemContainerContents contents = net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.get(stack);
                    if (contents != null) {
                        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
                        contents.copyInto(items);
                        for (int j = 0; j < items.size(); j++) {
                            ItemStack innerStack = items.get(j);
                            if (innerStack.is(targetItem)) {
                                ModDataComponents.ACTIVE_COLOR_INDEX.set(innerStack, payload.colorIndex());
                                net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.set(stack, ItemContainerContents.fromItems(items));
                                return;
                            }
                        }
                    }
                }
            }
        });
    }

    public void handleToolSettings(final ToolSettingsPayload payload, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Item targetItem = switch (payload.toolType()) {
                case 0 -> ModItems.PENCIL.get();
                case 1 -> ModItems.COLOR_PENCIL.get();
                case 2 -> ModItems.ERASER.get();
                case 3 -> ModItems.SMUDGE.get();
                case 4 -> ModItems.KNEADED_ERASER.get();
                case 5 -> ModItems.COLOR_MARKER.get();
                default -> null;
            };

            if (targetItem == null) return;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(targetItem)) {
                    ModDataComponents.BRUSH_SIZE.set(stack, payload.size());
                    ModDataComponents.BRUSH_HARDNESS.set(stack, payload.hardness());
                    if (targetItem == ModItems.COLOR_MARKER.get()) {
                        ModDataComponents.MARKER_ROTATION.set(stack, payload.rotation());
                    }
                    return;
                }
                if (stack.is(ModItems.PENCIL_CASE.get()) && net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.has(stack)) {
                    ItemContainerContents contents = net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.get(stack);
                    if (contents != null) {
                        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
                        contents.copyInto(items);
                        for (int j = 0; j < items.size(); j++) {
                            ItemStack innerStack = items.get(j);
                            if (innerStack.is(targetItem)) {
                                ModDataComponents.BRUSH_SIZE.set(innerStack, payload.size());
                                ModDataComponents.BRUSH_HARDNESS.set(innerStack, payload.hardness());
                                if (targetItem == ModItems.COLOR_MARKER.get()) {
                                    ModDataComponents.MARKER_ROTATION.set(innerStack, payload.rotation());
                                }
                                net.avizvul.esquissemod.component.ModDataComponents.CONTAINER.set(stack, ItemContainerContents.fromItems(items));
                                return;
                            }
                        }
                    }
                }
            }
        });
    }

    public void handleSealStamp(final SealStampPayload payload, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = payload.offHand() ? player.getOffhandItem() : player.getMainHandItem();
            if (!(stack.getItem() instanceof StampItem stamp)) return;
            if (StampItem.isSealed(stack)) return; // после запечатывания менять нельзя

            int size = stamp.getSize();
            if (payload.rows().size() != size) return;

            int mask = size >= 32 ? -1 : (1 << size) - 1;
            List<Integer> clean = new ArrayList<>(size);
            boolean any = false;
            for (int row : payload.rows()) {
                int v = row & mask;
                if (v != 0) any = true;
                clean.add(v);
            }
            if (!any) return; // пустой штамп запечатать нельзя

            ModDataComponents.STAMP_BITS.set(stack, clean);

            String title = payload.name().replace("§", "").trim();
            if (title.length() > 32) title = title.substring(0, 32);
            if (!title.isEmpty()) {
                net.avizvul.esquissemod.component.ModDataComponents.CUSTOM_NAME.set(stack,
                        Component.literal("\"" + title + "\"").withStyle(ChatFormatting.ITALIC));
            }

            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.INK_SAC_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        });
    }

    /** Переносит автора и название оригинала на копию. */
    private static void copyPageMeta(ItemStack from, ItemStack to) {
        String author = ModDataComponents.PAGE_AUTHOR.get(from);
        if (author != null) ModDataComponents.PAGE_AUTHOR.set(to, author);

        if (ModDataComponents.PAGE_SIGNED.getOrDefault(from, false)) {
            ModDataComponents.PAGE_SIGNED.set(to, true);
            Component name = net.avizvul.esquissemod.component.ModDataComponents.CUSTOM_NAME.get(from);
            if (name != null) net.avizvul.esquissemod.component.ModDataComponents.CUSTOM_NAME.set(to, name);
        }
    }

    public void handlePrinterAction(final PrinterActionPayload payload, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.containerMenu instanceof PrinterMenu printerMenu) {
                IItemHandler inv = printerMenu.getInventory();
                ItemStack sourcePage = inv.getStackInSlot(0);
                ItemStack paper = inv.getStackInSlot(1);
                ItemStack outputSlot = inv.getStackInSlot(2);
                ItemStack cyanDye = inv.getStackInSlot(3);
                ItemStack magentaDye = inv.getStackInSlot(4);
                ItemStack yellowDye = inv.getStackInSlot(5);
                ItemStack blackDye = inv.getStackInSlot(6);
                ItemStack catalyst = inv.getStackInSlot(7);

                if (sourcePage.isEmpty() || !sourcePage.is(ModItems.SKETCHED_PAGE.get())) return;
                SketchData sketchData = ModDataComponents.PAGE_DATA.get(sourcePage);
                if (sketchData == null || sketchData.isEmpty()) return;

                boolean isWarpEssence = !catalyst.isEmpty() && catalyst.is(ModItems.WARP_ESSENCE.get());
                boolean isPaperCatalyst = !catalyst.isEmpty() && catalyst.is(Items.GLOWSTONE_DUST);

                if (!isWarpEssence && !isPaperCatalyst) return;

                if (isPaperCatalyst) {
                    boolean hasPaper = !paper.isEmpty() && (paper.is(ModItems.EMPTY_PAGE.get()) || paper.is(Items.PAPER));
                    if (!hasPaper || !outputSlot.isEmpty()) return;
                }

                boolean hasAllDyes = !cyanDye.isEmpty() && !magentaDye.isEmpty() && !yellowDye.isEmpty() && !blackDye.isEmpty();
                if (!hasAllDyes) return;

                cyanDye.shrink(1);
                magentaDye.shrink(1);
                yellowDye.shrink(1);
                blackDye.shrink(1);
                catalyst.shrink(1);

                if (isWarpEssence) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        net.avizvul.esquissemod.network.ModNetwork.sendToPlayer(serverPlayer, new ExportSketchPayload(sketchData));
                    }
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 1.2f);
                } else {
                    paper.shrink(1);
                    ItemStack printedPage = new ItemStack(ModItems.SKETCHED_PAGE.get());
                    ModDataComponents.PAGE_DATA.set(printedPage, sketchData);
                    copyPageMeta(sourcePage, printedPage);
                    ModDataComponents.PAGE_COPIED.set(printedPage, true);
                    inv.insertItem(2, printedPage, false);
                }
            }
        });
    }

    public void handleSignPage(final SignPagePayload payload, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = payload.offHand() ? player.getOffhandItem() : player.getMainHandItem();
            if (!stack.is(ModItems.SKETCHED_PAGE.get())) return;
            if (ModDataComponents.PAGE_SIGNED.getOrDefault(stack, false)) return;

            // Сервер не доверяет клиенту: чистим форматирующие коды и ограничиваем длину
            String title = payload.title().replace("§", "").trim();
            if (title.isEmpty()) return;
            if (title.length() > 32) title = title.substring(0, 32);

            net.avizvul.esquissemod.component.ModDataComponents.CUSTOM_NAME.set(stack,
                    Component.literal("\"" + title + "\"").withStyle(ChatFormatting.ITALIC));
            ModDataComponents.PAGE_SIGNED.set(stack, true);

            // Автор обычно записан при отрыве; если нет (старая страница), автором становится подписавший
            if (!ModDataComponents.PAGE_AUTHOR.has(stack)) {
                ModDataComponents.PAGE_AUTHOR.set(stack, player.getGameProfile().getName());
            }

            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.INK_SAC_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        });
    }

    private static Item paintToolItem(int toolId) {
        return switch (toolId) {
            case 0 -> ModItems.COLOR_PENCIL.get();
            case 1 -> ModItems.COLOR_MARKER.get();
            case 2 -> ModItems.STAMP_PAD.get();
            default -> null;
        };
    }

}
