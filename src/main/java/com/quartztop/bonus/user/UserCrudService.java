package com.quartztop.bonus.user;

import com.quartztop.bonus.DuplicateResourceException;
import com.quartztop.bonus.repositoriesBonus.UserRepository;
import com.quartztop.bonus.servises.MessageService;
import com.quartztop.bonus.user.roles.Roles;
import com.quartztop.bonus.repositoriesBonus.RolesRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserCrudService {

    private final UserRepository userRepository;
    private final MessageService messageService;
    private final RolesRepository rolesRepository;

    public void updateUser(UserEntity userEntity) {
        userRepository.save(userEntity);
        //mapToDto(userEntity);
    }

    @Transactional
    public Optional<UserDto> create(UserDto userDto) {

        try {
            // Маппим DTO в сущность
            UserEntity userEntity = mapToEntity(userDto);

            // Ищем роль ROLE_USER перед сохранением пользователя
            Roles userRole = rolesRepository.findByRole("ROLE_USER");

            if (userRole == null) {
                throw new RuntimeException("Role not found");
            }

            // Назначаем роль пользователю
            userEntity.setRoles(userRole);

            // Сохраняем пользователя с назначенной ролью
            UserEntity newUser = userRepository.save(userEntity);

            // Устанавливаем ID в DTO
            userDto.setId(newUser.getId());

            return Optional.of(userDto);
        } catch (DataIntegrityViolationException e) {
            Throwable cause = e.getMostSpecificCause();

            // Обработка уникальных ограничений на email и телефон
            if (cause.getMessage() != null && cause.getMessage().contains("(e_mail)=")) {
                throw new DuplicateResourceException(messageService.getErrorMessageEmail());
            } else if (cause.getMessage() != null && cause.getMessage().contains("(phone)=")) {
                throw new DuplicateResourceException(messageService.getErrorMessagePhone());
            } else {
                throw e; // Перебрасываем оригинальное исключение, если оно не связано с уникальными ключами
            }
        }
    }

    public Optional<UserDto> getUser(int id) {
        Optional<UserEntity> userEntityOpt = userRepository.findById(id);
        return userEntityOpt.map(UserCrudService::mapToDto);
    }

    public List<UserEntity> getAllUsers() {

        List<UserEntity> users = userRepository.findAll();
        // Удаляем всех пользователей с ролью ROLE_SUPER_ADMIN
        users.removeIf(user -> user.getRoles().getRole().contains("ROLE_SUPER_ADMIN"));
        return users;
    }

    public List<UserEntity> getAllUsers(Roles role) {
        List<UserEntity> users = userRepository.findByRoles(role.getId());
        // Удаляем всех пользователей с ролью ROLE_SUPER_ADMIN
        users.removeIf(user -> user.getRoles().getRole().contains("ROLE_SUPER_ADMIN"));
        return users;
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
