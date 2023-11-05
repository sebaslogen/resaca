package com.sebaslogen.resacaapp.sample.ui.main.data

import com.sebaslogen.resacaapp.sample.closeableClosedGloballySharedCounter
import java.io.Closeable

class FakeRepo : Closeable {
    var someData: String = "Some fake data"

    override fun close() {
        closeableClosedGloballySharedCounter.incrementAndGet()
    }
}