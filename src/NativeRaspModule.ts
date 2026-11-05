import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
import type { RaspConfig } from './types';

const LINKING_ERROR =
  `The package 'rasp-react-native' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

export interface RaspNativeModule {
  /** Starts the native checks and begins emitting `threatDetected` events. */
  startRasp(config: RaspConfig): void;
}

export const RaspModule: RaspNativeModule = NativeModules.RaspReactNative
  ? NativeModules.RaspReactNative
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const raspEventEmitter = new NativeEventEmitter(
  NativeModules.RaspReactNative
);
