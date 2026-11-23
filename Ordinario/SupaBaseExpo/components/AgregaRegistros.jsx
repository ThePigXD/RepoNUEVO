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

export default function AgregaRegistros({ onRecordAdded }) {
  const [producto, setProducto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');
  const [loading, setLoading] = useState(false);

  const manejaGuardado = async () => {
    if (!producto.trim() || !descripcion.trim() || !precio.trim()) {
      Alert.alert('Faltan datos', 'Completa todos los campos para guardar el producto.');
      return;
    }

    const precioNumero = Number(precio);
    if (!Number.isFinite(precioNumero) || precioNumero < 0) {
      Alert.alert('Precio no válido', 'Escribe un precio igual o mayor a cero.');
      return;
    }

    setLoading(true);
    try {
      const { error } = await supabase.from('productos').insert({
        producto: producto.trim(),
        descripcion: descripcion.trim(),
        precio: precioNumero,
      });

      if (error) throw error;

      setProducto('');
      setDescripcion('');
      setPrecio('');
      onRecordAdded?.();
    } catch (error) {
      Alert.alert('No se pudo guardar', error.message || 'Intenta de nuevo más tarde.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.card}>
      <View style={styles.heading}>
        <View>
          <Text style={styles.kicker}>AGREGAR AL CATÁLOGO</Text>
          <Text style={styles.title}>Nuevo producto</Text>
        </View>
        <View style={styles.iconBadge}>
          <Text style={styles.iconText}>+</Text>
        </View>
      </View>

      <Text style={styles.label}>Nombre del producto</Text>
      <TextInput
        style={styles.input}
        placeholder="Ej. Silla de madera"
        placeholderTextColor="#9aa49c"
        value={producto}
        onChangeText={setProducto}
        editable={!loading}
        maxLength={100}
        returnKeyType="next"
      />

      <Text style={styles.label}>Descripción</Text>
      <TextInput
        style={[styles.input, styles.multilineInput]}
        placeholder="Detalles para identificarlo"
        placeholderTextColor="#9aa49c"
        value={descripcion}
        onChangeText={setDescripcion}
        editable={!loading}
        maxLength={500}
        multiline
        textAlignVertical="top"
      />

      <Text style={styles.label}>Precio</Text>
      <View style={styles.priceField}>
        <Text style={styles.currency}>$</Text>
        <TextInput
          style={styles.priceInput}
          placeholder="0.00"
          placeholderTextColor="#9aa49c"
          value={precio}
          keyboardType="decimal-pad"
          onChangeText={(value) => setPrecio(cleanPriceInput(value))}
          editable={!loading}
          maxLength={12}
        />
        <Text style={styles.currencyCode}>MXN</Text>
      </View>

      <TouchableOpacity
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={manejaGuardado}
        disabled={loading}
        activeOpacity={0.85}
      >
        {loading ? (
          <ActivityIndicator color="#ffffff" />
        ) : (
          <Text style={styles.buttonText}>Guardar producto</Text>
        )}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#ffffff',
    borderColor: '#e8ece7',
    borderWidth: 1,
    borderRadius: 20,
    padding: 20,
    marginBottom: 28,
    shadowColor: '#23382a',
    shadowOpacity: 0.05,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 7 },
    elevation: 2,
  },
  heading: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 20,
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
    fontSize: 21,
    fontWeight: '800',
  },
  iconBadge: {
    width: 42,
    height: 42,
    borderRadius: 14,
    backgroundColor: '#eaf0ea',
    alignItems: 'center',
    justifyContent: 'center',
  },
  iconText: {
    color: '#4f7359',
    fontSize: 26,
    fontWeight: '400',
    lineHeight: 30,
  },
  label: {
    color: '#3d4c42',
    fontSize: 13,
    fontWeight: '700',
    marginBottom: 7,
  },
  input: {
    minHeight: 48,
    borderWidth: 1,
    borderColor: '#e1e7e1',
    borderRadius: 12,
    paddingHorizontal: 13,
    paddingVertical: 11,
    marginBottom: 15,
    backgroundColor: '#fbfcfa',
    color: '#22372a',
    fontSize: 15,
  },
  multilineInput: {
    minHeight: 78,
  },
  priceField: {
    minHeight: 48,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#e1e7e1',
    borderRadius: 12,
    paddingHorizontal: 13,
    marginBottom: 18,
    backgroundColor: '#fbfcfa',
  },
  currency: {
    color: '#728077',
    fontSize: 16,
    fontWeight: '700',
  },
  priceInput: {
    flex: 1,
    color: '#22372a',
    fontSize: 15,
    paddingVertical: 10,
    paddingHorizontal: 9,
  },
  currencyCode: {
    color: '#879188',
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  button: {
    minHeight: 50,
    backgroundColor: '#466c50',
    borderRadius: 13,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonDisabled: {
    opacity: 0.65,
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 15,
    fontWeight: '800',
  },
});
