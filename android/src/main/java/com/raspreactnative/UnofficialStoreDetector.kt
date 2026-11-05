package com.raspreactnative

import android.content.Context
import android.os.Build

/**
 * Flags an install that didn't come through Google Play or one of
 * `androidConfig.supportedAlternativeStores` — e.g. a sideloaded APK. A
 * `null` installer (common for `adb install`, or a device with no store at
 * all) is treated as unofficial.
 */
object UnofficialStoreDetector {
  private const val GOOGLE_PLAY_PACKAGE = "com.android.vending"

  fun isUnofficialStore(context: Context, supportedAlternativeStores: List<String>): Boolean {
    val installer = installerPackageName(context) ?: return true
    val allowed = supportedAlternativeStores + GOOGLE_PLAY_PACKAGE
    return !allowed.contains(installer)
  }

  private fun installerPackageName(context: Context): String? {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
      } else {
        @Suppress("DEPRECATION")
        context.packageManager.getInstallerPackageName(context.packageName)
      }
    } catch (e: Exception) {
      null
    }
  }
}
