package net.avizvul.esquissemod.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record TextElement(String text, int x, int y, float scale, int color, float rotation) {

    public TextElement(String text, int x, int y, float scale, int color) {
        this(text, x, y, scale, color, 0.0f);
    }

    public TextElement(String text, int x, int y, float scale, int color, boolean isVertical) {
        this(text, x, y, scale, color, isVertical ? 90.0f : 0.0f);
    }

    public boolean isVertical() {
        return Math.abs(this.rotation % 180.0f - 90.0f) < 1.0f;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("text", this.text);
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putFloat("scale", this.scale);
        tag.putInt("color", this.color);
        tag.putFloat("rotation", this.rotation);
        return tag;
    }

    public static TextElement fromTag(CompoundTag tag) {
        return new TextElement(tag.getString("text"), tag.getInt("x"), tag.getInt("y"),
                tag.getFloat("scale"), tag.getInt("color"), tag.getFloat("rotation"));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.text);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeFloat(this.scale);
        buf.writeInt(this.color);
        buf.writeFloat(this.rotation);
    }

    public static TextElement read(FriendlyByteBuf buf) {
        return new TextElement(buf.readUtf(), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readInt(), buf.readFloat());
    }
}
