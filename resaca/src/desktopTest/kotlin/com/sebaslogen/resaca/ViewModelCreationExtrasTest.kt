@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Companion.VIEW_MODEL_KEY
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ViewModelCreationExtrasTest {

    // region helpers

    private fun plainOwner() = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    private fun ownerWithDefaultExtras(extras: CreationExtras) =
        object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
            override val viewModelStore: ViewModelStore = ViewModelStore()
            override val defaultViewModelProviderFactory: ViewModelProvider.Factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                        throw UnsupportedOperationException("dummy")
                    }
                }
            override val defaultViewModelCreationExtras: CreationExtras = extras
        }

    // endregion

    // region getCreationExtras

    @Test
    internal fun `getCreationExtras with plain owner returns Empty`() {
        val owner = plainOwner()

        val extras = owner.getCreationExtras()

        assertEquals(CreationExtras.Empty, extras)
    }

    @Test
    internal fun `getCreationExtras with HasDefaultViewModelProviderFactory returns defaultViewModelCreationExtras`() {
        val expectedExtras = MutableCreationExtras()
        expectedExtras[VIEW_MODEL_KEY] = "someKey"
        val owner = ownerWithDefaultExtras(expectedExtras)

        val extras = owner.getCreationExtras()

        assertEquals("someKey", extras[VIEW_MODEL_KEY])
    }

    // endregion

    // region addViewModelKey

    @Test
    internal fun `addViewModelKey adds key when absent`() {
        val extras = CreationExtras.Empty

        val result = extras.addViewModelKey("myKey")

        assertEquals("myKey", result[VIEW_MODEL_KEY])
    }

    @Test
    internal fun `addViewModelKey preserves existing key`() {
        val existing = MutableCreationExtras()
        existing[VIEW_MODEL_KEY] = "existingKey"

        val result = existing.addViewModelKey("newKey")

        // Should NOT overwrite the existing key
        assertEquals("existingKey", result[VIEW_MODEL_KEY])
    }

    @Test
    internal fun `addViewModelKey with no existing key creates MutableCreationExtras with key`() {
        val result = CreationExtras.Empty.addViewModelKey("testKey")

        assertNotNull(result)
        assertEquals("testKey", result[VIEW_MODEL_KEY])
    }

    // endregion
}
