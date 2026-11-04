package net.avizvul.esquissemod.network;

import io.netty.buffer.Unpooled;
import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class ModNetwork {
    private ModNetwork() {}

    private static final String PROTOCOL = "1";
    private static final byte KIND_SAVE = 0;
    private static final byte KIND_TEAR = 1;
    // Рисунок со всем текстом точно меньше: защита от клиента, который шлёт части без конца
    private static final int MAX_ASSEMBLED = 1024 * 1024;
    private static final int MAX_PARTS = MAX_ASSEMBLED / PayloadPart.MAX_DATA + 1;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EsquisseMod.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static final SketchbookPayloadHandler HANDLER = new SketchbookPayloadHandler();
    private static final Map<UUID, Assembly> ASSEMBLIES = new HashMap<>();
    private static int nextId = 0;

    private static final class Assembly {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int next = 0;
    }

    public static void register() {
        toServer(SketchbookSavePayload.class, SketchbookSavePayload::encode, SketchbookSavePayload::decode, HANDLER::handleData);
        toServer(TearPagePayload.class, TearPagePayload::encode, TearPagePayload::decode, HANDLER::handleTearPage);
        toServer(ChangeColorPayload.class, ChangeColorPayload::encode, ChangeColorPayload::decode, HANDLER::handleChangeColor);
        toServer(ToolSettingsPayload.class, ToolSettingsPayload::encode, ToolSettingsPayload::decode, HANDLER::handleToolSettings);
        toServer(PrinterActionPayload.class, PrinterActionPayload::encode, PrinterActionPayload::decode, HANDLER::handlePrinterAction);
        toServer(ToolUsagePayload.class, ToolUsagePayload::encode, ToolUsagePayload::decode, HANDLER::handleToolUsage);
        toServer(PaintUsagePayload.class, PaintUsagePayload::encode, PaintUsagePayload::decode, HANDLER::handlePaintUsage);
        toServer(SignPagePayload.class, SignPagePayload::encode, SignPagePayload::decode, HANDLER::handleSignPage);
        toServer(SealStampPayload.class, SealStampPayload::encode, SealStampPayload::decode, HANDLER::handleSealStamp);
        toServer(PayloadPart.class, PayloadPart::encode, PayloadPart::decode, ModNetwork::handlePart);

        CHANNEL.registerMessage(nextId++, ExportSketchPayload.class, ExportSketchPayload::encode, ExportSketchPayload::decode,
                (payload, context) -> {
                    context.get().enqueueWork(() ->
                            net.avizvul.esquissemod.util.SketchExporter.exportSketchToScreenshots(payload.sketchData()));
                    context.get().setPacketHandled(true);
                }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) ->
                ASSEMBLIES.remove(event.getEntity().getUUID()));
    }

    private static <M> void toServer(Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder,
                                     Function<FriendlyByteBuf, M> decoder, BiConsumer<M, PayloadContext> handler) {
        CHANNEL.registerMessage(nextId++, type, encoder, decoder, (payload, supplier) -> {
            NetworkEvent.Context context = supplier.get();
            handler.accept(payload, new PayloadContext(context.getSender(), context::enqueueWork));
            context.setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sendToServer(Object payload) {
        if (payload instanceof SketchbookSavePayload save) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            save.encode(buf);
            sendLarge(payload, KIND_SAVE, buf);
        } else if (payload instanceof TearPagePayload tear) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            tear.encode(buf);
            sendLarge(payload, KIND_TEAR, buf);
        } else {
            CHANNEL.sendToServer(payload);
        }
    }

    private static void sendLarge(Object payload, byte kind, FriendlyByteBuf buf) {
        int size = buf.readableBytes();
        if (size <= PayloadPart.MAX_DATA) {
            buf.release();
            CHANNEL.sendToServer(payload);
            return;
        }
        int count = (size + PayloadPart.MAX_DATA - 1) / PayloadPart.MAX_DATA;
        for (int i = 0; i < count; i++) {
            byte[] data = new byte[Math.min(PayloadPart.MAX_DATA, buf.readableBytes())];
            buf.readBytes(data);
            CHANNEL.sendToServer(new PayloadPart(kind, i, count, data));
        }
        buf.release();
    }

    public static void sendToPlayer(ServerPlayer player, Object payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    private static void handlePart(PayloadPart part, PayloadContext context) {
        context.enqueueWork(() -> {
            UUID id = context.player().getUUID();
            Assembly assembly = part.index() == 0 ? new Assembly() : ASSEMBLIES.get(id);
            if (assembly == null || assembly.next != part.index() || part.count() > MAX_PARTS
                    || assembly.bytes.size() + part.data().length > MAX_ASSEMBLED) {
                ASSEMBLIES.remove(id);
                return;
            }
            ASSEMBLIES.put(id, assembly);
            assembly.bytes.writeBytes(part.data());
            assembly.next++;
            if (assembly.next < part.count()) return;

            ASSEMBLIES.remove(id);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(assembly.bytes.toByteArray()));
            PayloadContext immediate = new PayloadContext(context.player(), Runnable::run);
            if (part.kind() == KIND_SAVE) HANDLER.handleData(SketchbookSavePayload.decode(buf), immediate);
            else if (part.kind() == KIND_TEAR) HANDLER.handleTearPage(TearPagePayload.decode(buf), immediate);
        });
    }
}
