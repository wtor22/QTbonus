package com.quartztop.bonus.user;

import com.quartztop.bonus.DuplicateResourceException;
import com.quartztop.bonus.ResourceNotFoundException;
import com.quartztop.bonus.orders.TypeActivity;
import com.quartztop.bonus.repositoriesBonus.TypeActivityRepositories;
import com.quartztop.bonus.repositoriesBonus.UserRepository;
import com.quartztop.bonus.servises.MessageService;
import com.quartztop.bonus.user.roles.Roles;
import com.quartztop.bonus.repositoriesBonus.RolesRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserCrudService {

    private final UserRepository userRepository;
    private final MessageService messageService;
    private final RolesRepository rolesRepository;
    private final TypeActivityRepositories typeActivityRepositories;

    public void updateUser(UserEntity userEntity) {
        userRepository.save(userEntity);
        //mapToDto(userEntity);
    }

    public void updateUser(int userId, UserDto userDto) {

        try {

            UserEntity existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            if (!existingUser.getPhone().equals(userDto.getPhone())) {
                if (userRepository.existsByPhone(userDto.getPhone())) {
                    throw new DuplicateResourceException(messageService.getErrorMessagePhone());
                }
            }

            // Обновляем поля пользователя
            existingUser.setPhone(userDto.getPhone());
            existingUser.setFio(userDto.getFio());
            existingUser.setNameSalon(userDto.getNameSalon());
            existingUser.setCity(userDto.getCity());
            existingUser.setAddress(userDto.getAddress());
            existingUser.setInnCompany(userDto.getInnCompany());

            userRepository.save(existingUser);

        } catch (DataIntegrityViolationException e) {
            Throwable cause = e.getMostSpecificCause();

            if (cause.getMessage() != null && cause.getMessage().contains("(e_mail)=")) {
                throw new DuplicateResourceException(messageService.getErrorMessageEmail());
            } else if (cause.getMessage() != null && cause.getMessage().contains("(phone)=")) {
                throw new DuplicateResourceException(messageService.getErrorMessagePhone());
            } else {
                throw e;
            }
        }
    }
    @Transactional
    public Optional<UserDto> create(UserDto userDto) {
        try {
            // Маппим DTO в сущность
            UserEntity userEntity = mapToEntity(userDto);

            Optional<TypeActivity> typeActivityOptional = typeActivityRepositories.findById(userDto.getTypeActivity());

            if (typeActivityOptional.isPresent()) {
                TypeActivity typeActivity = typeActivityOptional.get();
                userEntity.setTypeActivity(typeActivity);
            }
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
        List<UserEntity> users = userRepository.findAllWithCreateDate();
        // Удаляем всех пользователей с ролью ROLE_SUPER_ADMIN
        users.removeIf(user -> user.getRoles().getRole().contains("ROLE_SUPER_ADMIN"));
        return users;
    }
    public List<UserDto> getAllUsersDtoByManager(UserEntity manager) {
        List<UserEntity> users = userRepository.findAllWithCreateDateAndManager(manager);

        return users.stream().map(UserCrudService::mapToDto).toList();
    }
    public List<UserEntity> getAllUsersEntityByManager(UserEntity manager) {
        return userRepository.findAllWithCreateDateAndManager(manager);
    }

    public List<UserEntity> getAllUsers(Roles role) {
        List<UserEntity> users = userRepository.findByRoles(role);
        // Удаляем всех пользователей с ролью ROLE_SUPER_ADMIN
        users.removeIf(user -> user.getRoles().getRole().contains("ROLE_SUPER_ADMIN"));
        return users;
    }

    public Optional<UserEntity> getUserById(int id) {
        return userRepository.findById(id);
    }

    public UserEntity getUserByFio(String fio) {
        return userRepository.findByFio(fio);
    }

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserEntity findUserByEmail(String email) {
        if(findByEmail(email).isPresent()) {
            return findByEmail(email).orElseThrow();
        }
        return null;
    }
    public static UserDto mapToDto(UserEntity user) {

        UserDto userDto = new UserDto();

        userDto.setFio(user.getFio());
        userDto.setEmail(user.getEmail());
        userDto.setAddress(user.getAddress());
        userDto.setPhone(user.getPhone());
        userDto.setCity(user.getCity()); // не у всех есть
        userDto.setNameSalon(user.getNameSalon());
        userDto.setInnCompany(user.getInnCompany());
        userDto.setId(user.getId());
        userDto.setTypeActivity(user.getTypeActivity().getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formateDate = user.getCreateDate().format(formatter);
        userDto.setCreateDate(formateDate);

        return userDto;
    }

    public static UserEntity mapToEntity(UserDto user) {

        UserEntity userEntity = new UserEntity();

        userEntity.setFio(user.getFio());
        userEntity.setEmail(user.getEmail());
        userEntity.setAddress(user.getAddress());
        userEntity.setPhone(user.getPhone());
        userEntity.setCity(user.getCity());
        userEntity.setNameSalon(user.getNameSalon());
        userEntity.setInnCompany(user.getInnCompany());
        userEntity.setPassword(user.getPassword());


        return userEntity;
    }


}
