import express from "express";
import cors from "cors";
import path from "path";
import { fileURLToPath } from "url";
import { orchestrator } from "./core/orchestrator.js";

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
  try {
    const prompt = String(req.body?.input || req.body?.prompt || "").trim();
    if (!prompt) return res.status(400).json({ ok: false, result: "Digite um comando antes de executar." });
    const result = await orchestrator(prompt);
    res.json({ ok: true, ...result, output: result.result });
  } catch (error) {
    res.status(500).json({ ok: false, error: error.message || "Erro interno" });
  }
});

app.post("/api/run", async (req, res) => {
  try {
    const prompt = String(req.body?.input || req.body?.prompt || "").trim();
    if (!prompt) return res.status(400).json({ ok: false, result: "Digite um comando antes de executar." });
    const result = await orchestrator(prompt);
    res.json({ ok: true, input: prompt, ...result, output: result.result });
  } catch (error) {
    res.status(500).json({ ok: false, error: error.message || "Erro interno" });
  }
});

app.use((req, res) => {
  res.status(404).json({ ok: false, error: "Rota não encontrada" });
});

app.listen(PORT, () => console.log("RUNNING on port " + PORT));
