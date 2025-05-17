// 🔍 Vérifie l'image uploadée et appelle l'API
function checkFiles(files) {
  if (files.length !== 1) {
    alert("Bitte genau eine Datei hochladen.");
    return;
  }

  const file = files[0];
  const fileSize = file.size / 1024 / 1024;
  if (fileSize > 10) {
    alert("Datei zu gross (max. 10MB)");
    return;
  }

  const answerPart = document.getElementById("answerPart");
  const preview = document.getElementById("preview");
  const answer = document.getElementById("answer");
  answerPart.style.visibility = "visible";
  preview.src = URL.createObjectURL(file);
  answer.innerHTML = "🔍 Analyse läuft...";

  const formData = new FormData();
  formData.append("image", file);

  fetch("/predict", {
    method: "POST",
    body: formData
  })
    .then(res => res.text())
    .then(raw => {
      console.log("📦 RAW SERVER RESPONSE:", raw);
      let json;
      try {
        json = JSON.parse(raw);
      } catch (err) {
        console.error("❌ JSON parse error", err);
        answer.innerHTML = `<span style="color: red;">❌ Fehler bei der Analyse: JSON ungültig</span><br><pre>${raw}</pre>`;
        return;
      }

      if (Array.isArray(json) && json.length > 0 && json[0].className && json[0].probability) {
        const result = json[0];
        const name = result.className;
        const confidence = (result.probability * 100).toFixed(2);
        answer.innerHTML = `<strong>${name}</strong> (${confidence}%)`;
        updateReptileDex(name);
      } else {
        answer.innerHTML = `❌ Keine gültige Vorhersage.<br><pre>${raw}</pre>`;
      }
    })
    .catch(error => {
      console.error("❌ Fetch error:", error);
      answer.innerHTML = "❌ Fehler bei der Vorhersage.";
    });
}

// 🧬 Ajoute le nom dans le ReptilDex
function updateReptileDex(name) {
  const dex = document.getElementById("reptileDex");
  const items = dex.getElementsByTagName("li");

  if (items.length === 1 && items[0].innerText.includes("Noch keine")) {
    dex.innerHTML = '';
  }

  for (let item of items) {
    if (item.innerText.includes(name)) return;
  }

  const newItem = document.createElement("li");
  newItem.className = "list-group-item";
  newItem.innerText = `🦎 ${name}`;
  dex.appendChild(newItem);
}

// 🖼️ Galerie d'images aléatoires
const reptileImages = [
  "lizard_1.jpg", "lizard_2.jpg", "lizard_3.jpg",
  "gecko_1.jpg", "gecko_2.jpg", "snake_1.jpg", "snake_2.jpg",
  "crocodile_1.jpg", "crocodile_2.jpg",
  "chameleon_1.jpg", "chameleon_2.jpg",
  "salamander_1.jpg", "salamander_2.jpg"
];

function getCleanName(filename) {
  return filename.split("_")[0];
}

function shuffleArray(array) {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
}

function loadRandomImages() {
  const gallery = document.getElementById("gallery");
  gallery.innerHTML = "";
  const shuffled = shuffleArray(reptileImages).slice(0, 4);

  shuffled.forEach(img => {
    const name = getCleanName(img);
    const col = document.createElement("div");
    col.className = "col-md-3";
    col.innerHTML = `
      <div class="card bg-light shadow-sm border-0 mb-4">
        <img src="reptile_images/${img}" alt="${name}" class="card-img-top img-fluid" />
        <div class="card-body text-center">
          <h5 class="card-title mb-0">${name}</h5>
        </div>
      </div>
    `;
    gallery.appendChild(col);
  });
}

// 🎮 Quiz questions
const quizData = [
  { question: "Wie viele Beine haben die meisten Reptilien?", options: ["2", "4", "6", "8"], answer: "4" },
  { question: "Welches dieser Tiere ist ein Reptil?", options: ["Frosch", "Salamander", "Gecko", "Goldfisch"], answer: "Gecko" },
  { question: "Was ist das typische Merkmal von Reptilienhaut?", options: ["Feucht und glatt", "Trocken und schuppig", "Haarig", "Gefiedert"], answer: "Trocken und schuppig" },
  { question: "Wo legen die meisten Reptilien ihre Eier ab?", options: ["Im Wasser", "In Nestern am Boden", "In Bäumen", "Auf Blättern"], answer: "In Nestern am Boden" },
  { question: "Welches ist das größte lebende Reptil?", options: ["Komodowaran", "Alligator", "Lederschildkröte", "Salzwasserkrokodil"], answer: "Salzwasserkrokodil" }
];

let currentQuizIndex = 0;

function loadNextQuestion() {
  const quizQuestion = document.getElementById("quizQuestion");
  const quizOptions = document.getElementById("quizOptions");
  const quizResult = document.getElementById("quizResult");

  quizResult.innerHTML = "";

  if (currentQuizIndex >= quizData.length) {
    quizQuestion.innerHTML = "✅ Du hast alle Fragen beantwortet!";
    quizOptions.innerHTML = "";
    return;
  }

  const q = quizData[currentQuizIndex];
  quizQuestion.innerHTML = `<strong>Frage ${currentQuizIndex + 1}:</strong> ${q.question}`;

  quizOptions.innerHTML = "";
  q.options.forEach(opt => {
    const btn = document.createElement("button");
    btn.className = "btn btn-outline-success mb-2 me-2";
    btn.innerText = opt;
    btn.onclick = () => {
      if (opt === q.answer) {
        quizResult.innerHTML = "✅ Richtig!";
        quizResult.style.color = "green";
        currentQuizIndex++;
        setTimeout(loadNextQuestion, 1200);
      } else {
        quizResult.innerHTML = "❌ Falsch. Versuch es erneut!";
        quizResult.style.color = "red";
      }
    };
    quizOptions.appendChild(btn);
  });
}

document.addEventListener("DOMContentLoaded", () => {
  loadRandomImages();
  loadNextQuestion();
});
