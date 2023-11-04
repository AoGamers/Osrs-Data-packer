package io.blurite.api

import io.netty.buffer.ByteBuf

object ByteBuff {

    fun ByteBuf.writeNullableLargeSmartCorrect(value: Int?): ByteBuf = when {
        value == null -> {
            writeShort(32767)
        }
        value < Short.MAX_VALUE -> {
            writeShort(value)
        }
        else -> {
            writeInt(value)
            val writtenValue = getByte(writerIndex() - 4)
            setByte(writerIndex() - 4, writtenValue + 0x80)
        }
    }

    fun ByteBuf.readNullableLargeSmart(): Int {
        return if (this.array()[this.writerIndex()] < 0) {
            this.readInt() and Int.MAX_VALUE
        } else {
            val var1: Int = this.readUnsignedShort()
            if (var1 == 32767) -1 else var1
        }
    }


}