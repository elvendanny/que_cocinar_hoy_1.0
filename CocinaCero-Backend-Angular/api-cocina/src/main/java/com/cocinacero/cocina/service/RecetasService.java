package com.cocinacero.cocina.service;

import com.cocinacero.cocina.dto.Datos;
import com.cocinacero.cocina.model.*;
import com.cocinacero.cocina.repository.*;
import com.cocinacero.comun.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecetasService {
  private final Recetas recipes;
  private final Detalles details;
  private final CatalogosService catalogos;
  private final Archivos files;

  public RecetasService(Recetas r, Detalles d, CatalogosService c, Archivos f) {
    recipes = r;
    details = d;
    catalogos = c;
    files = f;
  }

  public Receta entity(long id) {
    return recipes.findById(id).orElseThrow(() -> new ApiError(404, "Receta no encontrada."));
  }

  public void propietario(Actor a, Receta r) {
    if (!a.admin() && !r.usuarioId.equals(a.id()))
      throw new ApiError(403, "Solo el propietario o ADMIN puede modificar esta receta.");
  }

  public Map<String, Object> view(Receta r) {
    var m = new LinkedHashMap<String, Object>();
    m.put("id", r.id);
    m.put("nombre", r.nombre);
    m.put("descripcion", r.descripcion);
    m.put("instrucciones", r.instrucciones);
    m.put("tiempoPreparacion", r.tiempoPreparacion);
    m.put("porciones", r.porciones);
    m.put("dificultad", r.dificultad);
    m.put("categoriaId", r.categoria.id);
    m.put("categoria", r.categoria.nombre);
    m.put("usuarioId", r.usuarioId);
    m.put("creadoEn", r.creadoEn);
    var images = files.findByRecetaId(r.id).stream()
        .filter(f -> f.tipo.equals("image/png") || f.tipo.equals("image/jpeg"))
        .sorted(Comparator.comparing(f -> f.id)).toList();
    var cover = images.stream().filter(f -> f.id.equals(r.portadaArchivoId)).findFirst()
        .orElse(images.isEmpty() ? null : images.get(0));
    m.put("imagenPrincipal", cover != null
        ? Map.of("origen", "ARCHIVO", "archivoId", cover.id, "tipo", cover.tipo, "url", "/api/archivos/" + cover.id + "/imagen")
        : Map.of("origen", "DEMO", "tipo", "image/png", "url", "/api/imagenes-demo/" + (r.imagenDemo == null ? "receta.png" : r.imagenDemo)));
    m.put(
        "ingredientes",
        details.findByRecetaId(r.id).stream()
            .map(
                d ->
                    Map.of(
                        "ingredienteId",
                        d.ingrediente.id,
                        "nombre",
                        d.ingrediente.nombre,
                        "cantidad",
                        d.cantidad,
                        "unidadMedida",
                        d.unidadMedida,
                        "opcional",
                        d.opcional))
            .toList());
    m.put(
        "archivos",
        files.findByRecetaId(r.id).stream()
            .map(
                f ->
                    Map.of(
                        "id",
                        f.id,
                        "nombreOriginal",
                        f.nombreOriginal,
                        "tipo",
                        f.tipo,
                        "bytes",
                        f.bytes))
            .toList());
    return m;
  }

  @Transactional(readOnly = true)
  public Object get(long id) {
    return view(entity(id));
  }

  @Transactional(readOnly = true)
  public Object list(String texto, Long categoriaId, String dificultad) {
    return list(texto, categoriaId, dificultad, null);
  }

  private org.springframework.data.jpa.domain.Specification<Receta> filtro(String texto, Long categoriaId, String dificultad, Long autor) {
    if (categoriaId != null && categoriaId <= 0) throw new ApiError(400, "Categoría inválida.");
    if (dificultad != null && !Set.of("FACIL", "MEDIA", "DIFICIL").contains(dificultad))
      throw new ApiError(400, "Dificultad inválida.");
    return (root, query, cb) -> {
      var condiciones = new ArrayList<jakarta.persistence.criteria.Predicate>();
      if (texto != null && !texto.isBlank()) {
        String valor = "%" + texto.trim().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        condiciones.add(cb.like(cb.lower(root.get("nombre")), valor, '!'));
      }
      if (categoriaId != null) condiciones.add(cb.equal(root.get("categoria").get("id"), categoriaId));
      if (dificultad != null) condiciones.add(cb.equal(root.get("dificultad"), dificultad));
      if (autor != null) condiciones.add(cb.equal(root.get("usuarioId"), autor));
      return cb.and(condiciones.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };
  }

  @Transactional(readOnly = true)
  public Object list(String texto, Long categoriaId, String dificultad, Long autor) {
    return recipes.findAll(filtro(texto, categoriaId, dificultad, autor), org.springframework.data.domain.Sort.by("id").descending()).stream().map(this::view).toList();
  }

  @Transactional(readOnly = true)
  public Object pagina(String texto, Long categoriaId, String dificultad, Long autor, int page, int limit) {
    if (page < 1 || limit < 1 || limit > 50) throw new ApiError(400, "Paginación inválida (limit: 1 a 50).");
    var result = recipes.findAll(filtro(texto, categoriaId, dificultad, autor),
        org.springframework.data.domain.PageRequest.of(page - 1, limit, org.springframework.data.domain.Sort.by("id").descending()));
    return Map.of("datos", result.getContent().stream().map(this::view).toList(), "total", result.getTotalElements(),
        "page", page, "limit", limit, "paginas", result.getTotalPages());
  }

  @Transactional
  public Object portada(Actor a, long id, Long archivoId) {
    var r = entity(id);
    propietario(a, r);
    if (archivoId != null) {
      var file = files.findById(archivoId).orElseThrow(() -> new ApiError(404, "Archivo no encontrado."));
      if (!file.receta.id.equals(r.id)) throw new ApiError(400, "La imagen debe pertenecer a esta receta.");
      if (!Set.of("image/png", "image/jpeg").contains(file.tipo)) throw new ApiError(400, "La portada debe ser PNG o JPEG.");
    }
    r.portadaArchivoId = archivoId;
    recipes.save(r);
    return view(r);
  }

  @Transactional
  public Object save(Actor a, Long id, Datos.Receta d) {
    var r = id == null ? new Receta() : entity(id);
    if (id == null) r.usuarioId = a.id();
    else propietario(a, r);
    var seen = new HashSet<Long>();
    for (var i : d.ingredientes()) {
      if (!seen.add(i.ingredienteId())) throw new ApiError(400, "No repitas ingredientes.");
      catalogos.ingrediente(i.ingredienteId());
    }
    r.nombre = d.nombre().trim();
    r.descripcion = d.descripcion();
    r.instrucciones = d.instrucciones();
    r.tiempoPreparacion = d.tiempoPreparacion();
    r.porciones = d.porciones();
    r.dificultad = d.dificultad();
    r.categoria = catalogos.categoria(d.categoriaId());
    recipes.saveAndFlush(r);
    if (id != null) {
      details.deleteByRecetaId(id);
      details.flush();
    }
    for (var i : d.ingredientes()) {
      var detail = new RecetaIngrediente();
      detail.receta = r;
      detail.ingrediente = catalogos.ingrediente(i.ingredienteId());
      detail.cantidad = i.cantidad();
      detail.unidadMedida = i.unidadMedida();
      detail.opcional = i.opcional();
      details.save(detail);
    }
    details.flush();
    return view(r);
  }

  @Transactional
  public void remove(Actor a, long id) {
    var r = entity(id);
    propietario(a, r);
    if (files.countByRecetaId(id) > 0)
      throw new ApiError(409, "Elimina primero los archivos de esta receta.");
    details.deleteByRecetaId(id);
    details.flush();
    recipes.delete(r);
  }

  @Transactional(readOnly = true)
  public Object recomendaciones(String ids) {
    Set<Long> selected = new HashSet<>();
    try {
      for (String s : ids.split(",")) {
        long n = Long.parseLong(s.trim());
        if (n <= 0) throw new IllegalArgumentException();
        selected.add(n);
      }
      if (selected.isEmpty() || selected.size() > 50) throw new IllegalArgumentException();
    } catch (Exception e) {
      throw new ApiError(400, "Envía de 1 a 50 IDs positivos separados por coma.");
    }
    var out = new ArrayList<Map<String, Object>>();
    for (var r : recipes.findAll()) {
      var required = details.findByRecetaId(r.id).stream().filter(d -> !d.opcional).toList();
      long coincidencias =
          required.stream().filter(d -> selected.contains(d.ingrediente.id)).count();
      if (coincidencias == 0) continue;
      var m = view(r);
      m.put("coincidencias", coincidencias);
      m.put("porcentaje", Math.round(10000.0 * coincidencias / required.size()) / 100.0);
      out.add(m);
    }
    out.sort(
        (a, b) ->
            Double.compare(
                ((Number) b.get("porcentaje")).doubleValue(),
                ((Number) a.get("porcentaje")).doubleValue()));
    return out;
  }
}
