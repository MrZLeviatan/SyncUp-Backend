package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.usuario.RegistrarUsuarioDto;
import co.edu.uniquindio.exception.ElementoRepetidoException;
import co.edu.uniquindio.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // Ubicado aquí si el registro es parte del flujo de autenticación
@RequiredArgsConstructor
public class AuthController {


    private final UsuarioService usuarioService;


    @PostMapping("/registro/usuario")
    public ResponseEntity<MensajeDto<String>> registrarUsuarios(@Valid @RequestBody RegistrarUsuarioDto registrarUsuarioDto)
            throws ElementoRepetidoException {

        usuarioService.registroUsuario(registrarUsuarioDto);

        return ResponseEntity.ok().body(new MensajeDto<>(false,"Registro logrado exitosamente."));
    }


}
