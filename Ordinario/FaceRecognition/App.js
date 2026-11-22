import React, { useRef, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { CameraView, useCameraPermissions } from 'expo-camera';
import * as MediaLibrary from 'expo-media-library/legacy';
import { inspectPortrait } from './faceDetector';
import { executePortraitPipeline, PORTRAIT_STAGES } from './portraitPipeline';

const palette = {
  ink: '#08111F',
  panel: '#101D2E',
  panelStrong: '#17273D',
  line: '#293B55',
  cream: '#F8F4E8',
  amber: '#FDB94E',
  coral: '#FF7A66',
  mint: '#73D6B0',
};

function TopBar({ cameraSide, onFlip }) {
  return (
    <View style={styles.topBar}>
      <View>
        <Text style={styles.eyebrow}>MONITOR VISUAL</Text>
        <Text style={styles.brand}>Rostro <Text style={styles.brandAccent}>Lab</Text></Text>
      </View>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel="Cambiar de camara"
        onPress={onFlip}
        style={({ pressed }) => [styles.flipButton, pressed && styles.pressed]}
      >
        <Text style={styles.flipIcon}>↻</Text>
        <Text style={styles.flipText}>{cameraSide === 'front' ? 'FRONTAL' : 'TRASERA'}</Text>
      </Pressable>
    </View>
  );
}

function PermissionView({ cameraStatus, galleryStatus, requestCamera, requestGallery }) {
  return (
    <View style={styles.permissionShell}>
      <StatusBar style="light" />
      <View style={styles.permissionMark}>
        <View style={styles.permissionMarkCore} />
      </View>
      <Text style={styles.permissionTitle}>Accesos necesarios</Text>
      <Text style={styles.permissionCopy}>
        El visor necesita la cámara para analizar tu toma y la galería para conservarla.
      </Text>
      {!cameraStatus?.granted && (
        <Pressable style={styles.primaryAction} onPress={requestCamera}>
          <Text style={styles.primaryActionText}>Activar cámara</Text>
        </Pressable>
      )}
      {!galleryStatus?.granted && (
        <Pressable style={styles.secondaryAction} onPress={requestGallery}>
          <Text style={styles.secondaryActionText}>Autorizar galería</Text>
        </Pressable>
      )}
    </View>
  );
}

export default function App() {
  const camera = useRef(null);
  const [cameraPermission, askCameraPermission] = useCameraPermissions();
  const [galleryPermission, askGalleryPermission] = MediaLibrary.usePermissions();
  const [cameraSide, setCameraSide] = useState('front');
  const [snapshot, setSnapshot] = useState(null);
  const [result, setResult] = useState(null);
  const [workflowStage, setWorkflowStage] = useState(PORTRAIT_STAGES.READY);

  if (!cameraPermission || !galleryPermission) {
    return (
      <View style={styles.loadingShell}>
        <StatusBar style="light" />
        <ActivityIndicator color={palette.amber} />
        <Text style={styles.loadingText}>Preparando sensores</Text>
      </View>
    );
  }

  if (!cameraPermission.granted || !galleryPermission.granted) {
    return (
      <PermissionView
        cameraStatus={cameraPermission}
        galleryStatus={galleryPermission}
        requestCamera={askCameraPermission}
        requestGallery={askGalleryPermission}
      />
    );
  }

  const capturePortrait = async () => {
    if (!camera.current || workflowStage !== PORTRAIT_STAGES.READY) return;

    setWorkflowStage(PORTRAIT_STAGES.CAPTURING);
    setResult({ kind: 'running', label: 'Creando fotografía…' });

    try {
      const photo = await camera.current.takePictureAsync({
        quality: 0.82,
        base64: true,
        skipProcessing: false,
      });
      setSnapshot(photo.uri);

      const observation = await executePortraitPipeline(
        photo,
        inspectPortrait,
        MediaLibrary.saveToLibraryAsync,
        (stage) => {
          setWorkflowStage(stage);
          const stageCopy = {
            [PORTRAIT_STAGES.VALIDATING]: ['running', 'Comprobando imagen…'],
            [PORTRAIT_STAGES.ANALYZING]: ['running', 'Analizando geometría facial…'],
            [PORTRAIT_STAGES.ARCHIVING]: ['running', 'Guardando en galería…'],
            [PORTRAIT_STAGES.COMPLETE]: ['success', 'Proceso terminado'],
          };

          if (stageCopy[stage]) {
            setResult({ kind: stageCopy[stage][0], label: stageCopy[stage][1] });
          }
        }
      );

      setResult(
        observation.detected
          ? {
              kind: 'success',
              label: `${observation.faceCount} rostro${observation.faceCount === 1 ? '' : 's'} encontrado${observation.faceCount === 1 ? '' : 's'}`,
              detail: `${observation.width} × ${observation.height} px · archivo archivado`,
            }
          : {
              kind: 'warning',
              label: 'Sin rostro confirmado',
              detail: 'La fotografía se guardó; ajusta el encuadre para repetir',
            }
      );

      Alert.alert('Archivo guardado', 'La fotografía quedó disponible en tu galería.');
    } catch (error) {
      console.error('Fallo al capturar o analizar:', error);
      setWorkflowStage(PORTRAIT_STAGES.FAILED);
      setResult({ kind: 'error', label: 'Toma no procesada', detail: 'Revisa permisos o vuelve a intentarlo' });
    }
  };

  const resetCapture = () => {
    setSnapshot(null);
    setResult(null);
    setWorkflowStage(PORTRAIT_STAGES.READY);
  };

  return (
    <View style={styles.appShell}>
      <StatusBar style="light" />
      {snapshot ? (
        <View style={styles.previewScreen}>
          <TopBar
            cameraSide={cameraSide}
            onFlip={() => setCameraSide((side) => (side === 'front' ? 'back' : 'front'))}
          />
          <View style={styles.previewFrame}>
            <Image source={{ uri: snapshot }} style={styles.previewPhoto} />
            <View style={styles.previewShade} />
            {result && (
              <View style={[styles.resultCard, styles[`result_${result.kind}`]]}>
                <View style={[styles.resultDot, styles[`dot_${result.kind}`]]} />
                <View style={styles.resultCopy}>
                  <Text style={styles.resultLabel}>{result.label}</Text>
                  {!!result.detail && <Text style={styles.resultDetail}>{result.detail}</Text>}
                </View>
              </View>
            )}
          </View>
          <Pressable
            accessibilityRole="button"
            onPress={resetCapture}
            style={({ pressed }) => [styles.newCaptureButton, pressed && styles.pressed]}
          >
            <Text style={styles.newCaptureText}>Nueva captura</Text>
          </Pressable>
        </View>
      ) : (
        <View style={styles.cameraScreen}>
          <CameraView
            ref={camera}
            style={StyleSheet.absoluteFillObject}
            facing={cameraSide}
            mode="picture"
          />
          <View style={styles.cameraWash} pointerEvents="none" />
          <TopBar
            cameraSide={cameraSide}
            onFlip={() => setCameraSide((side) => (side === 'front' ? 'back' : 'front'))}
          />

          <View style={styles.targetZone} pointerEvents="none">
            <View style={styles.cornerTopLeft} />
            <View style={styles.cornerTopRight} />
            <View style={styles.cornerBottomLeft} />
            <View style={styles.cornerBottomRight} />
            <Text style={styles.targetHint}>UBICA EL ROSTRO EN EL MARCO</Text>
          </View>

          <View style={styles.captureDock}>
            {result && (
              <Text style={[styles.liveStatus, result.kind === 'error' && styles.liveStatusError]}>
                {result.label}
              </Text>
            )}
            <Pressable
              accessibilityRole="button"
              accessibilityLabel="Capturar y analizar fotografia"
              disabled={workflowStage !== PORTRAIT_STAGES.READY}
              onPress={capturePortrait}
              style={({ pressed }) => [
                styles.shutter,
                pressed && styles.shutterPressed,
                workflowStage !== PORTRAIT_STAGES.READY && styles.shutterDisabled,
              ]}
            >
              {workflowStage !== PORTRAIT_STAGES.READY ? (
                <ActivityIndicator color={palette.ink} />
              ) : (
                <View style={styles.shutterCore} />
              )}
            </Pressable>
          </View>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  appShell: { flex: 1, backgroundColor: palette.ink },
  loadingShell: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: palette.ink },
  loadingText: { color: palette.cream, marginTop: 12, letterSpacing: 1.4, fontSize: 12 },

  permissionShell: {
    flex: 1,
    backgroundColor: palette.ink,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
  },
  permissionMark: {
    width: 116,
    height: 116,
    borderRadius: 58,
    borderWidth: 1,
    borderColor: palette.line,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 28,
  },
  permissionMarkCore: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: palette.amber,
  },
  permissionTitle: { color: palette.cream, fontSize: 28, fontWeight: '800', marginBottom: 10 },
  permissionCopy: { color: '#AAB6C7', lineHeight: 22, textAlign: 'center', marginBottom: 28 },
  primaryAction: {
    backgroundColor: palette.amber,
    width: '100%',
    maxWidth: 360,
    height: 54,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  primaryActionText: { color: palette.ink, fontWeight: '900', letterSpacing: 0.6 },
  secondaryAction: {
    width: '100%',
    maxWidth: 360,
    height: 54,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: palette.line,
    marginTop: 12,
  },
  secondaryActionText: { color: palette.cream, fontWeight: '700' },

  cameraScreen: { flex: 1, backgroundColor: palette.ink },
  cameraWash: { ...StyleSheet.absoluteFillObject, backgroundColor: 'rgba(3, 9, 17, 0.12)' },
  topBar: {
    height: 104,
    paddingHorizontal: 22,
    paddingTop: 42,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  eyebrow: { color: palette.amber, fontSize: 9, letterSpacing: 2.2, marginBottom: 2 },
  brand: { color: palette.cream, fontSize: 23, fontWeight: '800' },
  brandAccent: { color: palette.amber },
  flipButton: {
    height: 48,
    minWidth: 94,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: 'rgba(248, 244, 232, 0.26)',
    backgroundColor: 'rgba(8, 17, 31, 0.74)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  flipIcon: { color: palette.cream, fontSize: 17, lineHeight: 18 },
  flipText: { color: palette.cream, fontSize: 8, letterSpacing: 1.2, marginTop: 1 },

  targetZone: {
    width: 260,
    height: 330,
    borderRadius: 130,
    alignSelf: 'center',
    marginTop: 34,
  },
  cornerTopLeft: { position: 'absolute', left: 12, top: 28, width: 38, height: 38, borderLeftWidth: 2, borderTopWidth: 2, borderColor: palette.cream },
  cornerTopRight: { position: 'absolute', right: 12, top: 28, width: 38, height: 38, borderRightWidth: 2, borderTopWidth: 2, borderColor: palette.cream },
  cornerBottomLeft: { position: 'absolute', left: 12, bottom: 22, width: 38, height: 38, borderLeftWidth: 2, borderBottomWidth: 2, borderColor: palette.cream },
  cornerBottomRight: { position: 'absolute', right: 12, bottom: 22, width: 38, height: 38, borderRightWidth: 2, borderBottomWidth: 2, borderColor: palette.cream },
  targetHint: {
    position: 'absolute',
    bottom: -42,
    width: 260,
    textAlign: 'center',
    color: palette.cream,
    fontSize: 9,
    letterSpacing: 1.7,
  },
  captureDock: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 28,
    alignItems: 'center',
  },
  liveStatus: {
    color: palette.cream,
    backgroundColor: 'rgba(8, 17, 31, 0.82)',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 8,
    marginBottom: 10,
    fontSize: 12,
  },
  liveStatusError: { color: palette.coral },
  shutter: {
    width: 82,
    height: 82,
    borderRadius: 41,
    borderWidth: 2,
    borderColor: palette.cream,
    backgroundColor: 'rgba(8, 17, 31, 0.45)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  shutterPressed: { transform: [{ scale: 0.94 }] },
  shutterDisabled: { opacity: 0.7 },
  shutterCore: { width: 62, height: 62, borderRadius: 31, backgroundColor: palette.amber },

  previewScreen: { flex: 1, backgroundColor: palette.ink },
  previewFrame: { flex: 1, marginHorizontal: 18, marginBottom: 106, borderRadius: 28, overflow: 'hidden' },
  previewPhoto: { width: '100%', height: '100%', resizeMode: 'cover' },
  previewShade: { ...StyleSheet.absoluteFillObject, backgroundColor: 'rgba(8, 17, 31, 0.08)' },
  resultCard: {
    position: 'absolute',
    left: 16,
    right: 16,
    bottom: 16,
    minHeight: 72,
    borderRadius: 18,
    backgroundColor: 'rgba(8, 17, 31, 0.92)',
    borderWidth: 1,
    borderColor: palette.line,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
  },
  result_success: { borderColor: palette.mint },
  result_warning: { borderColor: palette.amber },
  result_error: { borderColor: palette.coral },
  result_running: { borderColor: palette.line },
  resultDot: { width: 12, height: 12, borderRadius: 6, marginRight: 12, backgroundColor: palette.line },
  dot_success: { backgroundColor: palette.mint },
  dot_warning: { backgroundColor: palette.amber },
  dot_error: { backgroundColor: palette.coral },
  dot_running: { backgroundColor: palette.cream },
  resultCopy: { flex: 1 },
  resultLabel: { color: palette.cream, fontSize: 16, fontWeight: '800' },
  resultDetail: { color: '#AAB6C7', fontSize: 12, marginTop: 3 },
  newCaptureButton: {
    position: 'absolute',
    left: 28,
    right: 28,
    bottom: 30,
    height: 58,
    borderRadius: 29,
    backgroundColor: palette.amber,
    alignItems: 'center',
    justifyContent: 'center',
  },
  newCaptureText: { color: palette.ink, fontSize: 15, fontWeight: '900', letterSpacing: 0.5 },
  pressed: { opacity: 0.82, transform: [{ scale: 0.985 }] },
});
