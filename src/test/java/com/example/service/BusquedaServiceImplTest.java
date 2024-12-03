package com.example.service;

import com.example.App;
import com.example.config.GlobalExceptionHandler;
import com.example.model.Compra;
import com.example.model.DetalleCompra;
import com.example.model.Producto;
import com.example.model.Response;
import com.example.repository.CompraRepository;
import com.example.repository.DetalleCompraRepository;
import com.example.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class BusquedaServiceImplTest {

    @InjectMocks
    private BusquedaServiceImpl busquedaService;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private DetalleCompraRepository detalleCompraRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRealizarCompra_Success() {
        // Arrange
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(100.0);
        producto.setStock(10);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        Compra compra = new Compra();
        compra.setIdCompra(1L);
        compra.setIdUsuario(1L);
        compra.setDetalles(Collections.singletonList(detalle));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(compraRepository.save(any(Compra.class))).thenReturn(compra);

        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, List.of(detalle));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Compra realizada con éxito", response.getBody().getError());
        verify(productoRepository, times(1)).save(producto);
        verify(detalleCompraRepository, times(1)).save(any(DetalleCompra.class));
    }

    @Test
    void testRealizarCompra_StockInsuficiente() {
        // Arrange
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Producto Test");
        producto.setStock(1);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setProducto(producto);
        detalle.setCantidad(5);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, List.of(detalle));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Stock insuficiente para el producto: Producto Test", response.getBody().getError());
        verify(productoRepository, never()).save(any(Producto.class));
        verify(detalleCompraRepository, never()).save(any(DetalleCompra.class));
    }

    @Test
    void testBuscarCompraPorId_Success() {
        // Arrange
        Compra compra = new Compra();
        compra.setIdCompra(1L);
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // Act
        ResponseEntity<Response> response = busquedaService.buscarCompraPorId(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getState());
        assertEquals(compra, response.getBody().getRes());
    }

    @Test
    void testBuscarCompraPorId_NotFound() {
        // Arrange
        when(compraRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            busquedaService.buscarCompraPorId(1L);
        } catch (RuntimeException e) {
            assertEquals("Compra no encontrada", e.getMessage());
        }
    }

    @Test
    void testGetAllCompras_Success() {
        // Arrange
        List<Compra> compras = Arrays.asList(new Compra(), new Compra());
        when(compraRepository.findAll()).thenReturn(compras);

        // Act
        List<Compra> result = busquedaService.getAllCompras();

        // Assert
        assertEquals(2, result.size());
        verify(compraRepository, times(1)).findAll();
    }

    @Test
    void testRealizarCompra_ProductoNoEncontrado() {
        // Arrange
        DetalleCompra detalle = new DetalleCompra();
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, List.of(detalle));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Producto no encontrado", response.getBody().getError());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllCompras_NoRecords() {
        // Arrange
        when(compraRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Compra> compras = busquedaService.getAllCompras();

        // Assert
        assertNotNull(compras);
        assertTrue(compras.isEmpty());
        verify(compraRepository, times(1)).findAll();
    }

    @Test
    void testRealizarCompra_MultipleProductos() {
        // Arrange
        Producto producto1 = new Producto();
        producto1.setIdProducto(1L);
        producto1.setNombre("Producto 1");
        producto1.setPrecio(100.0);
        producto1.setStock(10);

        Producto producto2 = new Producto();
        producto2.setIdProducto(2L);
        producto2.setNombre("Producto 2");
        producto2.setPrecio(50.0);
        producto2.setStock(5);

        DetalleCompra detalle1 = new DetalleCompra();
        detalle1.setProducto(producto1);
        detalle1.setCantidad(3);

        DetalleCompra detalle2 = new DetalleCompra();
        detalle2.setProducto(producto2);
        detalle2.setCantidad(2);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, List.of(detalle1, detalle2));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Compra realizada con éxito", response.getBody().getError());
        assertEquals(7, producto1.getStock());
        assertEquals(3, producto2.getStock());
    }

    @Test
    void testRealizarCompra_MultipleProductos_ErrorEnUno() {
        // Arrange
        Producto producto1 = new Producto();
        producto1.setIdProducto(1L);
        producto1.setNombre("Producto 1");
        producto1.setPrecio(100.0);
        producto1.setStock(10);

        Producto producto2 = new Producto();
        producto2.setIdProducto(2L);
        producto2.setNombre("Producto 2");
        producto2.setPrecio(50.0);
        producto2.setStock(2);

        DetalleCompra detalle1 = new DetalleCompra();
        detalle1.setProducto(producto1);
        detalle1.setCantidad(3);

        DetalleCompra detalle2 = new DetalleCompra();
        detalle2.setProducto(producto2);
        detalle2.setCantidad(6); // Error: stock insuficiente

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));

        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, List.of(detalle1, detalle2));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Stock insuficiente para el producto: Producto 2", response.getBody().getError());
        verify(productoRepository, never()).save(any()); // No debería guardar ningún producto
        verify(compraRepository, never()).save(any()); // La compra no debe guardarse
    }

    @Test
    void testBuscarCompraPorId_UnexpectedError() {
        // Arrange
        when(compraRepository.findById(1L)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<Response> response = busquedaService.buscarCompraPorId(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getError());
        verify(compraRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllCompras_UnexpectedError() {
        // Arrange
        when(compraRepository.findAll()).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        RuntimeException exception = null;
        try {
            busquedaService.getAllCompras();
        } catch (RuntimeException e) {
            exception = e;
        }

        // Assert
        assertNotNull(exception);
        assertEquals("Unexpected error", exception.getMessage());
        verify(compraRepository, times(1)).findAll();
    }

    @Test
    void testRealizarCompra_NullDetalles() {
        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No se han proporcionado detalles para la compra", response.getBody().getError());
    }

    @Test
    void testRealizarCompra_UnexpectedError() {
        // Arrange
        DetalleCompra detalle = new DetalleCompra();
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        when(productoRepository.findById(1L)).thenThrow(new RuntimeException("Error inesperado"));

        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, List.of(detalle));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error inesperado", response.getBody().getError());
    }

    @Test
    void testRealizarCompra_EmptyDetalles() {
        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, Collections.emptyList());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No se han proporcionado detalles para la compra", response.getBody().getError());
        verify(productoRepository, never()).findById(anyLong());
    }

    @Test
    void testRealizarCompra_DetallesVacios() {
        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, Collections.emptyList());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No se han proporcionado detalles para la compra", response.getBody().getError());
    }

    @Test
    void testRealizarCompra_ErrorGuardandoCompra() {
        // Arrange
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setStock(10);
        producto.setPrecio(100.0);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doThrow(new RuntimeException("Error al guardar compra")).when(compraRepository).save(any());

        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, List.of(detalle));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error al guardar compra", response.getBody().getError());
        verify(compraRepository, times(1)).save(any(Compra.class));
    }

    @Test
    void testRealizarCompra_ErrorGuardandoDetalle() {
        // Arrange
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setStock(10);
        producto.setPrecio(100.0);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doThrow(new RuntimeException("Error al guardar detalle")).when(detalleCompraRepository).save(any());

        // Act
        ResponseEntity<Response> response = busquedaService.realizarCompra(1L, List.of(detalle));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error al guardar detalle", response.getBody().getError());
        verify(detalleCompraRepository, times(1)).save(any(DetalleCompra.class));
    }

    @Test
    void testProductoGettersAndSetters() {
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Test Producto");
        producto.setPrecio(100.0);
        producto.setStock(10);
        producto.setCategoria("Electronics");

        assertEquals(1L, producto.getIdProducto());
        assertEquals("Test Producto", producto.getNombre());
        assertEquals(100.0, producto.getPrecio());
        assertEquals(10, producto.getStock());
        assertEquals("Electronics", producto.getCategoria());
    }

    @Test
    void testDetalleCompraGettersAndSetters() {
        DetalleCompra detalle = new DetalleCompra();
        Producto producto = new Producto();
        detalle.setProducto(producto);
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(100.0);
        detalle.setSubtotal(500.0);

        assertEquals(producto, detalle.getProducto());
        assertEquals(5, detalle.getCantidad());
        assertEquals(100.0, detalle.getPrecioUnitario());
        assertEquals(500.0, detalle.getSubtotal());
    }

    @Test
    void testResponseConstructorAndGetters() {
        Response response = new Response("success", null, "Operation completed");
        assertEquals("success", response.getState());
        assertNull(response.getRes());
        assertEquals("Operation completed", response.getError());
    }

    @Test
    void testResponseGettersAndSetters() {
        Response response = new Response("success", "data", "message");

        assertEquals("success", response.getState());
        assertEquals("data", response.getRes());
        assertEquals("message", response.getError());

        response.setState("error");
        response.setRes(null);
        response.setError("Error message");

        assertEquals("error", response.getState());
        assertNull(response.getRes());
        assertEquals("Error message", response.getError());
    }

    @Test
    void testResponseConstructors() {
        Response response = new Response("success", "data", "no error");
        assertEquals("success", response.getState());
        assertEquals("data", response.getRes());
        assertEquals("no error", response.getError());

        // Validando el constructor vacío (si existe)
        Response emptyResponse = new Response();
        assertNull(emptyResponse.getState());
        assertNull(emptyResponse.getRes());
        assertNull(emptyResponse.getError());
    }

    @Test
    void testHandleRuntimeException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException ex = new RuntimeException("Error inesperado");
        ResponseEntity<Response> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error inesperado", response.getBody().getError());
    }

    @Test
    void testProductoNotEquals() {
        Producto producto1 = new Producto();
        producto1.setIdProducto(1L);
        producto1.setNombre("Producto 1");

        Producto producto2 = new Producto();
        producto2.setIdProducto(2L);
        producto2.setNombre("Producto 2");

        assertNotEquals(producto1, producto2);
    }

    @Test
    void testProductoConstructor() {
        // Arrange
        String nombre = "Test Producto";
        String descripcion = "Descripción del producto";
        Double precio = 100.0;
        Integer stock = 10;
        String categoria = "Electronics";
        LocalDate fechaCreacion = LocalDate.of(2023, 11, 27);
        String estado = "Disponible";
        String imagenUrl = "http://example.com/image.jpg";

        // Act
        Producto producto = new Producto(nombre, descripcion, precio, stock, categoria, fechaCreacion, estado,
                imagenUrl);

        // Assert
        assertEquals(nombre, producto.getNombre());
        assertEquals(descripcion, producto.getDescripcion());
        assertEquals(precio, producto.getPrecio());
        assertEquals(stock, producto.getStock());
        assertEquals(categoria, producto.getCategoria());
        assertEquals(fechaCreacion, producto.getFechaCreacion());
        assertEquals(estado, producto.getEstado());
        assertEquals(imagenUrl, producto.getImagenUrl());
    }

    @Test
    void testMainMethod() {
        String[] args = {};
        App.main(args); // Solo verificar que el contexto de Spring Boot se inicia correctamente
    }

}
