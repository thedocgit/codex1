import express from "express";
import cors from "cors";

const app = express();
app.use(cors());
app.use(express.json());

app.get("/api/health", (req, res) => {
  res.json({ ok: true, service: "orquestrador-ia", status: "online" });
});

app.post("/api/run", async (req, res) => {
  const { prompt } = req.body;
  res.json({ ok: true, input: prompt, result: "Backend ativo" });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log("RUNNING"));