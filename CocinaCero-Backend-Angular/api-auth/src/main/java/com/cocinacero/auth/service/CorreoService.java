package com.cocinacero.auth.service;

public interface CorreoService {
  void enviar(String destinatario, String codigo);
}
