/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * http://aws.amazon.com/apache2.0/
 */
package ch.zhaw.deeplearningjava.reptile;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ai.djl.Model;

/** A helper class to load and save models. */
public final class Models {

    // 📏 Taille des images pour le prétraitement (peut être ajustée selon les besoins)
    public static final int IMAGE_HEIGHT = 100;
    public static final int IMAGE_WIDTH = 100;

    // 🧠 Nom du modèle
    public static final String MODEL_NAME = "shoeclassifier";

    private Models() {
        // static-only helper class
    }

    /** 📦 Retourne une instance vide du modèle sans architecture fixée */
    public static Model getModel() {
        return Model.newInstance(MODEL_NAME);
    }

    /** 📝 Sauvegarde des noms de classes dans un fichier synset.txt */
    public static void saveSynset(Path modelDir, List<String> synset) throws IOException {
        Path synsetFile = modelDir.resolve("synset.txt");
        try (Writer writer = Files.newBufferedWriter(synsetFile)) {
            writer.write(String.join("\n", synset));
        }
    }
}
