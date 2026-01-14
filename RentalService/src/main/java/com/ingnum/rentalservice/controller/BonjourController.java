package com.ingnum.rentalservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class RentalController {

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    Logger logger = LoggerFactory.getLogger(RentalController.class);

    @GetMapping("/bonjour")
    public String bonjour() {
        return "bonjour de RentalService";
    }

    @GetMapping("/customer/{name}")
    public String getCustomer(@PathVariable String name) {
        RestTemplate restTemplate = new RestTemplate();
        
        String url = customerServiceUrl; 
        
        logger.info("Envoi de la requête à : " + url);
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            return "Client : " + name + " | Réponse PHP : " + response;
        } catch (Exception e) {
            return "Erreur : le service PHP n'a pas répondu à l'adresse " + url;
        }
    }
}
