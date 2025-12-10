package com.ejemplo.resiliencedemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class MiControlador {

    @Autowired
    private ServicioLento servicioLento;

    @GetMapping("/llamar")
    public CompletableFuture<String> llamarServicio() {
        return servicioLento.operacionLenta();
    }

    @GetMapping("/health")
    public String health() {
        return "Servidor funcionando correctamente";
    }
}