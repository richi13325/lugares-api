package com.lugares.api.service;

import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.PasswordResetToken;
import com.lugares.api.entity.Usuario;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.PasswordResetTokenRepository;
import com.lugares.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void forgotPassword(String email, String userType) {
        validateUserExists(email, userType);

        tokenRepository.deleteByUserEmail(email);

        SecureRandom secureRandom = new SecureRandom();
        String code = String.format("%06d", secureRandom.nextInt(1000000));

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(code);
        resetToken.setUserEmail(email);
        resetToken.setUserType(userType);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        tokenRepository.save(resetToken);

        emailService.sendSimpleMessage(
                email,
                "Solicitud de restablecimiento de contrasenia",
                "Tu codigo para restablecer la contrasenia es: " + code
        );
    }

    @Transactional
    public String validateCode(String email, String code) {
        PasswordResetToken resetToken = tokenRepository.findByUserEmailAndToken(email, code)
                .orElseThrow(() -> new BusinessRuleException("Codigo invalido"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new BusinessRuleException("Codigo expirado");
        }

        String secureToken = UUID.randomUUID().toString();
        resetToken.setToken(secureToken);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(resetToken);

        return secureToken;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException("Token invalido"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new BusinessRuleException("Token expirado");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        if ("Usuario".equalsIgnoreCase(resetToken.getUserType())) {
            Usuario usuario = usuarioRepository.findByCorreoElectronico(resetToken.getUserEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correoElectronico", resetToken.getUserEmail()));
            usuario.setContrasenia(encodedPassword);
            usuarioRepository.save(usuario);
        } else {
            Cliente cliente = clienteRepository.findByCorreoElectronico(resetToken.getUserEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", "correoElectronico", resetToken.getUserEmail()));
            cliente.setContrasenia(encodedPassword);
            clienteRepository.save(cliente);
        }

        tokenRepository.delete(resetToken);
    }

    private void validateUserExists(String email, String userType) {
        if ("Usuario".equalsIgnoreCase(userType)) {
            usuarioRepository.findByCorreoElectronico(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correoElectronico", email));
        } else if ("Cliente".equalsIgnoreCase(userType)) {
            clienteRepository.findByCorreoElectronico(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", "correoElectronico", email));
        } else {
            throw new BusinessRuleException("Tipo de usuario invalido: " + userType);
        }
    }
}
