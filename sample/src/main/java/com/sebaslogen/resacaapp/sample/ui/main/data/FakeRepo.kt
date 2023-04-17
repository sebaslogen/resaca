package com.sebaslogen.resacaapp.sample.ui.main.data

import com.sebaslogen.resacaapp.sample.closeableClosedGloballySharedCounter
import java.io.Closeable

class FakeRepo : Closeable {
    var counter = 0

    override fun close() {
        closeableClosedGloballySharedCounter.incrementAndGet()
    }
}