package co.edu.uniquindio.service.utils;

import co.edu.uniquindio.graph.GrafoDeSimilitud;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.repo.CancionRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio clave responsable de la construcción y gestión del Grafo de Similitud entre {@link Cancion}.
 *
 * <p>Este servicio se inicializa automáticamente al inicio de la aplicación Spring y calcula los pesos
 * de las aristas del grafo basándose en métricas de similitud (género y artista principal).
 *
 * <p>Sus responsabilidades incluyen:
 * <ul>
 * <li>Obtener todas las canciones desde {@link CancionRepo}.</li>
 * <li>Crear una estructura de grafo {@link GrafoDeSimilitud} con las canciones como nodos.</li>
 * <li>Calcular y conectar las canciones con pesos (costos) que reflejan su afinidad.</li>
 * </ul>
 *
 * @see GrafoDeSimilitud
 * @see CancionRepo
 */
@Service
@RequiredArgsConstructor
public class SimilitudService {

    /**
     * Repositorio utilizado para acceder a los datos de las canciones en la base de datos.
     * Inyectado automáticamente por Lombok (@RequiredArgsConstructor).
     */
    private final CancionRepo cancionRepo;

    /**
     * Instancia del grafo de similitud que almacena todas las canciones y sus conexiones ponderadas.
     * Es la estructura de datos central para la lógica de recomendación.
     */
    private final GrafoDeSimilitud grafo;

    /**
     * Método de inicialización que se ejecuta inmediatamente después de que el Bean sea construido por Spring.
     * <p>La anotación {@code @PostConstruct} asegura que el grafo se construya con todas las canciones
     * tan pronto como la aplicación esté lista para servir.
     */
    @PostConstruct
    public void init() {
        inicializarGrafo();
    }


    /**
     * Construye el grafo de similitud cargando todas las canciones de la base de datos y conectándolas.
     *
     * <p>El proceso sigue dos pasos:
     * <ol>
     * <li>Agrega todas las canciones como nodos al grafo.</li>
     * <li>Utiliza dos bucles anidados para iterar sobre todos los pares posibles de canciones y calcular
     * y establecer el peso de similitud entre ellas.</li>
     * </ol>
     */
    public void inicializarGrafo() {

        List<Cancion> canciones = cancionRepo.findAll();

        /* 1. Agregar nodos:
         *  - Agrega cada canción como nodo dentro del grafo (sin conexiones todavía).
         *  - Se usa referencia de método (::) para simplificar la llamada.
         */
        canciones.forEach(grafo::agregarCancion);

        // 2. Conectar nodos con pesos:
        for (Cancion c1 : canciones) {
            for (Cancion c2 : canciones) {
                // Se conecta cada par de canciones, siempre que no sea la misma.
                if (!c1.equals(c2)) {
                    // Mientras más bajo sea el peso, mayor es la similitud
                    double peso = calcularPesoSimilitud(c1, c2);
                    // Registra la arista en el grafo (bidireccional, ya que es no dirigido).
                    grafo.conectarCanciones(c1, c2, peso);
                }
            }
        }
    }

    /**
     * Calcula el peso o 'costo' entre dos canciones, donde un costo más bajo implica mayor similitud.
     *
     * <p>El cálculo de la similitud se basa en la coincidencia de los siguientes atributos:
     * <ul>
     * <li>Género Musical (0.6 puntos): Si el género es el mismo, la similitud aumenta significativamente.</li>
     * <li>Artista Principal (0.4 puntos): Si el artista es el mismo, la similitud aumenta,
     * llevando el máximo de similitud a 1.0.</li>
     * </ul>
     *
     * <p>El resultado final es el costo, calculado como `1 - similitud` para adaptarse al algoritmo de
     * Dijkstra, donde el menor valor es el preferido.
     *
     * @param c1 La primera canción para la comparación.
     * @param c2 La segunda canción para la comparación.
     * @return El costo de la arista (peso) entre 0.0 (similitud perfecta) y 1.0 (sin similitud).
     */
    private double calcularPesoSimilitud(Cancion c1, Cancion c2) {

        double similitud = 0.0;

        // Ponderación de similitud:
        if (c1.getGeneroMusical().equals(c2.getGeneroMusical())) similitud += 0.6;
        if (c1.getArtistaPrincipal().equals(c2.getArtistaPrincipal())) similitud += 0.4;

        /*
         * Transformación Similitud (0..1) a Costo (1..0)
         * El costo es el valor a minimizar por Dijkstra.
         */
        return 1 - similitud;
    }

    /**
     * Proporciona acceso al grafo de similitud construido.
     *
     * <p>Permite que otros servicios de la capa de negocio utilicen el grafo precalculo para
     * ejecutar algoritmos como Dijkstra y generar recomendaciones.
     *
     * @return La instancia del {@link GrafoDeSimilitud} que contiene todas las canciones conectadas.
     */
    public GrafoDeSimilitud obtenerGrafo() {
        return grafo;
    }
}