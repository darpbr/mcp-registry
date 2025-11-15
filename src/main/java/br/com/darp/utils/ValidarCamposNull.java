package br.com.darp.utils;

public class ValidarCamposNull {

    public static boolean campoNull(String campo){
        if ((campo == null) || (campo.isEmpty())) {
            return true;
        }
        return false;
    }
}
