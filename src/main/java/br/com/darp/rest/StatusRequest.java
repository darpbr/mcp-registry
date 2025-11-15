package br.com.darp.rest;

public class StatusRequest {
    private String status;
    private String aprovador;

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
}
