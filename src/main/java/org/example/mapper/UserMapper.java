package org.example.mapper;

import org.example.dto.UserResponse;
import org.example.entity.User;
import org.example.util.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@NullMarked
@Mapper(imports = {StringUtils.class})
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "fullName", expression = "java(StringUtils.combineNames(user.getFirstName(), user.getLastName()))")
    @Mapping(target = "ageGroup", expression = "java(StringUtils.getAgeGroup(user.getAge()))")  
    @Mapping(target = "email", expression = "java(StringUtils.formatEmail(user.getEmail()))")
    UserResponse userToUserResponse(User user);

    default @Nullable UserResponse safeUserMapping(@Nullable User user) {
        if (user == null) {
            return null;
        }
        return userToUserResponse(user);
    }

    default String safeStringConversion(@Nullable Object value) {
        return StringUtils.safeToString(value);
    }
}