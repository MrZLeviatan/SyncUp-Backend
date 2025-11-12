package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.cancion.EditarCancionDto;
import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;

public interface CancionService {


    void agregarCancion(RegistrarCancionDto registrarCancionDto)
            throws ElementoNoEncontradoException, ElementoNoValidoException, IOException,
            InvalidDataException, UnsupportedTagException;


    void actualizarCancion(EditarCancionDto editarCancionDto) throws ElementoNoEncontradoException;


    void eliminarCancion(Long idCancion) throws ElementoNoEncontradoException;


}
