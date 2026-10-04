package com.cocinacero.cocina.service;

import com.cocinacero.cocina.dto.Datos;
import com.cocinacero.cocina.model.*;
import com.cocinacero.cocina.repository.*;
import com.cocinacero.comun.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogosService {
  private final Categorias cats;
  private final Ingredientes ing;
  private final Recetas recipes;
  private final Detalles details;

  public CatalogosService(Categorias c, Ingredientes i, Recetas r, Detalles d) {
    cats = c;
    ing = i;
    recipes = r;
    details = d;
  }

  public Categoria categoria(long id) {
    return cats.findById(id).orElseThrow(() -> new ApiError(404, "Categoría no encontrada."));
  }

  public Ingrediente ingrediente(long id) {
    return ing.findById(id).orElseThrow(() -> new ApiError(404, "Ingrediente no encontrado."));
  }

  public Object categorias() {
    return cats.findAll();
  }

  public Object ingredientes(String texto, String tipo) {
    return ing.findAll().stream()
        .filter(
            i ->
                texto == null
                    || i.nombre.toLowerCase(Locale.ROOT).contains(texto.toLowerCase(Locale.ROOT)))
        .filter(i -> tipo == null || i.tipo.equals(tipo))
        .toList();
  }

  @Transactional
  public Object categoria(Actor a, Long id, Datos.Categoria d) {
    a.requireAdmin();
    var c = id == null ? new Categoria() : categoria(id);
    if ((id == null || !c.nombre.equalsIgnoreCase(d.nombre().trim()))
        && cats.existsByNombreIgnoreCase(d.nombre().trim()))
      throw new ApiError(409, "Categoría duplicada.");
    c.nombre = d.nombre().trim();
    c.descripcion = d.descripcion();
    return cats.save(c);
  }

  @Transactional
  public Object ingrediente(Actor a, Long id, Datos.Ingrediente d) {
    a.requireAdmin();
    var i = id == null ? new Ingrediente() : ingrediente(id);
    if ((id == null || !i.nombre.equalsIgnoreCase(d.nombre().trim()))
        && ing.existsByNombreIgnoreCase(d.nombre().trim()))
      throw new ApiError(409, "Ingrediente duplicado.");
    i.nombre = d.nombre().trim();
    i.descripcion = d.descripcion();
    i.tipo = d.tipo();
    return ing.save(i);
  }

  @Transactional
  public void borrarCategoria(Actor a, long id) {
    a.requireAdmin();
    var c = categoria(id);
    if (recipes.existsByCategoriaId(id))
      throw new ApiError(409, "Categoría utilizada por una receta.");
    cats.delete(c);
  }

  @Transactional
  public void borrarIngrediente(Actor a, long id) {
    a.requireAdmin();
    var i = ingrediente(id);
    if (details.existsByIngredienteId(id))
      throw new ApiError(409, "Ingrediente utilizado por una receta.");
    ing.delete(i);
  }
}
