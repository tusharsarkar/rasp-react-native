import Foundation

/// Heuristic hooking-framework detection (Frida, Substrate/Substitute,
/// libcycript). Same caveat as the Android equivalent: a custom-built tool
/// that renames its library or skips the default port can dodge any single
/// check here.
enum HookDetector {

  private static let suspiciousImageKeywords = [
    "frida",
    "cynject",
    "libcycript",
    "substrate",
    "substitued",
    "sslkillswitch",
  ]

  private static let suspiciousPaths = [
    "/usr/lib/frida",
    "/usr/lib/libfrida-gadget.dylib",
    "/Library/MobileSubstrate/MobileSubstrate.dylib",
    "/Library/MobileSubstrate/DynamicLibraries",
  ]

  private static let fridaDefaultPorts: [UInt16] = [27042, 27043]

  static func isHookingDetected() -> Bool {
    hasSuspiciousLoadedImages() || hasSuspiciousFiles() || hasFridaServerListening()
  }

  private static func hasSuspiciousLoadedImages() -> Bool {
    let imageCount = _dyld_image_count()
    for i in 0..<imageCount {
      guard let namePtr = _dyld_get_image_name(i) else { continue }
      let name = String(cString: namePtr).lowercased()
      if suspiciousImageKeywords.contains(where: { name.contains($0) }) {
        return true
      }
    }
    return false
  }

  private static func hasSuspiciousFiles() -> Bool {
    suspiciousPaths.contains { FileManager.default.fileExists(atPath: $0) }
  }

  private static func hasFridaServerListening() -> Bool {
    fridaDefaultPorts.contains { canConnect(port: $0) }
  }

  private static func canConnect(port: UInt16, timeoutMs: Int32 = 200) -> Bool {
    let sock = socket(AF_INET, SOCK_STREAM, 0)
    guard sock >= 0 else { return false }
    defer { close(sock) }

    var timeout = timeval(tv_sec: 0, tv_usec: Int32(timeoutMs * 1000))
    setsockopt(sock, SOL_SOCKET, SO_SNDTIMEO, &timeout, socklen_t(MemoryLayout<timeval>.size))

    var addr = sockaddr_in()
    addr.sin_family = sa_family_t(AF_INET)
    addr.sin_port = port.bigEndian
    addr.sin_addr.s_addr = inet_addr("127.0.0.1")

    let result = withUnsafePointer(to: &addr) {
      $0.withMemoryRebound(to: sockaddr.self, capacity: 1) {
        connect(sock, $0, socklen_t(MemoryLayout<sockaddr_in>.size))
      }
    }

    return result == 0
  }
}
