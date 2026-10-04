package com.cocinacero.cocina.service;

import com.cocinacero.cocina.model.*;
import com.cocinacero.cocina.repository.*;
import com.cocinacero.comun.*;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ArchivosService {
  private final Archivos files;
  private final RecetasService recipes;
  private final Path root;

  public ArchivosService(Archivos f, RecetasService r, @Value("${app.uploads}") String dir) {
    files = f;
    recipes = r;
    root = Path.of(dir).toAbsolutePath().normalize();
  }

  private String tipo(byte[] b) {
    if (b.length >= 8
        && b[0] == (byte) 137
        && b[1] == 80
        && b[2] == 78
        && b[3] == 71
        && b[4] == 13
        && b[5] == 10
        && b[6] == 26
        && b[7] == 10) return "image/png";
    if (b.length >= 3 && b[0] == (byte) 255 && b[1] == (byte) 216 && b[2] == (byte) 255)
      return "image/jpeg";
    if (b.length >= 5
        && new String(b, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))
      return "application/pdf";
    throw new ApiError(400, "Solo se permiten PNG, JPEG y PDF con firma reconocida.");
  }

  private void validarImagen(byte[] bytes) throws java.io.IOException {
    try (var input = javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bytes))) {
      var readers = javax.imageio.ImageIO.getImageReaders(input);
      if (!readers.hasNext()) throw new ApiError(400, "Imagen dañada o no reconocida.");
      var reader = readers.next();
      try {
        reader.setInput(input);
        long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
        if (pixels > 20000000 || pixels <= 0) throw new ApiError(400, "Imagen demasiado grande (máximo 20 megapíxeles).");
        if (reader.read(0) == null) throw new ApiError(400, "Imagen dañada.");
      } catch (java.io.IOException ex) { throw new ApiError(400, "Imagen dañada."); }
      finally { reader.dispose(); }
    }
  }

  @Transactional
  public Object subir(Actor a, long recetaId, MultipartFile file) {
    var r = recipes.entity(recetaId);
    recipes.propietario(a, r);
    if (files.countByRecetaId(recetaId) >= 10)
      throw new ApiError(409, "Máximo 10 archivos por receta.");
    if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
      throw new ApiError(400, "Archivo vacío o mayor de 5 MB.");
    Path p = null;
    try {
      byte[] bytes = file.getBytes();
      String type = tipo(bytes);
      if (type.startsWith("image/")) validarImagen(bytes);
      Files.createDirectories(root);
      var f = new Archivo();
      f.receta = r;
      f.tipo = type;
      String original =
          Objects.toString(file.getOriginalFilename(), "archivo").replaceAll("[\\/\r\n]", "_");
      f.nombreOriginal = original.substring(0, Math.min(255, original.length()));
      f.nombreDisco = UUID.randomUUID().toString();
      f.bytes = bytes.length;
      p = root.resolve(f.nombreDisco);
      Files.write(p, bytes, StandardOpenOption.CREATE_NEW);
      files.saveAndFlush(f);
      return Map.of(
          "id",
          f.id,
          "recetaId",
          recetaId,
          "nombreOriginal",
          f.nombreOriginal,
          "tipo",
          f.tipo,
          "bytes",
          f.bytes);
    } catch (Exception e) {
      if (p != null)
        try {
          Files.deleteIfExists(p);
        } catch (Exception ignored) {
        }
      if (e instanceof ApiError x) throw x;
      throw new ApiError(500, "No se pudo guardar el archivo.");
    }
  }

  public Archivo get(long id) {
    return files.findById(id).orElseThrow(() -> new ApiError(404, "Archivo no encontrado."));
  }

  public byte[] bytes(long id) {
    try {
      return Files.readAllBytes(root.resolve(get(id).nombreDisco));
    } catch (java.io.IOException e) {
      throw new ApiError(404, "Contenido del archivo no disponible.");
    }
  }

  @Transactional
  public void remove(Actor a, long id) {
    var f = get(id);
    recipes.propietario(a, f.receta);
    try {
      Files.deleteIfExists(root.resolve(f.nombreDisco));
      if (f.id.equals(f.receta.portadaArchivoId)) f.receta.portadaArchivoId = null;
      files.delete(f);
    } catch (java.io.IOException e) {
      throw new ApiError(500, "No se pudo eliminar el archivo.");
    }
  }
}
