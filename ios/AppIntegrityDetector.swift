import Foundation
import Security

/// Flags a repackaged/resigned IPA: the running app's bundle ID or Apple
/// Team ID no longer matches what `iosConfig` declares. The team ID is read
/// from the `application-identifier` entitlement (format "TEAMID.bundleId"),
/// a public API (`SecTaskCopyValueForEntitlement`) that needs no special
/// capability.
enum AppIntegrityDetector {
  static func isTampered(expectedBundleId: String, expectedTeamId: String) -> Bool {
    let actualBundleId = Bundle.main.bundleIdentifier
    if actualBundleId != expectedBundleId {
      return true
    }

    guard let actualTeamId = currentTeamId() else {
      // Entitlement unreadable (e.g. ad-hoc/dev edge cases) — don't fail
      // closed on a signal we can't verify.
      return false
    }

    return actualTeamId != expectedTeamId
  }

  private static func currentTeamId() -> String? {
    guard let task = SecTaskCreateFromSelf(nil) else { return nil }
    guard
      let appIdentifier = SecTaskCopyValueForEntitlement(
        task, "application-identifier" as CFString, nil
      ) as? String,
      let dotRange = appIdentifier.range(of: ".")
    else {
      return nil
    }

    return String(appIdentifier[..<dotRange.lowerBound])
  }
}
