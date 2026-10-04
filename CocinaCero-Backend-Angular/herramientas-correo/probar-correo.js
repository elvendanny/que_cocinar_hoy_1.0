const fs = require("node:fs/promises");
const path = require("node:path");
const readline = require("node:readline/promises");
const { google } = require("googleapis");
(async () => {
  const token = JSON.parse(
    await fs.readFile(
      path.resolve(__dirname, "../configuracion-local/token.json"),
      "utf8",
    ),
  );
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });
  let sender, to;
  try {
    sender = (await rl.question("Gmail autorizado para enviar: ")).trim();
    to = (await rl.question("Correo destinatario de la prueba: ")).trim();
  } finally {
    rl.close();
  }
  const valid = /^[^\s<>@,;]+@[^\s<>@,;]+\.[^\s<>@,;]+$/;
  if (!valid.test(sender) || !valid.test(to))
    throw new Error("Correo inválido");
  const message = [
    `From: Cocina Cero <${sender}>`,
    `To: ${to}`,
    "Subject: Cocina Cero - Prueba de correo",
    `Date: ${new Date().toUTCString()}`,
    "MIME-Version: 1.0",
    "Content-Type: text/plain; charset=UTF-8",
    "",
    "Prueba real de Cocina Cero. Todavía no valida una cuenta.",
  ].join("\r\n");
  await google
    .gmail({ version: "v1", auth: google.auth.fromJSON(token) })
    .users.messages.send(
      {
        userId: "me",
        requestBody: { raw: Buffer.from(message).toString("base64url") },
      },
      { timeout: 15000, retry: false },
    );
  console.log("Gmail aceptó el envío. Revisa entrada y spam.");
})().catch((e) => {
  console.error(
    "No se confirmó el envío. Revisa Enviados antes de repetir. Código:",
    e.response?.status || e.code || e.name,
  );
  process.exitCode = 1;
});
