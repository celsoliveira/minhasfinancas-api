package com.cosilva.minhasfinancas.api.resource;

import java.util.Optional;

import javax.websocket.server.PathParam;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosilva.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.cosilva.minhasfinancas.api.dto.LancamentoDTO;
import com.cosilva.minhasfinancas.exception.RegraNegocioException;
import com.cosilva.minhasfinancas.model.entity.Lancamento;
import com.cosilva.minhasfinancas.model.entity.Usuario;
import com.cosilva.minhasfinancas.model.enums.StatusLancamento;
import com.cosilva.minhasfinancas.model.enums.TipoLancamento;
import com.cosilva.minhasfinancas.service.LancamentoService;
import com.cosilva.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoResource {
	
	private final LancamentoService service;
	private final UsuarioService usuarioService;
	
	/*
	public LancamentoResource(LancamentoService service, UsuarioService usuarioService) {
		this.service = service;
		this.usuarioService = usuarioService;
	}
	*/
	
	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO dto) {
		try {
			Lancamento entidade = converter(dto);
			entidade = service.salvar(entidade);
			return new ResponseEntity(entidade, HttpStatus.CREATED);
			//return ResponseEntity.ok(entidade);
		}catch(RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}	
	
	
	@PutMapping("{id}")
	public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
		
		return service.obterPorId(id).map(entidade -> {
			try {
			Lancamento lancamento = converter(dto);
			lancamento.setId(entidade.getId());
			
			service.atualizar(lancamento);
			return ResponseEntity.ok(lancamento);
			}catch(RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
			
		}).orElseGet(() -> 
			new ResponseEntity("Lançamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));
	}
	
	@GetMapping
	public ResponseEntity buscar(
			//@RequestParam java.util.Map<String, String> params   (alternativa)
			@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "ano", required = false) Integer ano,
			@RequestParam("usuario") Long idUsuario
			) {
		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
		
		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
		if (usuario.isEmpty()){
			return ResponseEntity.badRequest().body("Não foi possível realizar a consulta. Usuário não encontrado para o Id informado.");
		} else {
			lancamentoFiltro.setUsuario(usuario.get());
		}
		List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
		return ResponseEntity.ok(lancamentos);
	}
	
	
	@DeleteMapping("{id}")
	public ResponseEntity deletar(@PathVariable("id") Long id) {
		return service.obterPorId(id).map(entidade -> {
			service.deletar(entidade);
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}).orElseGet(() -> 
		new ResponseEntity("Lançamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));
	}
	
	
	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto) {
		
		return service.obterPorId(id).map(entidade -> {
			StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());
			
			if (statusSelecionado == null) {
				ResponseEntity.badRequest().body("Não foi possível atualizar o status do lançamento, envie um status válido.");
			}
			
			try {
				entidade.setStatus(statusSelecionado);
				service.atualizar(entidade);
				return ResponseEntity.ok(entidade);
				
			}catch(RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> 
		new ResponseEntity("Lançamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));
	}
	
	private Lancamento converter(LancamentoDTO dto) {
		
	Lancamento lancamento = new Lancamento();
		
		Usuario usuario = usuarioService
				.obterPorId(dto.getUsuario())
				.orElseThrow( () -> new RegraNegocioException("Usuário não encontrado para o Id informado."));
				
				
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		lancamento.setUsuario(usuario);
		
		if (dto.getTipo() != null) {
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
		}
		
		if (dto.getStatus() != null) {
			lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
		}
		return lancamento;	
	}
	
	
	
}
