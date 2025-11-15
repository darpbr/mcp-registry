package br.com.darp.service;

import java.util.List;

import br.com.darp.domain.McpServer;
import br.com.darp.domain.McpServerRepository;
import br.com.darp.domain.Status;
import br.com.darp.dto.McpServerDTO;
import br.com.darp.dto.McpServerResponseDTO;
import br.com.darp.exception.DomainException;
import br.com.darp.utils.ValidarCamposNull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class McpService {

    @Inject
    McpServerRepository mcpServerRepository;

    public McpServer createMcpServer(McpServerDTO mcpServerDTO) {
        validarCampos(mcpServerDTO);
        McpServer mcpServer = new McpServer(
                mcpServerDTO.getNome(),
                mcpServerDTO.getDescricao(),
                mcpServerDTO.getVersao(),
                mcpServerDTO.getUrl(),
                mcpServerDTO.getResponsavel(),
                mcpServerDTO.getContato());
        if (mcpServerDTO.getMetadados() != null) {
            mcpServer.setMetadados(mcpServerDTO.getMetadados());
        }
        return mcpServerRepository.save(mcpServer);
    }

    private void validarCampos(McpServerDTO mcpServer) {
        if (mcpServer == null) {
            throw new DomainException("MCP-Server não pode ser null.");
        }
        boolean campoNull = false;
        campoNull = ValidarCamposNull.campoNull(mcpServer.getNome());
        campoNull = ValidarCamposNull.campoNull(mcpServer.getDescricao()) || campoNull;
        campoNull = ValidarCamposNull.campoNull(mcpServer.getVersao()) || campoNull;
        campoNull = ValidarCamposNull.campoNull(mcpServer.getUrl()) || campoNull;
        campoNull = ValidarCamposNull.campoNull(mcpServer.getResponsavel()) || campoNull;
        campoNull = ValidarCamposNull.campoNull(mcpServer.getContato()) || campoNull;
        if (campoNull) {
            throw new DomainException("MCP-Server possui campo null ou em branco!\n" + mcpServer.toString());
        }
    }

    public List<McpServer> getAllMcpServers() {
        return mcpServerRepository.listAll();
    }

    public McpServer getMcpServerById(String id) {
        return mcpServerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MCP Server not found with id: " + id));
    }

    public void deleteMcpServerById(String id) {
        mcpServerRepository.deleteById(id);
    }

    public McpServer updateMcpServer(String id, McpServer updatedMcpServer) {
        McpServer existingMcpServer = getMcpServerById(id);

        existingMcpServer.setNome(updatedMcpServer.getNome());
        existingMcpServer.setDescricao(updatedMcpServer.getDescricao());
        existingMcpServer.setVersao(updatedMcpServer.getVersao());
        existingMcpServer.setUrl(updatedMcpServer.getUrl());
        existingMcpServer.setStatus(updatedMcpServer.getStatus());
        existingMcpServer.setPublicadoEm(updatedMcpServer.getPublicadoEm());
        existingMcpServer.setAtualizadoEm(updatedMcpServer.getAtualizadoEm());
        existingMcpServer.setMetadados(updatedMcpServer.getMetadados());

        return mcpServerRepository.save(existingMcpServer);
    }

    public List<McpServerResponseDTO> getMcpsPorStatus(String status) {
        String statusFormatado = status.trim().toUpperCase();
        validarStatus(statusFormatado);
        List<McpServer> ativos = mcpServerRepository.listPorStatus(statusFormatado);
        if (ativos.isEmpty()) {
            return List.of();
        }
        List<McpServerResponseDTO> response = ativos.stream().map(mcp -> {
            McpServerResponseDTO dto = new McpServerResponseDTO();
            dto.setNome(mcp.getNome());
            dto.setVersao(mcp.getVersao());
            dto.setDescricao(mcp.getDescricao());
            dto.setUrl(mcp.getUrl());
            dto.setResponsavel(mcp.getResponsavel());
            dto.setContato(mcp.getContato());
            if (mcp.getMetadados() != null) {
                dto.setMetadados(mcp.getMetadados());
            }
            return dto;
        }).toList();
        return response;
    }

    private void validarStatus(String status) {
        boolean statusValido = false;
        for (Status s : Status.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                statusValido = true;
                break;
            }
        }
        if (!statusValido) {
            throw new DomainException("Status inválido: " + status);
        }
    }

    public void ativarMcpServer(String id) {
        McpServer mcp = mcpServerRepository.findById(id)
                .orElseThrow(() -> new DomainException("Não localizado MCP Server com este id:" + id));
        mcp.ativarServer();
        mcpServerRepository.ativarMcp(mcp);
    }

    public void alterarStaturMcpServer(String id, String status, String aprovador) {
        McpServer mcp = mcpServerRepository.findById(id)
                .orElseThrow(() -> new DomainException("Não localizado MCP Server com este id:" + id));
        validarStatus(status);
        mcp.alterarStatuServer(status, aprovador);
        mcpServerRepository.atualizarStatus(mcp);
    }

}
