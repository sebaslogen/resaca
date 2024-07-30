import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import com.sebaslogen.resaca.rememberScoped

@Composable
public actual fun <T : Any, K : Any> rememberScopedMP(key: K): T {
    TODO("Not yet implemented")
}

@Composable
public actual fun <T : Any> rememberScoped(key: Any?, builder: @DisallowComposableCalls () -> T): T =
    rememberScoped(key, builder) // TODO: use resaca library