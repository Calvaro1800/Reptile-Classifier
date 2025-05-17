package ch.zhaw.deeplearningjava.reptile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SynsetGenerator {

    public static void main(String[] args) {
        // 📁 Dossier contenant les sous-dossiers (chaque classe de reptile)
        File datasetDir = new File("src/main/resources/dataset"); // ✅ ADAPTÉ à ton projet

        // 📄 Emplacement de sortie du fichier synset.txt
        File synsetFile = new File("models/synset.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(synsetFile))) {
            File[] classDirs = datasetDir.listFiles(File::isDirectory);

            if (classDirs != null && classDirs.length > 0) {
                for (File classDir : classDirs) {
                    writer.write(classDir.getName());
                    writer.newLine();
                }
                System.out.println("✅ Fichier synset.txt généré avec " + classDirs.length + " classes.");
            } else {
                System.out.println("❌ Aucun dossier trouvé dans : " + datasetDir.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }
}
