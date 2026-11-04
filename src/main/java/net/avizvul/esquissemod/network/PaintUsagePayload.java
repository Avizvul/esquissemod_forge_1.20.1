package net.avizvul.esquissemod.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public record PaintUsagePayload(int toolId, List<Integer> units) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.toolId);
        buf.writeVarIntArray(this.units.stream().mapToInt(Integer::intValue).toArray());
    }

    public static PaintUsagePayload decode(FriendlyByteBuf buf) {
        return new PaintUsagePayload(buf.readInt(), java.util.Arrays.stream(buf.readVarIntArray()).boxed().toList());
    }
}
