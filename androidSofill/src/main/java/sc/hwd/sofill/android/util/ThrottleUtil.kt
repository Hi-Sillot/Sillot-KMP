package sc.hwd.sofill.android.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class ThrottleUtil(private val scope: CoroutineScope = GlobalScope, val time: Long = 100L) {
    private var job: Job? = null

    fun runAction(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        action: suspend () -> Unit,
    ) {
        job?.cancel()
        job = null
        job = scope.launch(dispatcher) {
            delay(time)
            action.invoke()
        }
    }
}