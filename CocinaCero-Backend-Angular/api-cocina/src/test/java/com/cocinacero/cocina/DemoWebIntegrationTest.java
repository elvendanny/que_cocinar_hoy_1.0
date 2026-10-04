package com.cocinacero.cocina;
import static org.junit.jupiter.api.Assertions.*;
import com.cocinacero.cocina.config.DemoWeb;
import com.cocinacero.cocina.model.*;
import com.cocinacero.cocina.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
@SpringBootTest(properties={"spring.config.import=", "app.seed.web-enabled=false", "spring.datasource.url=jdbc:h2:mem:migracionweb;MODE=MariaDB;DB_CLOSE_DELAY=-1"})
@ActiveProfiles("test")
class DemoWebIntegrationTest {
  @Autowired Categorias cats; @Autowired Ingredientes ingredients; @Autowired Recetas recipes;
  @Autowired Detalles details; @Autowired CargasIniciales cargas; @Autowired ObjectMapper json;
  @Test @Transactional
  void actualizaBaseAnteriorSinSobrescribirNiDuplicar() throws Exception {
    assertEquals(3, recipes.count());
    var previous=recipes.findAll().get(0); previous.descripcion="Editada por el usuario"; recipes.saveAndFlush(previous);
    var own=new Receta(); own.nombre="Ensalada fresca de garbanzos"; own.descripcion="Mi receta propia";
    own.instrucciones="Preparación propia que se debe conservar."; own.usuarioId=24L; own.categoria=cats.findAll().get(0);
    own.dificultad="FACIL"; own.porciones=2; own.tiempoPreparacion=10; recipes.saveAndFlush(own);
    var seed=new DemoWeb(cats,ingredients,recipes,details,cargas,json,true,true);
    seed.run();
    assertEquals(9, recipes.count()); // 3 antiguas + 1 propia + 5 nuevas, sin colisión de nombre.
    assertEquals("Editada por el usuario", recipes.findById(previous.id).orElseThrow().descripcion);
    assertEquals(24L, recipes.findById(own.id).orElseThrow().usuarioId);
    assertNull(recipes.findById(own.id).orElseThrow().imagenDemo);
    seed.run(); assertEquals(9, recipes.count());
    var demo=recipes.findAll().stream().filter(r -> r.imagenDemo!=null).findFirst().orElseThrow();
    details.deleteByRecetaId(demo.id); details.flush(); recipes.delete(demo); recipes.flush();
    seed.run(); assertEquals(8, recipes.count()); // No resucita recetas borradas.
  }
}
