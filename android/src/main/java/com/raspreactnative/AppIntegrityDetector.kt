package com.raspreactnative

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import java.security.MessageDigest

/**
 * Flags a repackaged/resigned APK: the installed package name or signing
 * certificate no longer matches what `androidConfig` declares. An empty
 * `certificateHashes` list skips the signature half of the check rather
 * than failing closed on a missing config.
 */
object AppIntegrityDetector {

  fun isTampered(context: Context, config: AndroidRaspConfig): Boolean {
    val actualPackageName = context.packageName
    if (actualPackageName != config.packageName) {
      return true
    }

    if (config.certificateHashes.isEmpty()) {
      return false
    }

    val actualHashes = currentSigningCertificateHashes(context)
    return actualHashes.none { config.certificateHashes.contains(it) }
  }

  private fun currentSigningCertificateHashes(context: Context): List<String> {
    val signatures = try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        val packageInfo = context.packageManager.getPackageInfo(
          context.packageName,
          PackageManager.GET_SIGNING_CERTIFICATES
        )
        packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
      } else {
        @Suppress("DEPRECATION")
        val packageInfo = context.packageManager.getPackageInfo(
          context.packageName,
          PackageManager.GET_SIGNATURES
        )
        @Suppress("DEPRECATION")
        packageInfo.signatures ?: emptyArray()
      }
    } catch (e: Exception) {
      emptyArray<Signature>()
    }

    return signatures.map { sha256Base64(it.toByteArray()) }
  }

  private fun sha256Base64(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return Base64.encodeToString(digest, Base64.NO_WRAP)
  }
}
