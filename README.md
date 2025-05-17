
# Projekt 2 Java

---

## Übersicht

|                                | Bitte ausfüllen                                                                                        |
| ------------------------------ | ------------------------------------------------------------------------------------------------------ |
| Variante                       | Vorhandenes Modell + eigener Datensatz + eigene UI + Docker Deployment                                 |
| Datensatz (wenn selbstgewählt) | Bilder (JPEG, PNG), Reptilienkategorien: Gecko, Salamander, Schlange...                                |
| Datensatz (wenn selbstgewählt) | Lokal gespeichert (\~6000 Bilder), nicht im Repo                                                       |
| Modell (wenn selbstgewählt)    | EfficientNet B0 (aus DJL Model Zoo) – beibehalten trotz schwacher Ergebnisse                           |
| ML-Algorithmus                 | CNN-basierte Klassifikation via DJL EfficientNet                                                       |
| Repo URL                       | [https://github.com/Calvaro1800/Reptile-Classifier](https://github.com/Calvaro1800/Reptile-Classifier) |

---

## Dokumentation

---

### ✅ Daten

Zu Beginn wollte ich einen Dinosaurier-Klassifikator entwickeln – ungewöhnlich, aber spannend. Ich fand ein Kaggle-Dataset mit hunderten Bildern. Doch die Realität: viele Bilder waren **KI-generiert, 3D oder Comic**. Selbst für das Auge war es inkonsistent, für ein CNN-Modell **nicht lernbar**.

Ich habe dann auf **echte Reptilienarten** umgestellt:

* Gecko
* Lizard
* Snake
* Salamander
* Turtle

Die Bilder wurden in dieser Struktur gespeichert (nicht im GitHub enthalten):

```
src/main/resources/dataset/
├── Gecko/
├── Lizard/
├── Snake/
├── Salamander/
├── Turtle/
```

> DJL `ImageFolder` akzeptiert genau dieses Format.
> Split in 80% Training, 20% Validierung erfolgt automatisch über `.randomSplit()`.

---

### ✅ Training

Ich habe zuerst **ResNet50** benutzt, wie im mdm-10-djl Beispiel. Dann dachte ich: *warum nicht ein leichteres Modell wie EfficientNet B0?* – vortrainiert, kleiner, angeblich robuster.

Ich habe in `Models.java` Folgendes integriert:

```java
Block block = EfficientNet.builder()
    .setNumClasses(numClasses)
    .setImageShape(new Shape(3, 100, 100))
    .build();
model.setBlock(block);
```

Das Training (`Training.java`) wurde vollständig vorbereitet:

```java
ImageFolder dataset = ImageFolder.builder()
    .setRepositoryPath(Paths.get("src/main/resources/dataset"))
    .addTransform(new Resize(100, 100))
    .addTransform(new ToTensor())
    .setSampling(32, true)
    .build();
dataset.prepare();

Model model = Models.getModel();
Trainer trainer = model.newTrainer(config);
trainer.initialize(new Shape(1, 3, 100, 100));
EasyTrain.fit(trainer, 10, datasets[0], datasets[1]);
```

Aber:
Die Ergebnisse mit EfficientNet waren **nicht überzeugend**. Ein Gecko wurde z.B. als Maus erkannt. Ein Salamander als Fahrrad. Ich wollte zurück zu ResNet50 wechseln, aber:

* Es war zu spät
* Ich wollte das Deployment nicht gefährden
* Der Aufwand war nicht realistisch in der Woche vor Abgabe

➡️ **Ich habe EfficientNet B0 trotzdem behalten**, um das Projekt zu Ende zu bringen.

---

### ✅ Inference / Serving

Die Inferenz läuft via DJL `Criteria` + `Translator` + `Predictor`. Der Endpoint `/predict` verarbeitet ein MultipartFile (Bild) und liefert:

```json
{
  "className": "Gecko",
  "probability": 0.9421
}
```

Beispiel aus `ClassificationController.java`:

```java
Criteria<Image, Classifications> criteria = Criteria.builder()
    .setTypes(Image.class, Classifications.class)
    .optModelPath(Paths.get("src/main/resources/models"))
    .optTranslator(new MyTranslator())
    .optEngine("PyTorch")
    .build();

try (ZooModel<Image, Classifications> model = criteria.loadModel();
     Predictor<Image, Classifications> predictor = model.newPredictor()) {
    result = predictor.predict(img);
}
```

Das Frontend (in `index.html` + `script.js`) zeigt:

* Uploadfeld
* Bildvorschau
* Klassifikation (Name + Prozent)
* Interaktives Reptilien-Quiz (Multiple Choice)

---

### ✅ Deployment

#### 🔧 Lokal:

```bash
./mvnw clean package
java -jar target/reptile-classifier-0.0.1-SNAPSHOT.jar
```

#### 🐳 Docker:

```Dockerfile
FROM eclipse-temurin:21-jdk
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t reptile-classifier .
docker run -p 8080:8080 reptile-classifier
```

#### ☁️ Azure:

```bash
az webapp config appsettings set \
  --name reptile-classifier-app \
  --resource-group reptile-rg \
  --settings WEBSITES_PORT=8080

az webapp up --name reptile-classifier-app \
  --resource-group reptile-rg \
  --sku F1 --location westeurope
```

✅ Die App läuft live unter:
🔗 [https://reptile-classifier-app.azurewebsites.net/](https://reptile-classifier-app.azurewebsites.net/)

