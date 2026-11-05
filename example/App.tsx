/**
 * rasp-react-native example app.
 *
 * Runs the native checks on mount via `useRasp` and lists whichever threats
 * fire, so you can confirm the bridge is wired up on a real device/emulator
 * (most checks won't trigger anything interesting on a stock, unrooted
 * emulator — that's expected).
 *
 * @format
 */

import React, {useCallback, useState} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';
import {Colors} from 'react-native/Libraries/NewAppScreen';
import {useRasp} from 'rasp-react-native';
import type {ThreatType} from 'rasp-react-native';

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };
  const textColor = {color: isDarkMode ? Colors.white : Colors.black};

  const [threats, setThreats] = useState<ThreatType[]>([]);
  const [status, setStatus] = useState('Starting checks…');

  const onThreat = useCallback((threat: ThreatType) => {
    setThreats(previous =>
      previous.includes(threat) ? previous : [...previous, threat],
    );
  }, []);

  useRasp(
    {
      androidConfig: {
        // Replace with the real app's package name + release signing cert
        // hash(es) before using this against anything but this example app.
        packageName: 'com.raspexample',
        certificateHashes: [],
      },
      iosConfig: {
        appBundleId: 'org.reactjs.native.example.RaspExample',
        appTeamId: 'REPLACE_WITH_TEAM_ID',
      },
      isProd: false,
    },
    {
      privilegedAccess: () => onThreat('privilegedAccess'),
      debug: () => onThreat('debug'),
      appIntegrity: () => onThreat('appIntegrity'),
      hooks: () => onThreat('hooks'),
      unofficialStore: () => onThreat('unofficialStore'),
      started: () => setStatus('Checks running…'),
      initializationError: message => setStatus(`Init failed: ${message}`),
    },
  );

  return (
    <SafeAreaView style={[styles.flex, backgroundStyle]}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView contentInsetAdjustmentBehavior="automatic">
        <View style={styles.section}>
          <Text style={[styles.title, textColor]}>rasp-react-native</Text>
          <Text style={[styles.status, textColor]}>{status}</Text>

          <Text style={[styles.subtitle, textColor]}>Detected threats</Text>
          {threats.length === 0 ? (
            <Text style={textColor}>None so far.</Text>
          ) : (
            threats.map(threat => (
              <Text key={threat} style={[styles.threat, textColor]}>
                • {threat}
              </Text>
            ))
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  flex: {flex: 1},
  section: {marginTop: 32, paddingHorizontal: 24},
  title: {fontSize: 24, fontWeight: '700'},
  status: {marginTop: 8, fontSize: 14, fontStyle: 'italic'},
  subtitle: {marginTop: 24, fontSize: 18, fontWeight: '600'},
  threat: {marginTop: 4, fontSize: 16},
});

export default App;
