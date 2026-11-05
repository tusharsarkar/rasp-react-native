import { useEffect, useRef } from 'react';
import { startRasp, removeThreatListeners } from './index';
import type { RaspConfig, ThreatCallbacks } from './types';

/**
 * Hook form of `startRasp`/`removeThreatListeners`. Registers on mount and
 * tears down on unmount. `actions` is read through a ref so callers can pass
 * an inline object without re-registering the native listener every render.
 */
export function useRasp(config: RaspConfig, actions: ThreatCallbacks): void {
  const actionsRef = useRef(actions);
  actionsRef.current = actions;

  useEffect(() => {
    startRasp(config, new Proxy({} as ThreatCallbacks, {
      get(_target, prop: string) {
        return actionsRef.current[prop as keyof ThreatCallbacks];
      },
    }));

    return () => removeThreatListeners();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(config)]);
}
