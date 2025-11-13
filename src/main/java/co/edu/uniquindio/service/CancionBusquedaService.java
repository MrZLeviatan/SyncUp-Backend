package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.cancion.CancionDto;

import java.util.List;

public interface CancionBusquedaService {


    /**
     * Autocompletado de títulos usando Trie
     * @param prefijo prefijo de la canción
     * @return lista de títulos que coinciden
     */
    List<CancionDto> autocompletarTitulos(String prefijo);



}
