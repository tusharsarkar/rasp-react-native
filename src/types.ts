/** Threats the native layer can report. Only `privilegedAccess` and `debug` are implemented so far; the rest are reserved for upcoming checks. */
export type ThreatType =
  | 'privilegedAccess' // root (Android) / jailbreak (iOS)
  | 'debug' // debugger attached
  | 'simulator'
  | 'appIntegrity'
  | 'unofficialStore'
  | 'hooks'
  | 'deviceBinding'
  | 'secureHardwareNotAvailable'
  | 'systemVPN'
  | 'passcode'
  | 'obfuscationIssues'
  | 'devMode'
  | 'adbEnabled'
  | 'screenshot'
  | 'screenRecording'
  | 'multiInstance'
  | 'timeSpoofing'
  | 'locationSpoofing'
  | 'unsecureWifi'
  | 'automation'
  | 'bootloader';

export interface AndroidConfig {
  packageName: string;
  certificateHashes: string[];
  supportedAlternativeStores?: string[];
}

export interface IosConfig {
  appBundleId: string;
  appTeamId: string;
}

export interface RaspConfig {
  androidConfig: AndroidConfig;
  iosConfig: IosConfig;
  /** When true, checks run in production mode (e.g. signature checks are enforced). */
  isProd: boolean;
}

export type ThreatCallback = () => void;

/** One callback per `ThreatType`; only threats you provide a handler for are reported. */
export type ThreatCallbacks = Partial<Record<ThreatType, ThreatCallback>> & {
  /** Fires once the native module has registered and initial checks start. */
  started?: () => void;
  /** Fires if native initialization itself fails (bad config, etc). */
  initializationError?: (message: string) => void;
};
