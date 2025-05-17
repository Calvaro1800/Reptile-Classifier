package ch.zhaw.deeplearningjava.reptile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;

public class Inference {

    private final Predictor<Image, Classifications> predictor;

    public Inference() {
        try {
            // 🧠 Crée un Translator : Resize + ToTensor + Synset
            Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                    .addTransform(new Resize(224, 224)) // Taille pour EfficientNet-B0
                    .addTransform(new ToTensor())
                    .optApplySoftmax(true)
                    .optSynsetArtifactName("synset.txt") // Nom du fichier de classes
                    .build();

            // 📦 Définit les critères pour charger le modèle .pt
            Criteria<Image, Classifications> criteria = Criteria.builder()
                    .setTypes(Image.class, Classifications.class)
                    .optModelPath(Paths.get("/app/models"))
                    .optModelName("efficientnet_b0.pt") // ✅ Sans .pt !
                    .optTranslator(translator)
                    .optEngine("PyTorch") // DJL utilisera l'engine PyTorch
                    .build();

            // 📥 Charge le modèle et prépare le Predictor
            ZooModel<Image, Classifications> model = criteria.loadModel();
            this.predictor = model.newPredictor();

        } catch (Exception e) {
            throw new RuntimeException("❌ Erreur lors de l'initialisation du modèle DJL", e);
        }
    }

    /**
     * 🔍 Prédit la classe à partir d’un tableau d’octets (image binaire)
     * @param imageBytes tableau binaire (jpeg/png)
     * @return prédiction DJL sous forme de Classifications
     */
    public Classifications predict(byte[] imageBytes)
            throws ModelException, TranslateException, IOException {

        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            BufferedImage bufferedImage = ImageIO.read(is);
            Image img = ImageFactory.getInstance().fromImage(bufferedImage);
            return predictor.predict(img);
        }
    }
}
