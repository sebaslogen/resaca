package com.sebaslogen.resacaapp.ui.main.data

import com.sebaslogen.resacaapp.closeableClosedGloballySharedCounter
import java.io.Closeable

class FakeRepo : Closeable {
    var counter = 0

    override fun close() {
        closeableClosedGloballySharedCounter.incrementAndGet()
    }
}