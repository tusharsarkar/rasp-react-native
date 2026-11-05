package com.raspreactnative

import android.content.Context
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Heuristic hooking-framework detection (Frida, Xposed/LSPosed, Substrate).
 * As with root detection, a determined attacker can rename binaries/libs or
 * use a custom Frida build to dodge any one signal here — these checks are
 * meant to catch default/common setups, not provide a hard guarantee.
 */
object HookDetector {

  private val SUSPICIOUS_MAP_KEYWORDS = arrayOf(
    "frida",
    "gum-js-loop",
    "gmain",
    "linjector",
    "xposed",
    "substrate",
    "cynject"
  )

  private val XPOSED_PACKAGES = arrayOf(
    "de.robv.android.xposed.installer",
    "org.meowcat.edxposed.manager",
    "io.github.lsposed.manager",
    "com.solohsu.android.edxp.manager",
    "top.canyie.dreamland.manager"
  )

  private val FRIDA_DEFAULT_PORTS = intArrayOf(27042, 27043)

  fun isHookingDetected(context: Context): Boolean {
    return hasSuspiciousLoadedLibraries() ||
      hasXposedPackages(context) ||
      hasXposedBridgeClass() ||
      hasFridaServerListening()
  }

  private fun hasSuspiciousLoadedLibraries(): Boolean {
    return try {
      BufferedReader(FileReader("/proc/self/maps")).use { reader ->
        reader.lineSequence().any { line ->
          val lower = line.lowercase()
          SUSPICIOUS_MAP_KEYWORDS.any { lower.contains(it) }
        }
      }
    } catch (e: Exception) {
      false
    }
  }

  private fun hasXposedPackages(context: Context): Boolean {
    val pm = context.packageManager
    return XPOSED_PACKAGES.any { packageName ->
      try {
        pm.getPackageInfo(packageName, 0)
        true
      } catch (e: Exception) {
        false
      }
    }
  }

  private fun hasXposedBridgeClass(): Boolean {
    return try {
      Class.forName("de.robv.android.xposed.XposedBridge")
      true
    } catch (e: ClassNotFoundException) {
      false
    }
  }

  private fun hasFridaServerListening(): Boolean {
    return FRIDA_DEFAULT_PORTS.any { port ->
      try {
        Socket().use { socket ->
          socket.connect(InetSocketAddress("127.0.0.1", port), 200)
          true
        }
      } catch (e: Exception) {
        false
      }
    }
  }
}
