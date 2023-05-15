package com.mark.netrune.buffer

import kotlin.reflect.KProperty

class ReadableBufferIntLeDelegate : ReadableBufferDelegate {

    var value = Int.MIN_VALUE

    override fun read(buffer: ReadableBuffer) {
        value = buffer.readIntLe()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value

}