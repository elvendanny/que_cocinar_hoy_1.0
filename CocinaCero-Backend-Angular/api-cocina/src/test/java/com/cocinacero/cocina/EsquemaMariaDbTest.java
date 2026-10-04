package com.cocinacero.cocina;

import com.cocinacero.cocina.model.*;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Regresión del error 1074: revisa DDL real del dialecto MariaDB, sin servidor externo. */
class EsquemaMariaDbTest {
  @Test void instruccionesSeGeneraComoTextoLargoEnMariaDb() {
    StringWriter ddl = new StringWriter();
    Map<String, Object> config = new HashMap<>();
    config.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
    config.put("hibernate.boot.allow_jdbc_metadata_access", false);
    config.put("jakarta.persistence.schema-generation.database.action", "none");
    config.put("jakarta.persistence.schema-generation.scripts.action", "create");
    config.put("jakarta.persistence.schema-generation.scripts.create-target", ddl);
    var registry = new StandardServiceRegistryBuilder().applySettings(config).build();
    try {
      var metadata = new MetadataSources(registry)
          .addAnnotatedClass(Categoria.class).addAnnotatedClass(Ingrediente.class)
          .addAnnotatedClass(Receta.class).addAnnotatedClass(RecetaIngrediente.class)
          .addAnnotatedClass(Archivo.class).buildMetadata();
      SchemaManagementToolCoordinator.process(metadata, registry, config, action -> {});
      String sql = ddl.toString().toLowerCase();
      assertTrue(sql.contains("instrucciones mediumtext"), sql);
      assertFalse(sql.contains("varchar(20000)"), sql);
      assertTrue(sql.contains("references recetas"), sql);
    } finally {
      StandardServiceRegistryBuilder.destroy(registry);
    }
  }
}
