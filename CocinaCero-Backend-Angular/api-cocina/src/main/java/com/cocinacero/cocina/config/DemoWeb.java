package com.cocinacero.cocina.config;

import com.cocinacero.cocina.model.*;
import com.cocinacero.cocina.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Carga una sola vez. No cambia ni resucita recetas de usuarios. */
@Component
@Order(10)
public class DemoWeb implements CommandLineRunner {
  private final Categorias cats;
  private final Ingredientes ingredients;
  private final Recetas recipes;
  private final Detalles details;
  private final CargasIniciales cargas;
  private final ObjectMapper json;
  private final boolean enabled;
  public DemoWeb(Categorias c, Ingredientes i, Recetas r, Detalles d, CargasIniciales m,
      ObjectMapper j, @Value("${app.seed.enabled:true}") boolean enabled,
      @Value("${app.seed.web-enabled:true}") boolean web) {
    cats=c; ingredients=i; recipes=r; details=d; cargas=m; json=j; this.enabled=enabled && web;
  }
  @Override @Transactional
  public void run(String... args) throws Exception {
    if (!enabled || cargas.existsById("demo-web-v1")) return;
    try (var stream = new ClassPathResource("demo/recetas.json").getInputStream()) {
      for (var node : json.readTree(stream)) {
        if (recipes.findAll().stream().anyMatch(r -> r.nombre.equalsIgnoreCase(node.path("nombre").asText()))) continue;
        String category = node.path("categoria").asText();
        var cat = cats.findAll().stream().filter(x -> x.nombre.equalsIgnoreCase(category)).findFirst().orElseGet(() -> {
          var x = new Categoria(); x.nombre=category; x.descripcion="Categoría del recetario"; return cats.save(x);
        });
        var recipe = new Receta();
        recipe.nombre=node.path("nombre").asText(); recipe.descripcion=node.path("descripcion").asText();
        recipe.instrucciones=node.path("instrucciones").asText(); recipe.tiempoPreparacion=node.path("tiempoPreparacion").asInt();
        recipe.porciones=node.path("porciones").asInt(); recipe.dificultad="FACIL"; recipe.usuarioId=0L;
        recipe.categoria=cat; recipe.imagenDemo=node.path("imagen").asText(); recipes.save(recipe);
        for (var item : node.path("ingredientes")) {
          String name=item.path("nombre").asText();
          var ingredient=ingredients.findAll().stream().filter(x -> x.nombre.equalsIgnoreCase(name)).findFirst().orElseGet(() -> {
            var x=new Ingrediente(); x.nombre=name; x.descripcion=name+" para cocinar"; x.tipo=item.path("tipo").asText(); return ingredients.save(x);
          });
          var detail=new RecetaIngrediente(); detail.receta=recipe; detail.ingrediente=ingredient;
          detail.cantidad=new BigDecimal(item.path("cantidad").asText()); detail.unidadMedida=item.path("unidadMedida").asText();
          detail.opcional=item.path("opcional").asBoolean(false); details.save(detail);
        }
      }
    }
    var marker=new CargaInicial(); marker.clave="demo-web-v1"; cargas.save(marker);
  }
}
