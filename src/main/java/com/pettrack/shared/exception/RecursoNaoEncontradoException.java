package com.pettrack.shared.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem){
        super(mensagem);
    }
}