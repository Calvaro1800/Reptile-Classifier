package ch.zhaw.deeplearningjava.reptile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ai.djl.ModelException;
import ai.djl.modality.Classifications;
import ai.djl.translate.TranslateException;

@RestController
public class PredictionController {

    private final Inference inference;

    public PredictionController() {
        this.inference = new Inference();
    }

    // ✅ Route /predict avec support JSON propre pour le frontend
    @PostMapping(
        value = "/predict",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ClassificationResult>> predict(@RequestParam("image") MultipartFile image)
            throws IOException, ModelException, TranslateException {

        // 🇫🇷 Lit les bytes de l’image envoyée
        byte[] imageBytes = image.getBytes();

        // 🇫🇷 Prédit les classes via DJL
        Classifications result = inference.predict(imageBytes);

        // 🇫🇷 Structure les résultats pour que le frontend JS puisse les lire proprement
        List<ClassificationResult> formatted = result.items().stream()
                .map(item -> new ClassificationResult(item.getClassName(), item.getProbability()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(formatted);
    }
}
