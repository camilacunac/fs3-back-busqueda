package com.example.controller;

import com.example.model.Compra;
import com.example.model.DetalleCompra;
import com.example.model.Response;
import com.example.service.BusquedaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/busqueda")
public class BusquedaController {

    @Autowired
    private BusquedaService busquedaService;

    @PostMapping("/compra")
    public ResponseEntity<Response> realizarCompra(
            @RequestParam Long idUsuario,
            @RequestBody(required = false) List<DetalleCompra> detallesCompra) {
        if (detallesCompra == null || detallesCompra.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new Response("error", null, "No se han proporcionado detalles para la compra"));
        }
        return busquedaService.realizarCompra(idUsuario, detallesCompra);
    }

    @GetMapping("/compra/{id}")
    public ResponseEntity<Response> buscarCompraPorId(@PathVariable Long id) {
        return busquedaService.buscarCompraPorId(id);
    }

    @GetMapping("/compras")
    public ResponseEntity<?> getAllCompras() {
        try {
            List<Compra> compras = busquedaService.getAllCompras();
            return ResponseEntity.ok(compras);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("error", null, "Error inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/compras/usuario/{idUsuario}")
    public ResponseEntity<Response> obtenerComprasPorUsuario(@PathVariable Long idUsuario) {
        return busquedaService.buscarComprasPorUsuario(idUsuario);
    }

}
