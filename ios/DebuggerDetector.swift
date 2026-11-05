import Foundation

/// Apple-documented technique (qa1361): a debugger-attached process has the
/// P_TRACED flag set in its `kinfo_proc` struct, readable via `sysctl`.
enum DebuggerDetector {
  static func isDebuggerAttached() -> Bool {
    var info = kinfo_proc()
    var size = MemoryLayout<kinfo_proc>.stride
    var mib: [Int32] = [CTL_KERN, KERN_PROC, KERN_PROC_PID, getpid()]

    let result = sysctl(&mib, u_int(mib.count), &info, &size, nil, 0)
    guard result == 0 else { return false }

    return (info.kp_proc.p_flag & P_TRACED) != 0
  }
}
