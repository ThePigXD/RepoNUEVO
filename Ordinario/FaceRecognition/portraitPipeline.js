export const PORTRAIT_STAGES = Object.freeze({
  READY: 'ready',
  CAPTURING: 'capturing',
  VALIDATING: 'validating',
  ANALYZING: 'analyzing',
  ARCHIVING: 'archiving',
  COMPLETE: 'complete',
  FAILED: 'failed',
});

const pauseForRender = () => new Promise((resolve) => setTimeout(resolve, 80));

export async function executePortraitPipeline(photo, inspect, archive, publishStage) {
  if (!photo?.uri || !photo.base64) {
    throw new Error('La captura no contiene una imagen valida.');
  }

  publishStage(PORTRAIT_STAGES.VALIDATING);
  await pauseForRender();

  const observation = inspect(photo.base64);
  publishStage(PORTRAIT_STAGES.ANALYZING);
  await pauseForRender();

  publishStage(PORTRAIT_STAGES.ARCHIVING);
  await archive(photo.uri);

  publishStage(PORTRAIT_STAGES.COMPLETE);
  return observation;
}
