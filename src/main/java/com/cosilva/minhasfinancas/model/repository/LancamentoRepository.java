package com.cosilva.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cosilva.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{

}
