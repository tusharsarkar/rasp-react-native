import { RaspModule, raspEventEmitter } from './NativeRaspModule';
import type { RaspConfig, ThreatCallbacks, ThreatType } from './types';

export type {
  RaspConfig,
  AndroidConfig,
  IosConfig,
  ThreatCallbacks,
  ThreatType,
} from './types';

let subscription: { remove: () => void } | null = null;

/**
 * Starts RASP threat monitoring and wires native `threatDetected` events to
 * the callback matching their `ThreatType`. Safe to call once per app
 * lifetime (e.g. at the root component's mount); calling it again tears
 * down the previous listener first.
 */
export function startRasp(config: RaspConfig, actions: ThreatCallbacks): void {
  subscription?.remove();

  subscription = raspEventEmitter.addListener(
    'threatDetected',
    (threat: ThreatType) => {
      actions[threat]?.();
    }
  );

  try {
    RaspModule.startRasp(config);
    actions.started?.();
  } catch (error) {
    actions.initializationError?.(
      error instanceof Error ? error.message : String(error)
    );
  }
}

/** Removes the native event listener registered by `startRasp`. */
export function removeThreatListeners(): void {
  subscription?.remove();
  subscription = null;
}

export { useRasp } from './useRasp';
