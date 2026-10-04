const fs = require("node:fs/promises");
const path = require("node:path");
const { authenticate } = require("@google-cloud/local-auth");
(async () => {
  const dir = path.resolve(__dirname, "../configuracion-local");
  const keyfilePath = path.join(dir, "credentials.json");
  const keys = JSON.parse(await fs.readFile(keyfilePath, "utf8")).installed;
  if (!keys)
    throw new Error("Se requiere un cliente de Aplicación de escritorio.");
  const scope = "https://www.googleapis.com/auth/gmail.send";
  console.log(
    "Se abrirá Google. Autoriza la cuenta remitente registrada como usuario de prueba.",
  );
  const client = await authenticate({ keyfilePath, scopes: [scope] });
  if (!client.credentials.refresh_token)
    throw new Error(
      "No se recibió un token de renovación. Revisa la autorización de Google.",
    );
  const access = await client.getAccessToken();
  const info = await client.getTokenInfo(access.token);
  if (!info.scopes.includes(scope))
    throw new Error("No se concedió el permiso de envío.");
  await fs.writeFile(
    path.join(dir, "token.json"),
    JSON.stringify(
      {
        type: "authorized_user",
        client_id: keys.client_id,
        client_secret: keys.client_secret,
        refresh_token: client.credentials.refresh_token,
      },
      null,
      2,
    ),
    { mode: 0o600 },
  );
  console.log(
    "Autorización completada. Token guardado en configuracion-local/token.json.",
  );
})().catch((e) => {
  console.error("No se completó la autorización. Código:", e.code || e.name);
  process.exitCode = 1;
});
