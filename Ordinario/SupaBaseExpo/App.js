import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import AgregaRegistros from './components/AgregaRegistros';
import ConsultaRegistros from './components/ConsultaRegistros';

export default function App() {
  const [refreshKey, setRefreshKey] = useState(0);

  const handleRecordAdded = () => {
    setRefreshKey((value) => value + 1);
  };

  return (
    <View style={styles.screen}>
      <StatusBar style="dark" />
      <ScrollView
        contentContainerStyle={styles.container}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.header}>
          <View style={styles.eyebrow}>
            <View style={styles.eyebrowDot} />
            <Text style={styles.eyebrowText}>CONTROL DE ALMACÉN</Text>
          </View>
          <Text style={styles.title}>
            Inventario<Text style={styles.titleAccent}>.</Text>
          </Text>
          <Text style={styles.subtitle}>
            Administra los productos de tu inventario físico desde un solo lugar.
          </Text>
          <View style={styles.databaseBadge}>
            <Text style={styles.databaseBadgeText}>REGISTROS EN SUPABASE</Text>
          </View>
        </View>

        <AgregaRegistros onRecordAdded={handleRecordAdded} />
        <ConsultaRegistros refreshKey={refreshKey} />

        <Text style={styles.footer}>Inventario conectado a tu base de datos.</Text>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: '#f4f6f2',
  },
  container: {
    width: '100%',
    maxWidth: 720,
    alignSelf: 'center',
    paddingHorizontal: 20,
    paddingTop: 28,
    paddingBottom: 44,
  },
  header: {
    marginBottom: 24,
  },
  eyebrow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  eyebrowDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 8,
    backgroundColor: '#62866f',
  },
  eyebrowText: {
    color: '#62866f',
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 1.4,
  },
  title: {
    color: '#1d3025',
    fontSize: 36,
    fontWeight: '800',
    letterSpacing: -1,
  },
  titleAccent: {
    color: '#62866f',
  },
  subtitle: {
    maxWidth: 440,
    color: '#69756d',
    fontSize: 15,
    lineHeight: 22,
    marginTop: 7,
  },
  databaseBadge: {
    alignSelf: 'flex-start',
    backgroundColor: '#e5ece5',
    borderRadius: 20,
    paddingHorizontal: 11,
    paddingVertical: 7,
    marginTop: 16,
  },
  databaseBadgeText: {
    color: '#496752',
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 0.8,
  },
  footer: {
    color: '#879188',
    fontSize: 12,
    textAlign: 'center',
    marginTop: 10,
  },
});
