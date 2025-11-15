package co.edu.uniquindio.exception;

import co.edu.uniquindio.dto.MensajeDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class RestExceptionHandler {


    // Maneja ElementoNoEncontradoException (404)
    @ExceptionHandler(ElementoNoEncontradoException.class)
    public ResponseEntity<MensajeDto<String>> handleElementoNoEncontrado(ElementoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    // Maneja ElementoRepetidoException (409)
    @ExceptionHandler(ElementoRepetidoException.class)
    public ResponseEntity<MensajeDto<String>> handleElementoRepetido(ElementoRepetidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }



    // Maneja ElementoNoValido (422)
    @ExceptionHandler(ElementoNoValidoException.class)
    public ResponseEntity<MensajeDto<String>> handleElementoNoValido(ElementoNoValidoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }


    // Maneja excepciones no controladas (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeDto<String>> handleExcepcionGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeDto<>(true, "Error interno del servidor: " + ex.getMessage()));
    }

}
