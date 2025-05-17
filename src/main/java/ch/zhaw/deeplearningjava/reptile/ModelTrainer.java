package ch.zhaw.deeplearningjava.reptile;

import java.io.IOException;
import java.nio.file.Paths;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.basicmodelzoo.cv.classification.ResNetV1;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.RandomFlipLeftRight;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Batch;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;

public class ModelTrainer {

    public static void main(String[] args) throws IOException, TranslateException {

        String datasetPath = "src/main/resources/dataset"; // 📁 Remplace par ton propre chemin si besoin

        // 🧪 Prétraitement des images
        Pipeline pipeline = new Pipeline();
        pipeline.add(new Resize(224, 224))
                .add(new RandomFlipLeftRight())
                .add(new ToTensor())
                .add(new Normalize(
                        new float[]{0.485f, 0.456f, 0.406f},
                        new float[]{0.229f, 0.224f, 0.225f}
                ));

        ImageFolder dataset = ImageFolder.builder()
                .setRepositoryPath(Paths.get(datasetPath))
                .optPipeline(pipeline)
                .setSampling(32, true)
                .build();

        dataset.prepare();

        int numClasses = dataset.getSynset().size();

        Block block = ResNetV1.builder()
                .setImageShape(new Shape(3, 224, 224))
                .setNumLayers(18)
                .setOutSize(numClasses)
                .build();

        try (Model model = Model.newInstance("reptile-classifier")) {
            model.setBlock(block);

            DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                    .optOptimizer(Adam.builder()
                            .optLearningRateTracker(Tracker.fixed(0.001f))
                            .build())
                    .addTrainingListeners(TrainingListener.Defaults.logging());

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.setMetrics(new Metrics());
                trainer.initialize(new Shape(1, 3, 224, 224));

                for (int epoch = 0; epoch < 5; epoch++) {
                    for (Batch batch : trainer.iterateDataset(dataset)) {
                        EasyTrain.trainBatch(trainer, batch); // ✅ Bon appel ici
                        trainer.step();
                        batch.close();
                    }
                    trainer.notifyListeners(listener -> listener.onEpoch(trainer));
                }

                model.setProperty("Synset", String.join(",", dataset.getSynset()));
                model.save(Paths.get("models"), "reptile-classifier");
                System.out.println("✅ Modèle entraîné et sauvegardé !");
            }
        }
    }
}
