package br.com.darp.dto;

import java.util.HashMap;

public class McpServerDTO {

    private String nome;
    private String descricao;
    private String versao;
    private String url;
    private String responsavel;
    private String contato;
    private HashMap<String, String> metadados;

    public McpServerDTO() {
    }

    public McpServerDTO(String nome, String descricao, String versao, String url, String responsavel, String contato) {
        this.nome = nome;
        this.descricao = descricao;
        this.versao = versao;
        this.url = url;
        this.responsavel = responsavel;
        this.contato = contato;
    }

    public McpServerDTO(String nome, String descricao, String versao, String url, String responsavel, String contato, HashMap<String, String> metadados) {
        this.nome = nome;
        this.descricao = descricao;
        this.versao = versao;
        this.url = url;
        this.responsavel = responsavel;
        this.contato = contato;
        this.metadados = metadados;
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

    public HashMap<String, String> getMetadados() {
        return metadados;
    }

    public void setMetadados(HashMap<String, String> metadados) {
        this.metadados = metadados;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nome == null) ? 0 : nome.hashCode());
        result = prime * result + ((descricao == null) ? 0 : descricao.hashCode());
        result = prime * result + ((versao == null) ? 0 : versao.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((responsavel == null) ? 0 : responsavel.hashCode());
        result = prime * result + ((contato == null) ? 0 : contato.hashCode());
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
        McpServerDTO other = (McpServerDTO) obj;
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
        if (responsavel == null) {
            if (other.responsavel != null)
                return false;
        } else if (!responsavel.equals(other.responsavel))
            return false;
        if (contato == null) {
            if (other.contato != null)
                return false;
        } else if (!contato.equals(other.contato))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "McpServerDTO [\nnome=" + nome + ",\ndescricao=" + descricao + ",\nversao=" + versao + ",\nurl=" + url
                + ",\nresponsavel=" + responsavel + ",\ncontato=" + contato + "\n]";
    }
    
}
