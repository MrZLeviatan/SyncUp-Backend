package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.cancion.EditarCancionDto;
import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import co.edu.uniquindio.graph.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.repo.ArtistaRepo;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.service.CancionService;
import co.edu.uniquindio.service.utils.CloudinaryService;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.mpatric.mp3agic.Mp3File;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


@Service
@RequiredArgsConstructor
public class CancionServiceImpl implements CancionService {


    private final CancionRepo cancionRepo;
    private final CancionMapper cancionMapper;

    private final ArtistaRepo artistaRepo;

    private final CloudinaryService cloudinaryService;

    private final GrafoDeSimilitud grafoDeSimilitud;


    @Override
    public void agregarCancion(RegistrarCancionDto registrarCancionDto) throws ElementoNoEncontradoException, ElementoNoValidoException,
            IOException, InvalidDataException, UnsupportedTagException {

        // 1. Se busca el artista mediante su ID
        Artista artista = artistaRepo.findById(registrarCancionDto.artistaId())
                .orElseThrow(()-> new ElementoNoEncontradoException("Artista no  encontrado"));

        // 2, Se sube la imagen de la porta y se recibe su URL
        String urlImage = cloudinaryService.uploadImage(registrarCancionDto.imagenPortada());

        // 3. Se sube la canción y se recibe su URL
        MultipartFile archivoMp3 = registrarCancionDto.archivoCancion();
        String urlMp3 = cloudinaryService.uploadMp3(registrarCancionDto.archivoCancion());


        // 4. Calcular la duración de la canción desde mp3agic
            // Convertir MultipartFile a File temporal
            File mp3Temporal = File.createTempFile(archivoMp3.getOriginalFilename(), null);
            try (FileOutputStream fos = new FileOutputStream(mp3Temporal)) {
                fos.write(archivoMp3.getBytes());
            }

            // Se obtiene la duración del archivo MP3
            Mp3File mp3File = new Mp3File(mp3Temporal);
            long duracionSegundos = mp3File.getLengthInSeconds();
            String duracion = String.format("%02d:%02d", duracionSegundos / 60, duracionSegundos % 60);

        // Eliminar archivo temporal
        mp3Temporal.delete();

        // 5. Se mapea la canción mediante su DTO
        Cancion cancion = cancionMapper.toEntity(registrarCancionDto);

        // Se asignan los datos faltantes
        cancion.setUrlCancion(urlMp3);
        cancion.setUrlPortada(urlImage);
        cancion.setDuracion(duracion);
        cancion.setArtistaPrincipal(artista);
        cancionRepo.save(cancion);

        // Se agrega al grafo de similitud
        grafoDeSimilitud.agregarCancion(cancion);
    }



    @Override
    public void actualizarCancion(EditarCancionDto editarCancionDto) throws ElementoNoEncontradoException {

        // 1. Se busca la canción actualizar
        Cancion cancion = buscarCancionId(editarCancionDto.id());

        // 2. Se mapea la canción actualizar
        cancionMapper.updateUsuarioFromDto(editarCancionDto, cancion);

        // 3. Se guarda en la base de datos
        cancionRepo.save(cancion);
    }



    @Override
    public void eliminarCancion(Long idCancion) throws ElementoNoEncontradoException {

        // 1. Se busca la canción actualizar
        Cancion cancion = buscarCancionId(idCancion);

        // Remover de la lista del artista
        cancion.getArtistaPrincipal().getCanciones().remove(cancion);

        // 2. Se elimina la canción
        cancionRepo.delete(cancion);
    }


    private Cancion buscarCancionId(Long id) throws ElementoNoEncontradoException {
        return cancionRepo.findById(id).orElseThrow(()-> new ElementoNoEncontradoException("Canción no encontrada"));
    }


}
