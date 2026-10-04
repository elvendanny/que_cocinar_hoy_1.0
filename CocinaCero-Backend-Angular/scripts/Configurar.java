import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;

/** Configurador local: no envía correos, no modifica ninguna base de datos. */
public class Configurar {
  static final Scanner scanner = new Scanner(System.in);

  static String pedir(String texto, String defecto, boolean secreto) {
    String s;
    if (secreto && System.console() != null) {
      char[] chars = System.console().readPassword("%s: ", texto);
      s = new String(chars);
      Arrays.fill(chars, ' ');
    } else {
      System.out.print(texto + (secreto || defecto.isEmpty() ? "" : " [" + defecto + "]") + ": ");
      s = scanner.nextLine();
    }
    return s.isBlank() ? defecto : s;
  }

  static String secret() {
    byte[] b = new byte[48];
    new SecureRandom().nextBytes(b);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }

  static Properties load(Path p) throws Exception {
    Properties x = new Properties();
    if (Files.exists(p))
      try (var r = Files.newInputStream(p)) {
        x.load(r);
      }
    return x;
  }

  static void save(Path p, Properties x) throws Exception {
    try (var w = Files.newOutputStream(p)) {
      x.store(w, "Configuracion PRIVADA local Cocina Cero Deber 3");
    }
  }

  public static void main(String[] args) throws Exception {
    Path dir = Path.of("configuracion-local");
    Files.createDirectories(dir);
    var a = load(dir.resolve("api-auth.properties"));
    var c = load(dir.resolve("api-cocina.properties"));
    String host = pedir("Host MariaDB", "localhost", false);
    String port = pedir("Puerto MariaDB", "3306", false);
    a.setProperty(
        "spring.datasource.url", "jdbc:mariadb://" + host + ":" + port + "/cocina_cero_d3_auth_db");
    c.setProperty(
        "spring.datasource.url",
        "jdbc:mariadb://" + host + ":" + port + "/cocina_cero_d3_cocina_db");
    a.setProperty("spring.datasource.username", "cocina_d3_auth_user");
    c.setProperty("spring.datasource.username", "cocina_d3_cocina_user");
    String ap =
        pedir(
            "Clave MariaDB de cocina_d3_auth_user (Enter conserva la anterior)",
            a.getProperty("spring.datasource.password", ""),
            true);
    String cp =
        pedir(
            "Clave MariaDB de cocina_d3_cocina_user (Enter conserva la anterior)",
            c.getProperty("spring.datasource.password", ""),
            true);
    if (ap.isBlank() || cp.isBlank())
      throw new IllegalArgumentException("Debes indicar las claves de MariaDB.");
    a.setProperty("spring.datasource.password", ap);
    c.setProperty("spring.datasource.password", cp);
    a.setProperty(
        "app.admin-email",
        pedir(
            "Correo ADMIN de la aplicacion",
            a.getProperty("app.admin-email", "admin@cocinacero.local"),
            false));
    String admin =
        pedir(
            "Clave ADMIN (Enter genera una si es la primera vez)",
            a.getProperty("app.admin-password", ""),
            true);
    if (admin.isBlank()) admin = "Admin9!" + secret().substring(0, 18);
    if (admin.length() < 10 || admin.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException("Clave ADMIN: de 10 a 72 bytes.");
    a.setProperty("app.admin-password", admin);
    String from = pedir("Gmail autorizado para ENVIAR", a.getProperty("app.gmail-from", ""), false);
    if (!from.contains("@") || from.contains("\n") || from.contains("\r"))
      throw new IllegalArgumentException("Indica el correo remitente.");
    a.setProperty("app.gmail-from", from);
    a.putIfAbsent("app.jwt-secret", secret());
    String internal = a.getProperty("app.internal-key", secret());
    a.setProperty("app.internal-key", internal);
    c.setProperty("app.internal-key", internal);
    a.setProperty(
        "app.gmail-token", dir.resolve("token.json").toAbsolutePath().normalize().toString());
    c.setProperty(
        "app.uploads", Path.of("datos", "archivos").toAbsolutePath().normalize().toString());
    c.setProperty("app.auth-url", "http://localhost:8081");
    save(dir.resolve("api-auth.properties"), a);
    save(dir.resolve("api-cocina.properties"), c);
    System.out.println(
        "Configuracion guardada. Credenciales ADMIN en configuracion-local/api-auth.properties.");
    System.out.println("Token Gmail presente: " + Files.exists(dir.resolve("token.json")));
    System.out.println(
        "Si ADMIN ya existe, cambiar la clave en este archivo NO cambia su clave en la base.");
  }
}
