package br.com.felipefarias.webflux.controller;

import br.com.felipefarias.webflux.entity.User;
import br.com.felipefarias.webflux.mapper.UserMapper;
import br.com.felipefarias.webflux.model.request.UserRequest;
import br.com.felipefarias.webflux.model.response.UserResponse;
import br.com.felipefarias.webflux.service.UserService;
import br.com.felipefarias.webflux.service.exception.ObjectNotFoundException;
import com.mongodb.reactivestreams.client.MongoClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
class UserControllerImplTest {

    private static final String ID = "123456";
    private static final String NAME = "Felipe";
    private static final String EMAIL = "felipe@mail.com";
    private static final String PASSWORD = "123456";
    private static final String BASE_URI = "/users";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService service;

    @MockBean
    private UserMapper mapper;

    @MockBean
    private MongoClient mongoClient;

    @Test
    @DisplayName("Test endpoint save with success")
    void testSaveWithSuccess() {
        final UserRequest request = new UserRequest(NAME, EMAIL, PASSWORD);

        when(service.save(any(UserRequest.class))).thenReturn(Mono.just(User.builder().build()));

        webTestClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(request))
                .exchange()
                .expectStatus().isCreated();

        verify(service).save(any(UserRequest.class));
    }

    @Test
    @DisplayName("Test endpoint save with bad request")
    void testSaveWithBadRequest() {
        final UserRequest request = new UserRequest(NAME.concat(" "), EMAIL, PASSWORD);

        webTestClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.path").isEqualTo(BASE_URI)
                .jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.error").isEqualTo("Validation error")
                .jsonPath("$.message").isEqualTo("Error on validation attributes")
                .jsonPath("$.errors[0].fieldName").isEqualTo("name")
                .jsonPath("$.errors[0].message").isEqualTo("field cannot have blank spaces at the beginning or at end");
    }

    @Test
    @DisplayName("Test find by id endpoint with success")
    void testFindByIdWithSuccess() {
        final UserResponse userResponse = new UserResponse(ID, NAME, EMAIL, PASSWORD);

        when(service.findById(anyString())).thenReturn(Mono.just(User.builder().build()));
        when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

        webTestClient.get().uri(BASE_URI + "/" + ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(ID)
                .jsonPath("$.name").isEqualTo(NAME)
                .jsonPath("$.email").isEqualTo(EMAIL)
                .jsonPath("$.password").isEqualTo(PASSWORD);

        verify(service).findById(anyString());
        verify(mapper).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Test find by id endpoint with not found")
    void testFindByIdWithNotFound() {
        when(service.findById(anyString())).thenThrow(ObjectNotFoundException.class);

        webTestClient.get().uri(BASE_URI + "/" + ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Test find all endpoint with success")
    void testFindAllWithSuccess() {
        final UserResponse userResponse = new UserResponse(ID, NAME, EMAIL, PASSWORD);

        when(service.findAll()).thenReturn(Flux.just(User.builder().build()));
        when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

        webTestClient.get().uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(ID)
                .jsonPath("$.[0].name").isEqualTo(NAME)
                .jsonPath("$.[0].email").isEqualTo(EMAIL)
                .jsonPath("$.[0].password").isEqualTo(PASSWORD);

        verify(service).findAll();
        verify(mapper).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Test update endpoint with success")
    void testUpdateWithSuccess() {
        final UserResponse userResponse = new UserResponse(ID, NAME, EMAIL, PASSWORD);
        final UserRequest request = new UserRequest(NAME, EMAIL, PASSWORD);

        when(service.update(anyString(), any(UserRequest.class))).thenReturn(Mono.just(User.builder().build()));
        when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

        webTestClient.patch().uri(BASE_URI + "/" + ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(ID)
                .jsonPath("$.name").isEqualTo(NAME)
                .jsonPath("$.email").isEqualTo(EMAIL)
                .jsonPath("$.password").isEqualTo(PASSWORD);

        verify(service).update(anyString(), any(UserRequest.class));
        verify(mapper).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Test delete endpoint with success")
    void testDeleteWithSuccess() {
        when(service.delete(anyString())).thenReturn(Mono.just(User.builder().build()));

        webTestClient.delete().uri(BASE_URI + "/" + ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(service).delete(anyString());
    }

    @Test
    @DisplayName("Test delete endpoint with not found")
    void testDeleteWithNotFound() {
        when(service.delete(anyString())).thenThrow(ObjectNotFoundException.class);

        webTestClient.delete().uri(BASE_URI + "/" + ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}