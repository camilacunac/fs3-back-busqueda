package com.example.service;

import com.example.model.Compra;
import com.example.model.DetalleCompra;
import com.example.model.Producto;
import com.example.model.Response;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface BusquedaService {

    ResponseEntity<Response> realizarCompra(Long idUsuario, List<DetalleCompra> detallesCompra);

    ResponseEntity<Response> buscarCompraPorId(Long idCompra);

    List<Compra> getAllCompras();

    ResponseEntity<Response> buscarComprasPorUsuario(Long idUsuario);

}
