import Foundation

@objc(RaspReactNative)
class RaspReactNative: RCTEventEmitter {
  private var hasListeners = false

  override static func requiresMainQueueSetup() -> Bool {
    false
  }

  override func supportedEvents() -> [String]! {
    ["threatDetected"]
  }

  override func startObserving() {
    hasListeners = true
  }

  override func stopObserving() {
    hasListeners = false
  }

  @objc(startRasp:)
  func startRasp(_ config: NSDictionary) {
    DispatchQueue.global(qos: .utility).async { [weak self] in
      guard let self = self else { return }

      if JailbreakDetector.isJailbroken() {
        self.emit("privilegedAccess")
      }
      if DebuggerDetector.isDebuggerAttached() {
        self.emit("debug")
      }
      if HookDetector.isHookingDetected() {
        self.emit("hooks")
      }
      if UnofficialStoreDetector.isUnofficialStore() {
        self.emit("unofficialStore")
      }

      if let iosConfig = config["iosConfig"] as? [String: Any],
        let bundleId = iosConfig["appBundleId"] as? String,
        let teamId = iosConfig["appTeamId"] as? String,
        AppIntegrityDetector.isTampered(expectedBundleId: bundleId, expectedTeamId: teamId)
      {
        self.emit("appIntegrity")
      }
    }
  }

  private func emit(_ threatType: String) {
    guard hasListeners else { return }
    sendEvent(withName: "threatDetected", body: threatType)
  }
}
