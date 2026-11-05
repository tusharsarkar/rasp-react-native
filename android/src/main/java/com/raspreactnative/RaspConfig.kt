package com.raspreactnative

import com.facebook.react.bridge.ReadableMap

data class AndroidRaspConfig(
  val packageName: String,
  val certificateHashes: List<String>,
  val supportedAlternativeStores: List<String>
)

object RaspConfigParser {
  fun parseAndroidConfig(config: ReadableMap): AndroidRaspConfig? {
    val androidConfig = config.getMap("androidConfig") ?: return null
    val packageName = androidConfig.getString("packageName") ?: return null

    val certificateHashes = stringList(androidConfig.getArray("certificateHashes"))
    val supportedAlternativeStores = stringList(androidConfig.getArray("supportedAlternativeStores"))

    return AndroidRaspConfig(packageName, certificateHashes, supportedAlternativeStores)
  }

  private fun stringList(array: com.facebook.react.bridge.ReadableArray?): List<String> {
    if (array == null) return emptyList()
    val result = mutableListOf<String>()
    for (i in 0 until array.size()) {
      array.getString(i)?.let { result.add(it) }
    }
    return result
  }
}
