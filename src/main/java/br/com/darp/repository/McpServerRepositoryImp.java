package br.com.darp.repository;

import br.com.darp.domain.McpServer;
import br.com.darp.domain.McpServerRepository;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

@ApplicationScoped
public class McpServerRepositoryImp implements McpServerRepository, PanacheMongoRepository<McpServer> {

	@Override
	public Optional<McpServer> findById(String id) {
		return find("_id", new ObjectId(id)).firstResultOptional();
	}

	@Override
	public void deleteById(String id) {
        delete("_id", new ObjectId(id));
	}

	@Override
	public McpServer save(McpServer server) {
		persistOrUpdate(server);
		return server;
	}

	@Override
	public List<McpServer> listAll() {
		return findAll().list();
	}

	@Override
	public List<McpServer> listAtivos() {
		return find("status", "ATIVO").list();
	}

	@Override
	public void ativarMcp(McpServer mcp) {
		update(mcp);
	}

	@Override
	public List<McpServer> listPorStatus(String status) {
		return find("status", status.toUpperCase()).list();
	}

	@Override
	public void atualizarStatus(McpServer mcp) {
		update(mcp);
	}
}
