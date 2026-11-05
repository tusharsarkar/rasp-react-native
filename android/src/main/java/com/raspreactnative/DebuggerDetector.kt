package com.raspreactnative

import android.os.Debug

object DebuggerDetector {
  fun isDebuggerAttached(): Boolean =
    Debug.isDebuggerConnected() || Debug.waitingForDebugger()
}
