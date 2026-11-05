import Foundation

/// Flags an install that didn't go through the App Store or TestFlight: a
/// real App Store install always has a receipt at
/// `Bundle.main.appStoreReceiptURL`; a sideloaded (AltStore/jailbreak
/// install) or ad-hoc/enterprise build typically doesn't.
enum UnofficialStoreDetector {
  static func isUnofficialStore() -> Bool {
    #if targetEnvironment(simulator)
    return false
    #else
    guard let receiptURL = Bundle.main.appStoreReceiptURL else { return true }
    return !FileManager.default.fileExists(atPath: receiptURL.path)
    #endif
  }
}
