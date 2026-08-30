package com.curso.expecializacion.user.infraestructure.api;
import com.curso.expecializacion.product.common.mediator.Mediator;
import com.curso.expecializacion.user.application.query.delete.DeleteUserRequest;
import com.curso.expecializacion.user.application.login.LoginUserRequest;
import com.curso.expecializacion.user.application.login.LoginUserResponse;
import com.curso.expecializacion.user.application.query.finByUserName.FindByUserNameRequest;
import com.curso.expecializacion.user.application.query.finByUserName.FindByUserNameResponse;
import com.curso.expecializacion.user.application.query.update.UpdateUserRequest;
import com.curso.expecializacion.user.application.register.RegisterUserRequest;
import com.curso.expecializacion.user.application.register.RegisterUserResponse;
import com.curso.expecializacion.user.infraestructure.api.dto.*;
import com.curso.expecializacion.user.infraestructure.api.mapper.UserMapper;
import com.curso.expecializacion.user.infraestructure.database.mapper.UsuarioEntityMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RequestMapping("/user")
@Tag(name = "Users", description = "Endpoints para la gestión y autenticación de usuarios")
@RestController
@RequiredArgsConstructor
public class UserController implements User_Api {

    private final Mediator mediator;
    private final UserMapper userMapper;
    private final UsuarioEntityMapper usuarioEntityMapper;



    ////https://www.instagram.com/accounts/log_in/


    @Operation(summary = "Endpoint público de prueba")
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @Operation(summary = "Endpoint de prueba protegido", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/helloSecured")
    public String helloSecured() {
        return "hello con seguridad";
    }

    @Override
    @Operation(summary = "Iniciar sesión para obtener el Token JWT")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDto) {
        LoginUserRequest request = userMapper.mapToLoginUserRequest(loginRequestDto);
        LoginUserResponse response = mediator.dispacth(request);
        TokenResponseDTO tokenResponseDto = userMapper.mapToTokenResponseDto(response);
        return ResponseEntity.ok(tokenResponseDto);
    }

    @Override
    @Operation(summary = "Registrar un nuevo usuario")
    @PostMapping("/create")
    public ResponseEntity<TokenResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDto) {
        RegisterUserRequest request = userMapper.mapToRegisterUserRequest(registerRequestDto);
        RegisterUserResponse response = mediator.dispacth(request);
        TokenResponseDTO tokenResponseDto = userMapper.mapToTokenResponseDto(response);
        return ResponseEntity.ok(tokenResponseDto);
    }


    @Override
    @Operation(summary = "Eliminar un usuario por ID (Requiere ROL ADMIN)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteByID(@PathVariable Integer id) {
        mediator.dispacthAsync(new DeleteUserRequest(id));
        return ResponseEntity.noContent().build();
    }

    @Override
    @Operation(summary = "Buscar un usuario por su EMAIL (Permitido por todos los roles)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'INVITED')")
    @GetMapping("/filtrar/{username}")
    public ResponseEntity<UsuarioDTO> findByUserName(@PathVariable String username) {
        log.info("Capa Controller , obteniendo producto  con EMAIl:{}", username);
        FindByUserNameResponse response = mediator.dispacth(new FindByUserNameRequest(username));
        UsuarioDTO usuarioDTO = usuarioEntityMapper.mapToUserDTO(response.getUsuario());
        log.info("Capa Controller , OBTENIDO producto con EMAIl:{}", username);
        return ResponseEntity.ok(usuarioDTO);
    }


    @Override
    @Operation(summary = "Actualizar Usuario , (Permitido por USER y ADMIN)", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("")
    public ResponseEntity<Void> update(@RequestBody UpdateUsuarioDTO updateUsuarioDTO) {
        log.info("Capa Controller , actualizando usuario");
        UpdateUserRequest request = usuarioEntityMapper.mapToCreateUserRequest(updateUsuarioDTO);
        mediator.dispacth(request);
        log.info("Capa Controller , ACTUALIZADO usuario con Nombre:{}", updateUsuarioDTO.getNickname());
        return ResponseEntity.noContent().build();
    }
}
