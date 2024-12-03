package com.example.repository;

import com.example.model.Compra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByIdUsuario(Long idUsuario);
}
