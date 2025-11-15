package br.com.darp.domain;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import org.bson.types.ObjectId;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "mcp_servers")
public class McpServer {

    public ObjectId id;
    private String nome;
    private String descricao;
    private String versao;
    private String url;
    private String responsavel;
    private String contato;
    private String status;
    private String aprovador;
    private Date publicadoEm;
    private Date atualizadoEm;
    private boolean heartbeat1;
    private boolean heartbeat2;
    private HashMap<String, String> metadados;

    public McpServer() {
    }

    public McpServer(String nome, String descricao, String versao, String url, String responsavel, String contato) {
        this.nome = nome;
        this.descricao = descricao;
        this.versao = versao;
        this.url = url;
        this.responsavel = responsavel;
        this.contato = contato;
        this.status = Status.PENDENTE_ATIVACAO.name();
        this.heartbeat1 = false;
        this.heartbeat2 = false;
        this.publicadoEm = Date.from(Instant.now());
    }

    public McpServer(String nome, String descricao, String versao, String url, HashMap<String, String> metadados) {
        this.nome = nome;
        this.descricao = descricao;
        this.versao = versao;
        this.url = url;
        this.status = Status.PENDENTE_ATIVACAO.name();
        this.heartbeat1 = false;
        this.heartbeat2 = false;
        this.publicadoEm = Date.from(Instant.now());
        this.metadados = metadados;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getAprovador() {
        return aprovador;
    }
    public void setAprovador(String aprovador) {
        this.aprovador = aprovador;
    }
    public Date getPublicadoEm() {
        return publicadoEm;
    }

    public void setPublicadoEm(Date publicadoEm) {
        this.publicadoEm = publicadoEm;
    }

    public Date getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Date atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public boolean isHeartbeat1() {
        return heartbeat1;
    }

    public void setHeartbeat1(boolean heartbeat1) {
        this.heartbeat1 = heartbeat1;
    }
    public boolean isHeartbeat2() {
        return heartbeat2;
    }
    public void setHeartbeat2(boolean heartbeat2) {
        this.heartbeat2 = heartbeat2;
    }

    public HashMap<String, String> getMetadados() {
        return metadados;
    }

    public void setMetadados(HashMap<String, String> metadados) {
        this.metadados = metadados;
    }

    public void informarHeartbeat(int heartbeatNumber) {
        if (heartbeatNumber == 1) {
            this.heartbeat1 = true;
            this.heartbeat2 = false;
        } else if (heartbeatNumber == 2) {
            this.heartbeat2 = true;
            this.heartbeat1 = false;
        } else {
            throw new IllegalArgumentException("Número de heartbeat inválido: " + heartbeatNumber);
        }
    }

    public void alterarStatuServer(String status, String aprovador) {
        this.status = status;
        this.atualizadoEm = Date.from(Instant.now());
        this.aprovador = aprovador;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((nome == null) ? 0 : nome.hashCode());
        result = prime * result + ((descricao == null) ? 0 : descricao.hashCode());
        result = prime * result + ((versao == null) ? 0 : versao.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((publicadoEm == null) ? 0 : publicadoEm.hashCode());
        result = prime * result + ((atualizadoEm == null) ? 0 : atualizadoEm.hashCode());
        result = prime * result + (heartbeat1 ? 1231 : 1237);
        result = prime * result + ((metadados == null) ? 0 : metadados.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        McpServer other = (McpServer) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (nome == null) {
            if (other.nome != null)
                return false;
        } else if (!nome.equals(other.nome))
            return false;
        if (descricao == null) {
            if (other.descricao != null)
                return false;
        } else if (!descricao.equals(other.descricao))
            return false;
        if (versao == null) {
            if (other.versao != null)
                return false;
        } else if (!versao.equals(other.versao))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (publicadoEm == null) {
            if (other.publicadoEm != null)
                return false;
        } else if (!publicadoEm.equals(other.publicadoEm))
            return false;
        if (atualizadoEm == null) {
            if (other.atualizadoEm != null)
                return false;
        } else if (!atualizadoEm.equals(other.atualizadoEm))
            return false;
        if (heartbeat1 != other.heartbeat1)
            return false;
        if (metadados == null) {
            if (other.metadados != null)
                return false;
        } else if (!metadados.equals(other.metadados))
            return false;
        return true;
    }

    public void ativarServer() {
        this.status = Status.ATIVO.name();
        this.atualizadoEm = Date.from(Instant.now());
    }

}
