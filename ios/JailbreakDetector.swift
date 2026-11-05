import Foundation
import UIKit

/// Heuristic jailbreak detection — like its Android counterpart, no single
/// check is conclusive against a determined bypass tool; these are combined
/// to raise the bar rather than guarantee detection.
enum JailbreakDetector {

  private static let suspiciousPaths = [
    "/Applications/Cydia.app",
    "/Applications/Sileo.app",
    "/Applications/Zebra.app",
    "/Library/MobileSubstrate/MobileSubstrate.dylib",
    "/bin/bash",
    "/usr/sbin/sshd",
    "/etc/apt",
    "/private/var/lib/apt",
    "/private/var/lib/cydia",
    "/private/var/cache/apt",
  ]

  static func isJailbroken() -> Bool {
    #if targetEnvironment(simulator)
    return false
    #else
    return hasSuspiciousFiles() || canWriteOutsideSandbox() || canOpenCydiaUrl()
    #endif
  }

  private static func hasSuspiciousFiles() -> Bool {
    suspiciousPaths.contains { FileManager.default.fileExists(atPath: $0) }
  }

  private static func canWriteOutsideSandbox() -> Bool {
    let testPath = "/private/\(UUID().uuidString)"
    do {
      try "jailbreak_test".write(toFile: testPath, atomically: true, encoding: .utf8)
      try FileManager.default.removeItem(atPath: testPath)
      return true
    } catch {
      return false
    }
  }

  private static func canOpenCydiaUrl() -> Bool {
    guard let url = URL(string: "cydia://package/com.example.package") else { return false }
    var canOpen = false
    if Thread.isMainThread {
      canOpen = UIApplication.shared.canOpenURL(url)
    } else {
      DispatchQueue.main.sync {
        canOpen = UIApplication.shared.canOpenURL(url)
      }
    }
    return canOpen
  }
}
