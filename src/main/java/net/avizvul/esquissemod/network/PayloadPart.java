package net.avizvul.esquissemod.network;

import net.minecraft.network.FriendlyByteBuf;

/** Часть большого пакета клиент -> сервер: у такого пакета лимит 32767 байт, а страница весит до 97 КБ. */
public record PayloadPart(byte kind, int index, int count, byte[] data) {
    public static final int MAX_DATA = 30000;

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(this.kind);
        buf.writeVarInt(this.index);
        buf.writeVarInt(this.count);
        buf.writeByteArray(this.data);
    }

    public static PayloadPart decode(FriendlyByteBuf buf) {
        return new PayloadPart(buf.readByte(), buf.readVarInt(), buf.readVarInt(), buf.readByteArray(MAX_DATA));
    }
}
