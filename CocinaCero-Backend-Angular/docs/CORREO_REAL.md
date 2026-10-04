# Gmail real y reutilización de la autorización

El servicio Java lee `token.json` con los campos client_id, client_secret y refresh_token del formato autorizado que ya generaste. Solicita un token de acceso a Google y envía el mensaje por Gmail API con alcance gmail.send. No utiliza tu contraseña personal de Gmail.

1. Copia token.json y credentials.json a configuracion-local.
2. En CONFIGURAR.cmd indica el mismo Gmail que autorizaste para enviar. El destinatario se introduce en el registro de cada cliente.
3. Registra un correo nuevo desde Postman. La comprobación real de entrega se realiza en tu equipo, revisando bandeja de entrada y spam.

No es necesario tener dominio propio ni que ambas personas estén en la misma red. Tu equipo necesita Internet y acceso HTTPS a Google. Si solo usas Postman en tu mismo equipo, las API pueden continuar en localhost.

Si aparece 502 y el token ya no sirve, puedes reutilizar el asistente de autorización incluido (requiere el Node que ya instalaste):

```powershell
npm.cmd ci --prefix herramientas-correo
node herramientas-correo/autorizar-gmail.js
```

El asistente utiliza credentials.json y guarda un token nuevo en la configuración local de ESTE proyecto. No ejecutes la autorización sin necesidad ni revoques el permiso del deber 2 para hacer las prácticas. Los dos proyectos utilizan sus copias locales, pero dependen del mismo permiso OAuth de Google: si lo revocas en la cuenta, puede afectar a ambos.

`credentials.json` se conserva para el asistente. La API Java utiliza directamente `token.json`. No subas ninguno a repositorios ni a la entrega. La aplicación no comprueba la vigencia de Gmail en /health; se comprueba al solicitar un envío.
