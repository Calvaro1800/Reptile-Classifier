/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * http://aws.amazon.com/apache2.0/
 */
package ch.zhaw.deeplearningjava.reptile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.basicmodelzoo.cv.classification.ResNetV1;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.TrainingResult;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;

/**
 * 🇬🇧 Trains a classification model using DJL and saves the resulting model for inference.
 * 🇫🇷 Entraîne un modèle de classification avec DJL et le sauvegarde pour l'inférence.
 * 🇩🇪 Trainiert ein Klassifikationsmodell mit DJL und speichert es für spätere Verwendung.
 */
public final class Training {

    private static final int BATCH_SIZE = 32;   // Taille du lot (Batch size)
    private static final int EPOCHS = 10;       // Nombre d'itérations complètes sur le dataset

    public static void main(String[] args) throws IOException, TranslateException {

        // 📁 Répertoire de sauvegarde du modèle entraîné
        Path modelDir = Paths.get("src/main/resources/models");

        // 📂 Chargement du dataset d'entraînement (ex: lizard, snake, salamander)
        ImageFolder dataset = initDataset("src/main/resources/dataset");
        RandomAccessDataset[] datasets = dataset.randomSplit(8, 2); // 80% train / 20% val

        // ⚙️ Configuration de l'entraînement avec fonction de perte et accuracy
        Loss loss = Loss.softmaxCrossEntropyLoss();
        TrainingConfig config = setupTrainingConfig(loss);

        // 🧠 Création dynamique du modèle avec nombre de classes correct
        Model model = Models.getModel();
        int numClasses = dataset.getSynset().size();  // nombre réel de classes

        Block resNet = ResNetV1.builder()
                .setImageShape(new Shape(3, Models.IMAGE_HEIGHT, Models.IMAGE_WIDTH))
                .setNumLayers(50)
                .setOutSize(numClasses)
                .build();

        model.setBlock(resNet);

        // 🎯 Initialisation du trainer
        Trainer trainer = model.newTrainer(config);
        trainer.setMetrics(new Metrics());
        Shape inputShape = new Shape(1, 3, Models.IMAGE_HEIGHT, Models.IMAGE_WIDTH);
        trainer.initialize(inputShape);

        // 🚀 Entraînement du modèle (10 epochs par défaut)
        EasyTrain.fit(trainer, EPOCHS, datasets[0], datasets[1]);

        // 🧾 Sauvegarde des propriétés du modèle
        TrainingResult result = trainer.getTrainingResult();
        model.setProperty("Epoch", String.valueOf(EPOCHS));
        model.setProperty("Accuracy", String.format("%.5f", result.getValidateEvaluation("Accuracy")));
        model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));

        // 💾 Sauvegarde du modèle et des classes
        model.save(modelDir, Models.MODEL_NAME);
        Models.saveSynset(modelDir, dataset.getSynset());

        System.out.println("✅ Modèle entraîné et sauvegardé avec succès.");
    }

    // 📥 Préparation du dataset d'images avec resize + normalisation
    private static ImageFolder initDataset(String datasetRoot) throws IOException, TranslateException {
        ImageFolder dataset = ImageFolder.builder()
                .setRepositoryPath(Paths.get(datasetRoot))
                .optMaxDepth(10)
                .addTransform(new Resize(Models.IMAGE_WIDTH, Models.IMAGE_HEIGHT))
                .addTransform(new ToTensor())
                .setSampling(BATCH_SIZE, true)
                .build();

        dataset.prepare();
        return dataset;
    }

    // ⚙️ Configuration DJL avec Loss + Accuracy + logs
    private static TrainingConfig setupTrainingConfig(Loss loss) {
        return new DefaultTrainingConfig(loss)
                .addEvaluator(new Accuracy())
                .addTrainingListeners(TrainingListener.Defaults.logging());

                 }
}
