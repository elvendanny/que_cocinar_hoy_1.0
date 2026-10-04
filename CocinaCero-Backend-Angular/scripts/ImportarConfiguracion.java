import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Copia privada local. No conecta a MariaDB ni modifica el proyecto anterior. */
public class ImportarConfiguracion {
  static Properties load(Path p) throws IOException {
    var result = new Properties();
    try (var in = Files.newInputStream(p)) { result.load(in); }
    return result;
  }
  static void save(Path p, Properties values) throws IOException {
    try (var out = Files.newOutputStream(p)) { values.store(out, "Configuracion PRIVADA Cocina Cero - Angular"); }
  }
  static Path resolve(Path base, String text) {
    Path p = Path.of(text);
    return (p.isAbsolute() ? p : base.resolve(p)).toAbsolutePath().normalize();
  }
  static void copyTree(Path source, Path destination) throws IOException {
    if (!Files.isDirectory(source)) return;
    if (destination.startsWith(source)) throw new IOException("La carpeta de destino no puede estar dentro del origen.");
    try (var paths = Files.walk(source)) {
      for (var file : paths.toList()) {
        if (Files.isSymbolicLink(file)) throw new IOException("No se copian enlaces simbolicos.");
        Path target = destination.resolve(source.relativize(file));
        if (Files.isDirectory(file)) Files.createDirectories(target);
        else Files.copy(file, target); // No reemplaza archivos previos.
      }
    }
  }
  public static void main(String[] args) {
    try { run(args); }
    catch (Exception e) {
      System.err.println("No se completo la importacion: " + e.getMessage());
      System.err.println("El proyecto anterior sigue intacto. Revisa la ruta indicada.");
      System.exit(1);
    }
  }
  static void run(String[] args) throws Exception {
    String input;
    if (args.length > 0) input = args[0];
    else {
      System.out.println("Ruta de la carpeta anterior CocinaCero-Deber3 (contiene configuracion-local):");
      input = new Scanner(System.in).nextLine();
    }
    input = input.trim();
    if (input.startsWith("\"") && input.endsWith("\"")) input=input.substring(1,input.length()-1);
    Path old = Path.of(input).toAbsolutePath().normalize();
    Path root = Path.of("").toAbsolutePath().normalize();
    if (old.equals(root)) throw new IOException("Selecciona el proyecto ANTERIOR, no esta carpeta nueva.");
    Path source = old.resolve("configuracion-local");
    Path destination = root.resolve("configuracion-local");
    var auth = load(source.resolve("api-auth.properties"));
    var cocina = load(source.resolve("api-cocina.properties"));
    if (Files.exists(destination.resolve("api-auth.properties")) || Files.exists(destination.resolve("api-cocina.properties")) || Files.exists(destination.resolve("token.json")))
      throw new IOException("Ya hay configuracion privada en esta copia. No se sobrescribe; si ya importaste, continua con COMPILAR_Y_PROBAR.");
    if (!auth.getProperty("app.internal-key", "").equals(cocina.getProperty("app.internal-key", "")) || auth.getProperty("app.internal-key", "").length() < 32)
      throw new IOException("Las claves internas del proyecto anterior no coinciden o son invalidas.");
    Path token = resolve(old, auth.getProperty("app.gmail-token", "configuracion-local/token.json"));
    if (!Files.isRegularFile(token)) throw new IOException("No se encuentra el token Gmail configurado en el proyecto anterior.");
    Path uploads = resolve(old, cocina.getProperty("app.uploads", "datos/archivos"));
    Path newUploads = root.resolve("datos/archivos");
    if (Files.exists(newUploads)) throw new IOException("Ya existe datos/archivos en esta copia; no se sobrescribe.");
    // Primero se prepara todo en una carpeta temporal; solo se publica cuando la copia termina.
    Path stage = Files.createTempDirectory(root, ".importacion-");
    try {
      Path stagedConfig=stage.resolve("configuracion-local");
      copyTree(source, stagedConfig);
      Files.copy(token, stagedConfig.resolve("token.json"), StandardCopyOption.REPLACE_EXISTING);
      Path stagedUploads=stage.resolve("archivos");
      copyTree(uploads, stagedUploads);
      if (!Files.exists(stagedUploads)) Files.createDirectories(stagedUploads);
      auth.setProperty("app.gmail-token", destination.resolve("token.json").toString());
      cocina.setProperty("app.uploads", newUploads.toString());
      for (var properties : List.of(auth,cocina))
        properties.putIfAbsent("app.cors.allowed-origins", "http://localhost:4200,http://127.0.0.1:4200");
      save(stagedConfig.resolve("api-auth.properties"), auth);
      save(stagedConfig.resolve("api-cocina.properties"), cocina);
      Files.createDirectories(destination);
      // LEEME.txt puede existir en el ZIP; solo ese archivo se reemplaza.
      try (var paths=Files.walk(stagedConfig)) {
        for (var file : paths.toList()) {
          var target=destination.resolve(stagedConfig.relativize(file));
          if (Files.isDirectory(file)) Files.createDirectories(target);
          else if (file.getFileName().toString().equals("LEEME.txt")) Files.copy(file,target,StandardCopyOption.REPLACE_EXISTING);
          else Files.copy(file,target);
        }
      }
      Files.createDirectories(newUploads.getParent());
      Files.move(stagedUploads,newUploads);
      System.out.println("Configuracion y archivos copiados. No se muestran claves ni se envio correo.");
      System.out.println("Se conservan las MISMAS dos bases MariaDB, usuarios y credenciales de Gmail.");
      System.out.println("No ejecutes la version anterior a la vez. Siguiente: COMPILAR_Y_PROBAR.cmd.");
      if (!Files.isDirectory(uploads)) System.out.println("La carpeta anterior de adjuntos no existia; se creo una vacia.");
    } finally {
      try (var paths = Files.walk(stage)) {
        for (var file : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
      }
    }
  }
}
