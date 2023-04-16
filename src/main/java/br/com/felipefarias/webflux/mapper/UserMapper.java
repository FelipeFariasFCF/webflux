package br.com.felipefarias.webflux.mapper;

import br.com.felipefarias.webflux.entity.User;
import br.com.felipefarias.webflux.model.request.UserRequest;
import br.com.felipefarias.webflux.model.response.UserResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(final UserRequest request);

    @Mapping(target = "id", ignore = true)
    User toEntity(final UserRequest request, @MappingTarget final User entity);

    UserResponse toResponse(final User entity);
}