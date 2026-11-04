package net.avizvul.esquissemod.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraftforge.network.filters.ForgeConnectionNetworkFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/** Из коробки Forge делит только рецепты, теги, достижения и логин. Добавляем пакеты, несущие предметы и рисунки. */
@Mixin(value = ForgeConnectionNetworkFilter.class, remap = false)
public abstract class ForgeConnectionNetworkFilterMixin {

    private static final int ESQUISSE_MAX_BUNDLE_SIZE = 1024 * 1024;

    @Shadow
    private static void splitPacket(Packet<?> packet, List<? super Packet<?>> out) {
        throw new AssertionError();
    }

    @Inject(method = "buildHandlers", at = @At("RETURN"), cancellable = true)
    private static void esquissemod$splitItemPackets(Connection connection,
            CallbackInfoReturnable<Map<Class<? extends Packet<?>>, BiConsumer<Packet<?>, List<? super Packet<?>>>>> cir) {
        // Пустая карта: на той стороне нет Forge, части собрать некому
        if (cir.getReturnValue().isEmpty()) return;

        Map<Class<? extends Packet<?>>, BiConsumer<Packet<?>, List<? super Packet<?>>>> handlers = new HashMap<>(cir.getReturnValue());
        BiConsumer<Packet<?>, List<? super Packet<?>>> split = (packet, out) -> splitPacket(packet, out);
        handlers.put(ClientboundContainerSetContentPacket.class, split);
        handlers.put(ClientboundContainerSetSlotPacket.class, split);
        handlers.put(ClientboundSetEquipmentPacket.class, split);
        handlers.put(ClientboundSetEntityDataPacket.class, split);
        handlers.put(ClientboundBlockEntityDataPacket.class, split);
        handlers.put(ClientboundLevelChunkWithLightPacket.class, split);
        handlers.put(ClientboundBundlePacket.class, (packet, out) -> esquissemod$splitBundle((ClientboundBundlePacket) packet, out));
        cir.setReturnValue(handlers);
    }

    /** Появление сущности идёт бандлом, поэтому предмет на земле иначе минует разбивку. */
    private static void esquissemod$splitBundle(ClientboundBundlePacket bundle, List<? super Packet<?>> out) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (Packet<?> sub : bundle.subPackets()) sub.write(buf);
            if (buf.readableBytes() <= ESQUISSE_MAX_BUNDLE_SIZE) {
                out.add(bundle);
                return;
            }
        } finally {
            buf.release();
        }
        for (Packet<?> sub : bundle.subPackets()) splitPacket(sub, out);
    }
}
