package ch.zhaw.deeplearningjava.reptile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ai.djl.modality.Classifications;
import jakarta.annotation.PostConstruct;

@RestController
public class ClassificationController {

    private Inference inference;

    @PostConstruct
    public void init() {
        try {
            inference = new Inference();
        } catch (Exception e) {
            System.err.println("❌ Échec de l'initialisation du modèle DJL : " + e.getMessage());
            inference = null;
        }
    }

    @GetMapping("/ping")
    public String ping() {
        return "Classification app is up and running!";
    }

    @PostMapping(path = "/analyze")
    public String predict(@RequestParam("image") MultipartFile image) {
        try {
            if (inference == null) {
                return "{\"error\": \"Model not initialized.\"}";
            }
            Classifications result = inference.predict(image.getBytes());
            return result.toJson();
        } catch (Exception e) {
            return "{\"error\": \"Prediction failed: " + e.getMessage() + "\"}";
        }
    }
}
