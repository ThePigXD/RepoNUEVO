import React, { useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { supabase } from '../lib/supabase';
import ActualizarRegistros from './ActualizarRegistros';
import EliminarRegistros from './EliminarRegistros';

const moneyFormatter = new Intl.NumberFormat('es-MX', {
  style: 'currency',
  currency: 'MXN',
});

export default function ConsultaRegistros({ refreshKey }) {
  const [registros, setRegistros] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [search, setSearch] = useState('');
  const [retryKey, setRetryKey] = useState(0);
  const [registroSeleccionado, setRegistroSeleccionado] = useState(null);
  const [registroEliminar, setRegistroEliminar] = useState(null);

  const solicitarActualizacion = () => {
    setErrorMessage('');
    setLoading(true);
    setRetryKey((value) => value + 1);
  };

  useEffect(() => {
    let active = true;

    const cargarRegistros = async () => {
      try {
        const { data, error } = await supabase
          .from('productos')
          .select('id, producto, descripcion, precio')
          .order('id', { ascending: true });

        if (error) throw error;
        if (active) {
          setRegistros(data || []);
          setErrorMessage('');
        }
      } catch (error) {
        if (active) {
          setErrorMessage(error.message || 'No se pudieron cargar los productos.');
        }
      } finally {
        if (active) setLoading(false);
      }
    };

    cargarRegistros();
    return () => {
      active = false;
    };
  }, [refreshKey, retryKey]);

  const registrosFiltrados = useMemo(() => {
    const term = search.trim().toLocaleLowerCase('es-MX');
    if (!term) return registros;

    return registros.filter((registro) =>
      `${registro.producto || ''} ${registro.descripcion || ''}`
        .toLocaleLowerCase('es-MX')
        .includes(term),
    );
  }, [registros, search]);

  const manejarActualizacion = () => {
    setRegistroSeleccionado(null);
    solicitarActualizacion();
  };

  const manejarEliminacion = () => {
    setRegistroEliminar(null);
    solicitarActualizacion();
  };

  return (
    <View style={styles.container}>
      <View style={styles.sectionHeading}>
        <View>
          <Text style={styles.kicker}>EXISTENCIAS</Text>
          <Text style={styles.title}>Productos</Text>
        </View>
        <View style={styles.countBadge}>
          <Text style={styles.countText}>{registros.length}</Text>
        </View>
      </View>

      <View style={styles.toolbar}>
        <TextInput
          style={styles.searchInput}
          placeholder="Buscar por nombre o descripción"
          placeholderTextColor="#929c94"
          value={search}
          onChangeText={setSearch}
          accessibilityLabel="Buscar productos"
          returnKeyType="search"
        />
        <TouchableOpacity
          style={styles.refreshButton}
          onPress={solicitarActualizacion}
          disabled={loading}
          accessibilityRole="button"
          accessibilityLabel="Actualizar productos"
        >
          <Text style={styles.refreshText}>↻</Text>
        </TouchableOpacity>
      </View>

      {registroSeleccionado ? (
        <ActualizarRegistros
          key={registroSeleccionado.id}
          registro={registroSeleccionado}
          onCancel={() => setRegistroSeleccionado(null)}
          onUpdated={manejarActualizacion}
        />
      ) : null}

      {registroEliminar ? (
        <EliminarRegistros
          registro={registroEliminar}
          onCancel={() => setRegistroEliminar(null)}
          onDeleted={manejarEliminacion}
        />
      ) : null}

      {loading ? (
        <ActivityIndicator size="large" color="#466c50" style={styles.loader} />
      ) : errorMessage ? (
        <View style={styles.messageCard}>
          <Text style={styles.messageTitle}>No fue posible cargar el inventario</Text>
          <Text style={styles.messageText}>{errorMessage}</Text>
          <TouchableOpacity
            style={styles.retryButton}
            onPress={solicitarActualizacion}
          >
            <Text style={styles.retryText}>Intentar de nuevo</Text>
          </TouchableOpacity>
        </View>
      ) : registrosFiltrados.length === 0 ? (
        <View style={styles.messageCard}>
          <Text style={styles.emptyIcon}>{search.trim() ? '⌕' : '＋'}</Text>
          <Text style={styles.messageTitle}>
            {search.trim() ? 'Sin resultados' : 'Aún no hay productos'}
          </Text>
          <Text style={styles.messageText}>
            {search.trim()
              ? 'Prueba con otro nombre o descripción.'
              : 'Agrega el primer producto para iniciar el inventario.'}
          </Text>
        </View>
      ) : (
        <>
          <Text style={styles.resultText}>
            {search.trim()
              ? `${registrosFiltrados.length} de ${registros.length} productos`
              : 'Catálogo de productos'}
          </Text>
          {registrosFiltrados.map((registro) => (
            <View key={registro.id} style={styles.productCard}>
              <View style={styles.productHeader}>
                <View style={styles.productMark}>
                  <Text style={styles.productMarkText}>
                    {(registro.producto || '?').trim().charAt(0).toUpperCase()}
                  </Text>
                </View>
                <View style={styles.productHeading}>
                  <Text style={styles.itemTitle}>{registro.producto}</Text>
                  <Text style={styles.itemPrice}>
                    {moneyFormatter.format(Number(registro.precio || 0))}
                  </Text>
                </View>
              </View>
              <Text style={styles.itemDescription}>{registro.descripcion}</Text>

              <View style={styles.actions}>
                <TouchableOpacity
                  style={[styles.actionButton, styles.editButton]}
                  onPress={() => {
                    setRegistroEliminar(null);
                    setRegistroSeleccionado(registro);
                  }}
                  activeOpacity={0.8}
                >
                  <Text style={styles.editButtonText}>Editar</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.actionButton, styles.deleteButton]}
                  onPress={() => {
                    setRegistroSeleccionado(null);
                    setRegistroEliminar(registro);
                  }}
                  activeOpacity={0.8}
                >
                  <Text style={styles.deleteButtonText}>Eliminar</Text>
                </TouchableOpacity>
              </View>
            </View>
          ))}
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 12,
  },
  sectionHeading: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 14,
  },
  kicker: {
    color: '#728077',
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.1,
    marginBottom: 4,
  },
  title: {
    color: '#22372a',
    fontSize: 23,
    fontWeight: '800',
  },
  countBadge: {
    minWidth: 38,
    height: 38,
    paddingHorizontal: 10,
    borderRadius: 13,
    backgroundColor: '#e5ece5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  countText: {
    color: '#496752',
    fontSize: 14,
    fontWeight: '800',
  },
  toolbar: {
    flexDirection: 'row',
    marginBottom: 14,
  },
  searchInput: {
    flex: 1,
    minHeight: 48,
    borderWidth: 1,
    borderColor: '#e1e7e1',
    borderRadius: 13,
    backgroundColor: '#ffffff',
    color: '#22372a',
    fontSize: 14,
    paddingHorizontal: 14,
  },
  refreshButton: {
    width: 48,
    height: 48,
    marginLeft: 9,
    borderRadius: 13,
    backgroundColor: '#e5ece5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  refreshText: {
    color: '#496752',
    fontSize: 25,
    lineHeight: 29,
  },
  loader: {
    marginVertical: 32,
  },
  messageCard: {
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderColor: '#e8ece7',
    borderWidth: 1,
    borderRadius: 18,
    paddingHorizontal: 20,
    paddingVertical: 26,
    marginTop: 2,
  },
  emptyIcon: {
    color: '#62866f',
    fontSize: 29,
    marginBottom: 5,
  },
  messageTitle: {
    color: '#2c3d31',
    fontSize: 16,
    fontWeight: '800',
    textAlign: 'center',
  },
  messageText: {
    color: '#758078',
    fontSize: 13,
    lineHeight: 19,
    textAlign: 'center',
    marginTop: 6,
  },
  retryButton: {
    backgroundColor: '#466c50',
    borderRadius: 10,
    paddingHorizontal: 15,
    paddingVertical: 10,
    marginTop: 14,
  },
  retryText: {
    color: '#ffffff',
    fontSize: 13,
    fontWeight: '700',
  },
  resultText: {
    color: '#879188',
    fontSize: 12,
    fontWeight: '600',
    marginBottom: 10,
  },
  productCard: {
    backgroundColor: '#ffffff',
    borderColor: '#e8ece7',
    borderWidth: 1,
    borderRadius: 18,
    padding: 16,
    marginBottom: 11,
    shadowColor: '#23382a',
    shadowOpacity: 0.035,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 4 },
    elevation: 1,
  },
  productHeader: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  productMark: {
    width: 43,
    height: 43,
    borderRadius: 14,
    backgroundColor: '#edf2ed',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  productMarkText: {
    color: '#55745d',
    fontSize: 17,
    fontWeight: '800',
  },
  productHeading: {
    flex: 1,
  },
  itemTitle: {
    color: '#26392c',
    fontSize: 16,
    fontWeight: '800',
  },
  itemPrice: {
    color: '#53775b',
    fontSize: 14,
    fontWeight: '800',
    marginTop: 3,
  },
  itemDescription: {
    color: '#758078',
    fontSize: 13,
    lineHeight: 19,
    marginTop: 13,
  },
  actions: {
    flexDirection: 'row',
    borderTopWidth: 1,
    borderTopColor: '#eef1ed',
    marginTop: 14,
    paddingTop: 12,
  },
  actionButton: {
    flex: 1,
    minHeight: 40,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  editButton: {
    backgroundColor: '#edf2ed',
    marginRight: 8,
  },
  editButtonText: {
    color: '#496752',
    fontSize: 13,
    fontWeight: '800',
  },
  deleteButton: {
    backgroundColor: '#fbefec',
  },
  deleteButtonText: {
    color: '#a65548',
    fontSize: 13,
    fontWeight: '800',
  },
});
