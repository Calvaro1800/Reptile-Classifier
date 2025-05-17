package ch.zhaw.deeplearningjava.reptile;

// ✅ Petit POJO pour envoyer proprement JSON vers JS
public class ClassificationResult {

    public String className;
    public double probability;

    public ClassificationResult(String className, double probability) {
        this.className = className;
        this.probability = probability;
    }
}
