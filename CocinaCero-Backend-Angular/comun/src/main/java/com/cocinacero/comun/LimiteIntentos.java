package com.cocinacero.comun;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite local por IP, sin confiar en cabeceras de proxy. Para varios nodos usar un almacén
 * compartido.
 */
public class LimiteIntentos {
  private record Ventana(long inicio, int n) {}

  private final ConcurrentHashMap<String, Ventana> datos = new ConcurrentHashMap<>();

  public void comprobar(String ip) {
    long now = System.currentTimeMillis();
    if (datos.size() > 10000) datos.entrySet().removeIf(e -> now - e.getValue().inicio() > 900000);
    Ventana v =
        datos.compute(
            ip,
            (k, old) ->
                old == null || now - old.inicio() > 900000
                    ? new Ventana(now, 1)
                    : new Ventana(old.inicio(), old.n() + 1));
    if (v.n() > 60) throw new ApiError(429, "Demasiados intentos. Espera 15 minutos.");
  }
}
