# rasp-react-native

Runtime Application Self-Protection (RASP) for React Native. Native checks run on-device (Kotlin on Android, Swift on iOS) and report back to JS through a single `threatDetected` event.

## Status

Implemented so far:
- `privilegedAccess` — root (Android) / jailbreak (iOS) detection
- `debug` — attached debugger detection
- `appIntegrity` — repackaging/tamper detection: Android checks the installed package name + signing certificate hash against `androidConfig`; iOS checks the bundle ID + Apple Team ID (via the `application-identifier` entitlement) against `iosConfig`
- `hooks` — hooking framework detection (Frida, Xposed/LSPosed, Substrate/Substitute): scans loaded libraries/images, known package names or classes, known file paths, and probes Frida's default listening ports
- `unofficialStore` — sideload detection: Android checks the install source against Google Play + `androidConfig.supportedAlternativeStores`; iOS checks for an App Store/TestFlight receipt

Everything else in [the full threat list](#planned-threats) is reserved API surface (`ThreatType` already includes them) but not yet wired to a native check — see Roadmap.

## Install

```bash
yarn add rasp-react-native
cd ios && pod install
```

## Usage

```tsx
import { useRasp } from 'rasp-react-native';

function App() {
  useRasp(
    {
      androidConfig: {
        packageName: 'com.avibra.lifex',
        certificateHashes: ['<base64-sha256-of-release-cert>'],
      },
      iosConfig: {
        appBundleId: 'com.avibra.lifex',
        appTeamId: '<APPLE_TEAM_ID>',
      },
      isProd: true,
    },
    {
      privilegedAccess: () => console.warn('Root/Jailbreak detected'),
      debug: () => console.warn('Debugger attached'),
      started: () => console.log('RASP checks running'),
      initializationError: (message) => console.error('RASP init failed', message),
    }
  );

  return /* ... */;
}
```

Class-component equivalent:

```ts
import { startRasp, removeThreatListeners } from 'rasp-react-native';

startRasp(config, actions);
// ...
removeThreatListeners();
```

## Example app

`example/` is a bare React Native app (generated with the RN CLI) wired to this library's source via a `file:..` dependency, for manual testing on a device/emulator. It shows every detected threat on screen (see `example/App.tsx`).

```bash
# one-time setup
npm install            # build this library's lib/ output (postinstall not wired up — run npm run build too)
npm run build
cd example && npm install

# Android
npm run android

# iOS
cd ios && bundle install && bundle exec pod install && cd ..
npm run ios
```

Metro is configured (`example/metro.config.js`) to watch the parent directory, so edits to `src/` are picked up without re-running `npm run build` — only the native Kotlin/Swift changes need an app rebuild. Rebuild `lib/` (`npm run build` at the repo root) before testing a published/packed version.

Most checks won't fire anything on a stock, unrooted emulator/simulator with an ad-hoc debug build — that's expected. `unofficialStore` is likely to fire, since Metro/debug installs aren't installed through the Play Store or an App Store receipt.

## Planned threats

`simulator`, `deviceBinding`, `secureHardwareNotAvailable`, `systemVPN`, `passcode`, `obfuscationIssues`, `devMode`, `adbEnabled`, `screenshot`, `screenRecording`, `multiInstance`, `timeSpoofing`, `locationSpoofing`, `unsecureWifi`, `automation`, `bootloader`.

## Roadmap

1. ~~Root/Jailbreak + debugger detection~~
2. ~~App integrity / signature (tamper) detection~~
3. ~~Hooking framework detection (Frida, Xposed/LSPosed)~~
4. ~~Unofficial store / sideload detection~~
5. Remaining Android-only and iOS-only checks

## Notes

- Detection is heuristic by nature: each check raises the bar against casual bypass but no single signal (or combination) is airtight against a determined attacker with a hiding module (e.g. Magisk Hide / Shamiko). Treat this as defense-in-depth, not a hard guarantee.
- `startRasp` currently runs checks once, off the main/UI thread, shortly after the native module starts — it does not yet poll continuously.
