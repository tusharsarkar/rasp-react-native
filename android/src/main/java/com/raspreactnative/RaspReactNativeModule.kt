package com.raspreactnative

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.concurrent.Executors

class RaspReactNativeModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val executor = Executors.newSingleThreadExecutor()

  override fun getName(): String = "RaspReactNative"

  /**
   * Runs the current checks (root, debugger, app integrity, hooking) off
   * the UI thread and emits a `threatDetected` event per positive hit.
   */
  @ReactMethod
  fun startRasp(config: ReadableMap) {
    executor.execute {
      val context = reactApplicationContext

      if (RootDetector.isDeviceRooted(context)) {
        emit("privilegedAccess")
      }
      if (DebuggerDetector.isDebuggerAttached()) {
        emit("debug")
      }
      if (HookDetector.isHookingDetected(context)) {
        emit("hooks")
      }

      RaspConfigParser.parseAndroidConfig(config)?.let { androidConfig ->
        if (AppIntegrityDetector.isTampered(context, androidConfig)) {
          emit("appIntegrity")
        }
        if (UnofficialStoreDetector.isUnofficialStore(context, androidConfig.supportedAlternativeStores)) {
          emit("unofficialStore")
        }
      }
    }
  }

  private fun emit(threatType: String) {
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("threatDetected", threatType)
  }
}
