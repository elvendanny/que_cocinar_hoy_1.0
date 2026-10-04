package com.cocinacero.cocina.config;

import com.cocinacero.cocina.model.*;
import com.cocinacero.cocina.repository.*;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.springframework.core.annotation.Order(0)
public class Semilla implements CommandLineRunner {
  private final Categorias cats;
  private final Ingredientes ing;
  private final Recetas recipes;
  private final Detalles details;
  private final boolean enabled;

  public Semilla(
      Categorias c,
      Ingredientes i,
      Recetas r,
      Detalles d,
      @Value("${app.seed.enabled}") boolean en) {
    cats = c;
    ing = i;
    recipes = r;
    details = d;
    enabled = en;
  }

  @Transactional
  public void run(String... args) {
    if (!enabled || cats.count() > 0) return;
    var c = new Categoria();
    c.nombre = "Comidas rápidas";
    c.descripcion = "Preparaciones sencillas";
    cats.save(c);
    var i = new Ingrediente();
    i.nombre = "Tomate";
    i.tipo = "VEGETAL";
    i.descripcion = "Tomate fresco";
    ing.save(i);
    var j = new Ingrediente();
    j.nombre = "Arroz";
    j.tipo = "CEREAL";
    j.descripcion = "Arroz blanco";
    ing.save(j);
    var k = new Ingrediente();
    k.nombre = "Huevo";
    k.tipo = "PROTEINA";
    k.descripcion = "Huevo de gallina";
    ing.save(k);
    String[] names = {"Arroz con tomate", "Arroz con huevo", "Tomate con huevo"};
    Ingrediente[][] pairs = {{i, j}, {j, k}, {i, k}};
    for (int n = 0; n < 3; n++) {
      var r = new Receta();
      r.nombre = names[n];
      r.descripcion = "Receta de demostración precargada";
      r.instrucciones = "Preparar los ingredientes, cocinar hasta que estén listos y servir.";
      r.tiempoPreparacion = 20;
      r.porciones = 2;
      r.dificultad = "FACIL";
      r.categoria = c;
      r.usuarioId = 0L;
      recipes.save(r);
      for (var item : pairs[n]) {
        var d = new RecetaIngrediente();
        d.receta = r;
        d.ingrediente = item;
        d.cantidad = new BigDecimal("1.00");
        d.unidadMedida = "UNIDADES";
        details.save(d);
      }
    }
  }
}
