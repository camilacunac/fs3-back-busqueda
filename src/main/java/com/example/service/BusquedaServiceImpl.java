package com.example.service;

import com.example.model.Compra;
import com.example.model.DetalleCompra;
import com.example.model.Producto;
import com.example.model.Response;
import com.example.repository.CompraRepository;
import com.example.repository.DetalleCompraRepository;
import com.example.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BusquedaServiceImpl implements BusquedaService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Override
    public ResponseEntity<Response> buscarComprasPorUsuario(Long idUsuario) {
        try {
            List<Compra> compras = compraRepository.findByIdUsuario(idUsuario);
            if (compras.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response("error", null, "No se encontraron compras para este usuario"));
            }
            return ResponseEntity.ok(new Response("success", compras, ""));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("error", null, e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Response> realizarCompra(Long idUsuario, List<DetalleCompra> detallesCompra) {
        try {
            if (detallesCompra == null || detallesCompra.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response("error", null, "No se han proporcionado detalles para la compra"));
            }

            double totalCompra = 0;
            Compra nuevaCompra = new Compra();
            nuevaCompra.setIdUsuario(idUsuario);
            nuevaCompra.setFechaCompra(LocalDate.now());
            nuevaCompra.setEstado("Pendiente");

            // Validar todos los productos antes de procesar
            for (DetalleCompra detalle : detallesCompra) {
                Producto producto = productoRepository.findById(detalle.getProducto().getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                if (producto.getStock() < detalle.getCantidad()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new Response("error", null,
                                    "Stock insuficiente para el producto: " + producto.getNombre()));
                }
            }

            // Procesar los detalles después de validar
            for (DetalleCompra detalle : detallesCompra) {
                Producto producto = productoRepository.findById(detalle.getProducto().getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                detalle.setCompra(nuevaCompra);
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.setSubtotal(detalle.getCantidad() * producto.getPrecio());
                totalCompra += detalle.getSubtotal();

                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoRepository.save(producto);
            }

            nuevaCompra.setTotal(totalCompra);
            compraRepository.save(nuevaCompra);
            detallesCompra.forEach(detalleCompraRepository::save);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new Response("success", nuevaCompra, "Compra realizada con éxito"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response("error", null, e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Response> buscarCompraPorId(Long idCompra) {
        try {
            Compra compra = compraRepository.findById(idCompra)
                    .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
            return ResponseEntity.ok(new Response("success", compra, ""));
        } catch (Exception e) {
            return ResponseEntity.ok(new Response("error", null, e.getMessage()));
        }
    }

    @Override
    public List<Compra> getAllCompras() {
        return compraRepository.findAll();

    }
}
