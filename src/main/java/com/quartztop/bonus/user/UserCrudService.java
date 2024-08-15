package com.quartztop.bonus.user;

import com.quartztop.bonus.DuplicateResourceException;
import com.quartztop.bonus.servises.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserCrudService {

    private final UserRepository userRepository;
    private final MessageService messageService;

    public void updateUser(UserEntity userEntity) {
        userRepository.save(userEntity);
        mapToDto(userEntity);
    }

    public Optional<UserDto> create(UserDto userDto) {
        try{
            UserEntity userEntity = mapToEntity(userDto);
            UserEntity newUser = userRepository.save(userEntity);
            userDto.setId(newUser.getId());
            return Optional.of(userDto);
        } catch (DataIntegrityViolationException e) {
            Throwable cause = e.getMostSpecificCause();
            if (cause.getMessage().contains("users_e_mail_key") ) {
                throw new DuplicateResourceException(messageService.getErrorMessageEmail());
            } else if (cause.getMessage().contains("users_phone_key")) {
                throw new DuplicateResourceException(messageService.getErrorMessagePhone());
            } else {
                throw e;
            }
        }
    }

    public Optional<UserDto> getUser(int id) {
        Optional<UserEntity> userEntityOpt = userRepository.findById(id);
        return userEntityOpt.map(UserCrudService::mapToDto);
    }

    public Optional<UserEntity> getUserById(int id) {
        return userRepository.findById(id);
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public static UserDto mapToDto(UserEntity user) {

        UserDto userDto = new UserDto();

        userDto.setFio(user.getFio());
        userDto.setEmail(user.getEmail());
        userDto.setAddress(user.getAddress());
        userDto.setPhone(user.getPhone());
        userDto.setManager(user.getManager());
        userDto.setNameSalon(user.getNameSalon());
        userDto.setId(user.getId());

        return userDto;
    }

    public static UserEntity mapToEntity(UserDto user) {

        UserEntity userEntity = new UserEntity();

        userEntity.setFio(user.getFio());
        userEntity.setEmail(user.getEmail());
        userEntity.setAddress(user.getAddress());
        userEntity.setPhone(user.getPhone());
        userEntity.setManager(user.getManager());
        userEntity.setNameSalon(user.getNameSalon());
        userEntity.setPassword(user.getPassword());

        return userEntity;
    }


}
