// Note: This package is used to be able to access ViewModel's protected clear method
package androidx.lifecycle

/**
 * The purpose of this object is simply to be able to call the protected [ViewModel.clear] method from this library
 */
internal object ViewModelClearer {
    fun clearViewModel(viewModel: ViewModel) {
        viewModel.clear()
    }
}