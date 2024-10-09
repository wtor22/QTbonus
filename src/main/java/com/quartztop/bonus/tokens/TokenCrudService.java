package com.quartztop.bonus.tokens;

import com.quartztop.bonus.repositoriesBonus.TokenRepository;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.repositoriesBonus.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class TokenCrudService {

    private final TokenRepository tokenRepository;
    private UserRepository userRepository;

    public void create(String token, UserDto userDto) {
        UserEntity user = userRepository.findById(
                userDto.getId()).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userDto.getId()));
        TokenEntity tokenEntity = new TokenEntity(token,user);
        if(userDto.getManager() != null) {
            tokenEntity.setManager(userDto.getManager());
        }
        tokenRepository.save(tokenEntity);
    }

    public void updateStatus(String token) {

        TokenEntity tokenEntity = getByToken(token).orElseThrow();
        tokenEntity.setClosed(true);
        tokenRepository.save(tokenEntity);
    }

    public Optional<TokenEntity> getByToken(String token) {

        if (token == null || token.trim().isEmpty()) {
            // TODO Логирование или выброс исключения
            return Optional.empty();
        }
        if(tokenRepository.findByToken(token).isEmpty()) {
            log.info("TOKEN IS EMPTY " );
        }

        return tokenRepository.findByToken(token);
    }
}
