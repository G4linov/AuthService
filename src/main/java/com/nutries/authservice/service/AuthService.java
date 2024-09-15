package com.nutries.authservice.service;

import com.nutries.authservice.dto.ProfileRegisterDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {


    private final RestTemplate restTemplate;

    // URL ProfileService (можно вынести в application.yml или application.properties)
    @Value("${profileService.url}")
    private String profileServiceUrl;

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public void createProfileInProfileService(ProfileRegisterDto profileRegisterDto) {
        String url = profileServiceUrl + "/api/profiles";  // URL для создания профиля

        // Создаем заголовки для запроса (можно добавить авторизацию, если нужно)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Создаем тело запроса
        HttpEntity<ProfileRegisterDto> request = new HttpEntity<>(profileRegisterDto, headers);

        // Отправляем POST-запрос в ProfileService
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class
        );

        // Обрабатываем ответ (можно добавить логику для обработки ошибок и успеха)
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Профиль создан успешно");
        } else {
            System.err.println("Ошибка при создании профиля");
        }
    }
}
