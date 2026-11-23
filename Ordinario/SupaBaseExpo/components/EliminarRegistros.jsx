import React, { useState } from 'react';
import { ActivityIndicator, Alert, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { supabase } from '../lib/supabase';

export default function EliminarRegistros({ registro, onCancel, onDeleted }) {
  const [loading, setLoading] = useState(false);

  const eliminarRegistro = async () => {
    setLoading(true);
    try {
      const { error } = await supabase.from('productos').delete().eq('id', registro.id);

      if (error) throw error;

      onDeleted?.();
      onCancel?.();
    } catch (error) {
      Alert.alert('No se pudo eliminar', error.message || 'Intenta de nuevo más tarde.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.card}>
      <View style={styles.warningBadge}>
        <Text style={styles.warningMark}>!</Text>
      </View>
      <Text style={styles.title}>Eliminar producto</Text>
      <Text style={styles.description}>
        ¿Quieres quitar <Text style={styles.productName}>{registro.producto}</Text> del inventario?
        Esta acción no se puede deshacer.
      </Text>

      <View style={styles.actions}>
        <TouchableOpacity
          style={[styles.button, styles.deleteButton, loading && styles.buttonDisabled]}
          onPress={eliminarRegistro}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator color="#ffffff" />
          ) : (
            <Text style={styles.deleteText}>Sí, eliminar</Text>
          )}
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.button, styles.cancelButton]}
          onPress={onCancel}
          disabled={loading}
        >
          <Text style={styles.cancelText}>Conservar</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fffaf8',
    borderColor: '#f0d8d2',
    borderWidth: 1,
    borderRadius: 18,
    padding: 18,
    marginBottom: 14,
  },
  warningBadge: {
    width: 34,
    height: 34,
    borderRadius: 12,
    backgroundColor: '#f7e5df',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 10,
  },
  warningMark: {
    color: '#a65548',
    fontSize: 17,
    fontWeight: '800',
  },
  title: {
    color: '#50332e',
    fontSize: 17,
    fontWeight: '800',
  },
  description: {
    color: '#80645e',
    fontSize: 13,
    lineHeight: 19,
    marginTop: 7,
  },
  productName: {
    color: '#50332e',
    fontWeight: '800',
  },
  actions: {
    flexDirection: 'row',
    marginTop: 16,
  },
  button: {
    flex: 1,
    minHeight: 44,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  deleteButton: {
    backgroundColor: '#a65548',
    marginRight: 8,
  },
  buttonDisabled: {
    opacity: 0.65,
  },
  cancelButton: {
    backgroundColor: '#f0e9e6',
  },
  deleteText: {
    color: '#ffffff',
    fontSize: 13,
    fontWeight: '800',
  },
  cancelText: {
    color: '#72564f',
    fontSize: 13,
    fontWeight: '700',
  },
});
