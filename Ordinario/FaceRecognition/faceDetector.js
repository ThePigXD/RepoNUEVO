import jpeg from 'jpeg-js';
import { toByteArray } from 'base64-js';
import 'tracking/build/tracking.js';
import 'tracking/build/data/face-min.js';

const trackerEngine = globalThis.tracking;
const ANALYSIS_LIMIT = 480;

function preparePixels(decodedImage) {
  const largestSide = Math.max(decodedImage.width, decodedImage.height);
  const ratio = largestSide > ANALYSIS_LIMIT ? ANALYSIS_LIMIT / largestSide : 1;
  const outputWidth = Math.max(1, Math.round(decodedImage.width * ratio));
  const outputHeight = Math.max(1, Math.round(decodedImage.height * ratio));

  if (ratio === 1) {
    return { pixels: decodedImage.data, width: outputWidth, height: outputHeight };
  }

  const sampled = new Uint8Array(outputWidth * outputHeight * 4);

  for (let row = 0; row < outputHeight; row += 1) {
    const sourceRow = Math.min(decodedImage.height - 1, Math.floor(row / ratio));

    for (let column = 0; column < outputWidth; column += 1) {
      const sourceColumn = Math.min(decodedImage.width - 1, Math.floor(column / ratio));
      const sourceOffset = (sourceRow * decodedImage.width + sourceColumn) * 4;
      const targetOffset = (row * outputWidth + column) * 4;

      sampled[targetOffset] = decodedImage.data[sourceOffset];
      sampled[targetOffset + 1] = decodedImage.data[sourceOffset + 1];
      sampled[targetOffset + 2] = decodedImage.data[sourceOffset + 2];
      sampled[targetOffset + 3] = decodedImage.data[sourceOffset + 3];
    }
  }

  return { pixels: sampled, width: outputWidth, height: outputHeight };
}

export function inspectPortrait(encodedImage) {
  if (!encodedImage) {
    return { detected: false, faceCount: 0, width: 0, height: 0 };
  }

  const decoded = jpeg.decode(toByteArray(encodedImage), { useTArray: true });
  const prepared = preparePixels(decoded);
  const detections = trackerEngine.ViolaJones.detect(
    prepared.pixels,
    prepared.width,
    prepared.height,
    1.1,
    1.22,
    2,
    0,
    trackerEngine.ViolaJones.classifiers.face
  );

  return {
    detected: detections.length > 0,
    faceCount: detections.length,
    width: decoded.width,
    height: decoded.height,
  };
}