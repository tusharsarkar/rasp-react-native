package com.raspreactnative

import android.content.Context
import android.os.Build
import java.io.File

/**
 * Heuristic root detection: no single check is conclusive on its own (a
 * hidden-root module can defeat any one of these), so callers should treat
 * this as "likely rooted" rather than a cryptographic guarantee.
 */
object RootDetector {

  private val KNOWN_ROOT_APPS = arrayOf(
    "com.noshufou.android.su",
    "com.noshufou.android.su.elite",
    "eu.chainfire.supersu",
    "com.koushikdutta.superuser",
    "com.thirdparty.superuser",
    "com.yellowes.su",
    "com.topjohnwu.magisk",
    "com.kingroot.kingmaster",
    "com.kingo.root",
    "com.smedialink.oneclickroot",
    "com.zhiqupk.root.global",
    "com.alephzain.framaroot"
  )

  private val KNOWN_SU_PATHS = arrayOf(
    "/system/app/Superuser.apk",
    "/sbin/su",
    "/system/bin/su",
    "/system/xbin/su",
    "/data/local/xbin/su",
    "/data/local/bin/su",
    "/system/sd/xbin/su",
    "/system/bin/failsafe/su",
    "/data/local/su",
    "/su/bin/su"
  )

  fun isDeviceRooted(context: Context): Boolean {
    return hasTestKeysBuildTag() ||
      hasKnownRootBinaries() ||
      hasKnownRootPackages(context) ||
      canExecuteSuCommand()
  }

  private fun hasTestKeysBuildTag(): Boolean {
    val tags = Build.TAGS
    return tags != null && tags.contains("test-keys")
  }

  private fun hasKnownRootBinaries(): Boolean =
    KNOWN_SU_PATHS.any { File(it).exists() }

  private fun hasKnownRootPackages(context: Context): Boolean {
    val pm = context.packageManager
    return KNOWN_ROOT_APPS.any { packageName ->
      try {
        pm.getPackageInfo(packageName, 0)
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  private fun canExecuteSuCommand(): Boolean {
    return try {
      val process = ProcessBuilder("which", "su").redirectErrorStream(true).start()
      val output = process.inputStream.bufferedReader().readLine()
      process.destroy()
      !output.isNullOrEmpty()
    } catch (e: Exception) {
      false
    }
  }
}
