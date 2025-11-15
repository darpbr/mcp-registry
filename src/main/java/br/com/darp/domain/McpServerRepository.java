package br.com.darp.domain;

import java.util.List;
import java.util.Optional;

public interface McpServerRepository {
    McpServer save(McpServer mcpServer);
    List<McpServer> listAll();
    Optional<McpServer> findById(String id);
    void deleteById(String id);
    List<McpServer> listAtivos();
    void ativarMcp(McpServer mcp);
    List<McpServer> listPorStatus(String status);
    void atualizarStatus(McpServer mcp);
}
