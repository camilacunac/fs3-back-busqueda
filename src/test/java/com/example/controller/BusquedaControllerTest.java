package com.example.controller;

import com.example.model.Compra;
import com.example.model.DetalleCompra;
import com.example.model.Response;
import com.example.service.BusquedaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BusquedaControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private BusquedaController busquedaController;

    @Mock
    private BusquedaService busquedaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(busquedaController).build();
    }

    @Test
    void testRealizarCompra_Success() throws Exception {
        Response response = new Response("success", null, "Compra realizada con éxito");

        when(busquedaService.realizarCompra(eq(1L), anyList()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/busqueda/compra")
                .param("idUsuario", "1")
                .contentType("application/json")
                .content("""
                        [
                            {"producto": {"idProducto": 1}, "cantidad": 2}
                        ]
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Compra realizada con éxito"));

        verify(busquedaService, times(1)).realizarCompra(eq(1L), anyList());
    }

    @Test
    void testBuscarCompraPorId_Success() throws Exception {
        Response response = new Response("success", new Compra(), "");
        when(busquedaService.buscarCompraPorId(1L)).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(get("/busqueda/compra/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("success"));

        verify(busquedaService, times(1)).buscarCompraPorId(1L);
    }

    @Test
    void testGetAllCompras_Success() throws Exception {
        List<Compra> compras = Arrays.asList(new Compra(), new Compra());
        when(busquedaService.getAllCompras()).thenReturn(compras);

        mockMvc.perform(get("/busqueda/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(busquedaService, times(1)).getAllCompras();
    }

    @Test
    void testRealizarCompra_EmptyDetalles() throws Exception {
        mockMvc.perform(post("/busqueda/compra")
                .param("idUsuario", "1")
                .contentType("application/json")
                .content("[]")) // Lista vacía
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se han proporcionado detalles para la compra"));

    }

    @Test
    void testGetAllCompras_NoRecords() throws Exception {
        when(busquedaService.getAllCompras()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/busqueda/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(busquedaService, times(1)).getAllCompras();
    }

    @Test
    void testRealizarCompra_StockInsuficiente() throws Exception {
        Response response = new Response("error", null, "Stock insuficiente para el producto: Producto Test");
        when(busquedaService.realizarCompra(eq(1L), anyList()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/busqueda/compra")
                .param("idUsuario", "1")
                .contentType("application/json")
                .content("""
                        [
                            {"producto": {"idProducto": 1}, "cantidad": 5}
                        ]
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Stock insuficiente para el producto: Producto Test"));

        verify(busquedaService, times(1)).realizarCompra(eq(1L), anyList());
    }

    @Test
    void testRealizarCompra_NullDetalles() throws Exception {
        mockMvc.perform(post("/busqueda/compra")
                .param("idUsuario", "1")
                .contentType("application/json")
                .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se han proporcionado detalles para la compra"));

        verify(busquedaService, never()).realizarCompra(eq(1L), anyList());
    }

    @Test
    void testRealizarCompra_UnexpectedError() throws Exception {
        Response response = new Response("error", null, "Unexpected error");
        when(busquedaService.realizarCompra(eq(1L), anyList()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/busqueda/compra")
                .param("idUsuario", "1")
                .contentType("application/json")
                .content("""
                        [
                            {"producto": {"idProducto": 1}, "cantidad": 2}
                        ]
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unexpected error"));

        verify(busquedaService, times(1)).realizarCompra(eq(1L), anyList());
    }

    @Test
    void testBuscarCompraPorId_UnexpectedError() throws Exception {
        Response response = new Response("error", null, "Unexpected error");
        when(busquedaService.buscarCompraPorId(1L)).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(get("/busqueda/compra/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unexpected error"));

        verify(busquedaService, times(1)).buscarCompraPorId(1L);
    }

    @Test
    void testBuscarCompraPorId_Error() throws Exception {
        Response response = new Response("error", null, "Error al buscar compra");
        when(busquedaService.buscarCompraPorId(eq(1L)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));

        mockMvc.perform(get("/busqueda/compra/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error al buscar compra"));

        verify(busquedaService, times(1)).buscarCompraPorId(1L);
    }

    @Test
    void testGetAllCompras_UnexpectedError() throws Exception {
        when(busquedaService.getAllCompras()).thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(get("/busqueda/compras"))
                .andExpect(status().isInternalServerError());

        verify(busquedaService, times(1)).getAllCompras();
    }

}
