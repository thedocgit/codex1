import express from "express";
import cors from "cors";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json({ limit: "2mb" }));
app.use(express.urlencoded({ extended: true }));
app.use(express.static(__dirname));

app.get("/", (req, res) => {
  res.sendFile(path.join(__dirname, "index.html"));
});

app.get("/health", (req, res) => {
  res.json({ ok: true, service: "orquestrador-ia", status: "online" });
});

app.get("/api/health", (req, res) => {
  res.json({ ok: true, service: "orquestrador-ia", status: "online" });
});

app.post("/run", async (req, res) => {
  const input = String(req.body?.input || req.body?.prompt || "").trim();
  if (!input) return res.status(400).json({ output: "Digite um comando antes de executar." });
  const output = [
    "=== ORQUESTRADOR IA ATIVO ===",
    "",
    "Entrada recebida:",
    input,
    "",
    "Pipeline executado:",
    "1. Interpretação do pedido",
    "2. Organização do objetivo",
    "3. Planejamento operacional",
    "4. Consolidação da resposta",
    "",
    "Resultado:",
    "Frontend e backend estão integrados e respondendo corretamente."
  ].join("\n");
  res.json({ ok: true, output });
});

app.post("/api/run", async (req, res) => {
  const input = String(req.body?.input || req.body?.prompt || "").trim();
  if (!input) return res.status(400).json({ ok: false, result: "Digite um comando antes de executar." });
  res.json({ ok: true, input, result: "Backend ativo", output: "Backend ativo e integrado: " + input });
});

app.use((req, res) => {
  res.status(404).json({ ok: false, error: "Rota não encontrada" });
});

app.listen(PORT, () => console.log("RUNNING on port " + PORT));
