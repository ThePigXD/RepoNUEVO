import React, { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { supabase } from '../lib/supabase';

function cleanPriceInput(value) {
  const normalized = value.replace(',', '.').replace(/[^\d.]/g, '');
  const [whole, ...decimals] = normalized.split('.');

  return decimals.length ? `${whole}.${decimals.join('').slice(0, 2)}` : whole;
}

export default function ActualizarRegistros({ registro, onCancel, onUpdated }) {
  const [producto, setProducto] = useState(registro?.producto || '');
  const [descripcion, setDescripcion] = useState(registro?.descripcion || '');
  const [precio, setPrecio] = useState(String(registro?.precio ?? ''));
  const [loading, setLoading] = useState(false);

  const actualizarRegistro = async () => {
    if (!producto.trim() || !descripcion.trim() || !precio.trim()) {
      Alert.alert('Faltan datos', 'Completa todos los campos para actualizar el producto.');
      return;
    }

    const precioNumero = Number(precio);
    if (!Number.isFinite(precioNumero) || precioNumero < 0) {
      Alert.alert('Precio no válido', 'Escribe un precio igual o mayor a cero.');
      return;
    }

    setLoading(true);
    try {
      const { error } = await supabase
        .from('productos')
        .update({
          producto: producto.trim(),
          descripcion: descripcion.trim(),
          precio: precioNumero,
        })
        .eq('id', registro.id);

      if (error) throw error;

      onUpdated?.();
      onCancel?.();
    } catch (error) {
      Alert.alert('No se pudo actualizar', error.message || 'Intenta de nuevo más tarde.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.card}>
      <Text style={styles.kicker}>MODIFICAR CATÁLOGO</Text>
      <Text style={styles.title}>Editar producto</Text>

      <Text style={styles.label}>Nombre del producto</Text>
      <TextInput
        style={styles.input}
        value={producto}
        onChangeText={setProducto}
        placeholder="Producto"
        placeholderTextColor="#9aa49c"
        editable={!loading}
        maxLength={100}
      />
      <Text style={styles.label}>Descripción</Text>
      <TextInput
        style={[styles.input, styles.multilineInput]}
        value={descripcion}
        onChangeText={setDescripcion}
        placeholder="Descripción"
        placeholderTextColor="#9aa49c"
        editable={!loading}
        maxLength={500}
        multiline
        textAlignVertical="top"
      />
      <Text style={styles.label}>Precio (MXN)</Text>
      <TextInput
        style={styles.input}
        value={precio}
        onChangeText={(value) => setPrecio(cleanPriceInput(value))}
        keyboardType="decimal-pad"
        placeholder="0.00"
        placeholderTextColor="#9aa49c"
        editable={!loading}
        maxLength={12}
      />

      <View style={styles.actions}>
        <TouchableOpacity
          style={[styles.button, styles.saveButton, loading && styles.buttonDisabled]}
          onPress={actualizarRegistro}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator color="#ffffff" />
          ) : (
            <Text style={styles.saveText}>Guardar cambios</Text>
          )}
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.button, styles.cancelButton]}
          onPress={onCancel}
          disabled={loading}
        >
          <Text style={styles.cancelText}>Cancelar</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#ffffff',
    borderColor: '#dce6dc',
    borderWidth: 1,
    borderRadius: 18,
    padding: 18,
    marginBottom: 14,
  },
  kicker: {
    color: '#728077',
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1,
    marginBottom: 5,
  },
  title: {
    color: '#22372a',
    fontSize: 19,
    fontWeight: '800',
    marginBottom: 16,
  },
  label: {
    color: '#3d4c42',
    fontSize: 12,
    fontWeight: '700',
    marginBottom: 6,
  },
  input: {
    minHeight: 46,
    borderWidth: 1,
    borderColor: '#e1e7e1',
    borderRadius: 11,
    paddingHorizontal: 12,
    paddingVertical: 10,
    marginBottom: 13,
    backgroundColor: '#fbfcfa',
    color: '#22372a',
    fontSize: 14,
  },
  multilineInput: {
    minHeight: 72,
  },
  actions: {
    flexDirection: 'row',
    marginTop: 2,
  },
  button: {
    flex: 1,
    minHeight: 44,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  saveButton: {
    backgroundColor: '#466c50',
    marginRight: 8,
  },
  buttonDisabled: {
    opacity: 0.65,
  },
  cancelButton: {
    backgroundColor: '#edf0ed',
  },
  saveText: {
    color: '#ffffff',
    fontSize: 13,
    fontWeight: '800',
  },
  cancelText: {
    color: '#58655b',
    fontSize: 13,
    fontWeight: '700',
  },
});
