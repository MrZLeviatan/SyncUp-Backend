package co.edu.uniquindio.dto;

/**
 Este record tiene dos campos: 'error', un booleano que indica si la respuesta es
 un error, y 'mensaje', un campo genérico que puede contener cualquier tipo de dato,
 como un String o un objeto.
 **/
public record MensajeDto<T>(boolean error, T mensaje) {

}
