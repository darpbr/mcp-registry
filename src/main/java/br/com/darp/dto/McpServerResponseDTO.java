package br.com.darp.dto;

import java.util.HashMap;

public class McpServerResponseDTO {

    private String nome;
    private String versao;
    private String descricao;
    private String url;
    private String responsavel;
    private String contato;
    private HashMap<String, String> metadados;
    
    public McpServerResponseDTO() {
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getVersao() {
        return versao;
    }
    public void setVersao(String versao) {
        this.versao = versao;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
    public String toString() {
        return "McpServerResponseDTO [nome=" + nome + ", versao=" + versao + ", descricao=" + descricao + ", url=" + url
                + ", responsavel=" + responsavel + ", contato=" + contato + ", metadados=" + metadados + "]";
    }

}
