package com.gmt.rental;

import com.gmt.rental.entity.User;
import com.gmt.rental.entity.Vehicle;
import com.gmt.rental.entity.Category;
import com.gmt.rental.entity.RentalHistory;
import com.gmt.rental.repository.CategoryRepository;
import com.gmt.rental.repository.UserRepository;
import com.gmt.rental.repository.VehicleRepository;
import com.gmt.rental.repository.QuoteRequestRepository;
import com.gmt.rental.repository.RentalHistoryRepository;
import com.gmt.rental.service.admin.AdminCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:gmt_rental_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class AdminApiIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private QuoteRequestRepository quoteRequestRepository;
    @Autowired private RentalHistoryRepository rentalHistoryRepository;
    @Autowired private AdminCategoryService adminCategoryService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void adminCanCreateAndUpdateUsersWithoutStoringPlaintextPasswords() throws Exception {
        String adminEmail = "user-admin-" + UUID.randomUUID() + "@gmt.test";
        User admin = new User();
        admin.setNom("Admin");
        admin.setEmail(adminEmail);
        admin.setRole("ADMIN");
        admin.setActif(true);
        admin.setMotDePasse(passwordEncoder.encode("admin-test-password"));
        userRepository.save(admin);

        String loginBody = """
                {"email":"%s","motDePasse":"admin-test-password"}
                """.formatted(adminEmail);
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(loginResponse);
        if (!matcher.find()) throw new AssertionError("La réponse de connexion ne contient pas de jeton JWT");
        String auth = "Bearer " + matcher.group(1);

        String userEmail = "managed-user-" + UUID.randomUUID() + "@gmt.test";
        String createUserBody = """
                {"nom":"User","prenom":"Test","email":"%s","motDePasse":"user-test-password","role":"USER","actif":true}
                """.formatted(userEmail);
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.role").value("USER"));

        User savedUser = userRepository.findByEmailIgnoreCase(userEmail).orElseThrow();
        Long userId = savedUser.getId();
        String originalHash = savedUser.getMotDePasse();
        assertNotEquals("user-test-password", originalHash);
        assertTrue(passwordEncoder.matches("user-test-password", originalHash));
        mockMvc.perform(get("/api/admin/users").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(userEmail)));

        String updateBody = """
                {"nom":"User Modified","prenom":"Test","email":"%s","role":"USER","actif":false}
                """.formatted(userEmail);
        mockMvc.perform(put("/api/admin/users/{id}", userId)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("User Modified"))
                .andExpect(jsonPath("$.actif").value(false));
        savedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(originalHash, savedUser.getMotDePasse(), "Un mot de passe vide doit conserver le hash existant");
        assertFalse(savedUser.getActif());

        String passwordUpdateBody = """
                {"nom":"User Modified","prenom":"Test","email":"%s","motDePasse":"new-test-password","role":"USER","actif":true}
                """.formatted(userEmail);
        mockMvc.perform(put("/api/admin/users/{id}", userId)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordUpdateBody))
                .andExpect(status().isOk());
        String updatedHash = userRepository.findById(userId).orElseThrow().getMotDePasse();
        assertNotEquals("new-test-password", updatedHash);
        assertTrue(passwordEncoder.matches("new-test-password", updatedHash));

        String adminUserEmail = "managed-admin-" + UUID.randomUUID() + "@gmt.test";
        String createAdminBody = """
                {"nom":"Admin","prenom":"Test","email":"%s","motDePasse":"new-admin-password","role":"ADMIN","actif":true}
                """.formatted(adminUserEmail);
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAdminBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
        User savedAdmin = userRepository.findByEmailIgnoreCase(adminUserEmail).orElseThrow();
        assertTrue(passwordEncoder.matches("new-admin-password", savedAdmin.getMotDePasse()));

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserBody))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Invalid\",\"prenom\":\"User\",\"email\":\"invalid\",\"motDePasse\":\"secret\",\"role\":\"USER\",\"actif\":true}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/admin/users/{id}", 999999)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/users").header("Authorization", auth)).andExpect(status().isOk());
    }

    @Test
    @Transactional
    void adminCategoriesAreReturnedNewestFirst() throws Exception {
        for (String name : List.of("Test catégorie A", "Test catégorie B", "Test catégorie C")) {
            Category category = new Category();
            category.setNom(name);
            categoryRepository.save(category);
        }

        List<String> firstThree = adminCategoryService.findAll().stream()
                .map(Category::getNom)
                .limit(3)
                .toList();

        assertEquals(3, firstThree.size());
        assertEquals(List.of("Test catégorie C", "Test catégorie B", "Test catégorie A"), firstThree);
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Test catégorie C"))
                .andExpect(jsonPath("$[1].nom").value("Test catégorie B"))
                .andExpect(jsonPath("$[2].nom").value("Test catégorie A"));
    }

    @Test
    @Transactional
    void adminRoutesRequireJwtAndPublicQuoteContractStillWorks() throws Exception {
        mockMvc.perform(get("/api/admin/vehicules")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/api/parametres")).andExpect(status().isOk());

        Vehicle vehicle = new Vehicle();
        vehicle.setMarque("GMT");
        vehicle.setModele("Test");
        vehicle.setPlaces(5);
        vehicle.setPrixParJour(new BigDecimal("200"));
        vehicle.setStatut("ACTIF");
        Category category = new Category();
        category.setNom("Test " + UUID.randomUUID());
        category = categoryRepository.save(category);
        vehicle.setCategorie(category);
        vehicle = vehicleRepository.save(vehicle);
        mockMvc.perform(get("/api/vehicules")
                        .param("statut", "ACTIF")
                        .param("categorieId", category.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"modele\":\"Test\"")));

        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = start.plusDays(5);
        String quoteBody = """
                {"nom":"Ben Ali","prenom":"Sami","email":"sami-%s@email.com","telephone":"+216 22 333 444","vehiculeId":%d,"dateDebut":"%s","dateFin":"%s","lieuPriseEnCharge":"Aéroport Tunis-Carthage","lieuRestitution":"Agence GMT Sousse","message":"Test contrat public"}
                """.formatted(UUID.randomUUID(), vehicle.getId(), start, end);
        mockMvc.perform(post("/api/public/demandes").contentType(MediaType.APPLICATION_JSON).content(quoteBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1000")));
        if (quoteRequestRepository.findAll().size() != 1
                || !"NOUVELLE".equals(quoteRequestRepository.findAll().getFirst().getStatut())) {
            throw new AssertionError("La demande publique doit être persistée avec le statut NOUVELLE");
        }

        String missingVehicleBody = quoteBody.replace("\"vehiculeId\":" + vehicle.getId(), "\"vehiculeId\":999999");
        mockMvc.perform(post("/api/public/demandes").contentType(MediaType.APPLICATION_JSON).content(missingVehicleBody))
                .andExpect(status().isNotFound());

        String adminEmail = "admin-" + UUID.randomUUID() + "@gmt.test";
        User admin = new User();
        admin.setNom("Test");
        admin.setEmail(adminEmail);
        admin.setRole("ADMIN");
        admin.setActif(true);
        admin.setMotDePasse(passwordEncoder.encode("test-password"));
        userRepository.save(admin);

        String loginBody = """
                {"email":"%s","motDePasse":"test-password"}
                """.formatted(adminEmail);
        String loginResponse = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(loginResponse);
        if (!matcher.find()) throw new AssertionError("La réponse de connexion ne contient pas de jeton JWT");

        String token = matcher.group(1);
        for (String name : List.of("Test A", "Test B", "Test C")) {
            mockMvc.perform(post("/api/admin/categories")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nom\":\"" + name + "\",\"description\":\"\"}"))
                    .andExpect(status().isCreated());
        }
        for (int refresh = 0; refresh < 2; refresh++) {
            mockMvc.perform(get("/api/admin/categories").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nom").value("Test C"))
                    .andExpect(jsonPath("$[1].nom").value("Test B"))
                    .andExpect(jsonPath("$[2].nom").value("Test A"));
        }

        mockMvc.perform(get("/api/admin/dashboard/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("totalVehicules")));

        mockMvc.perform(patch("/api/admin/vehicules/{id}/statut", vehicle.getId())
                        .header("Authorization", "Bearer " + token)
                        .param("statut", "INACTIF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("INACTIF"));
        if (!"INACTIF".equals(vehicleRepository.findById(vehicle.getId()).orElseThrow().getStatut())) {
            throw new AssertionError("Le statut INACTIF doit être persisté après l’appel PATCH");
        }
        mockMvc.perform(get("/api/admin/vehicules/{id}", vehicle.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("INACTIF"));

        mockMvc.perform(patch("/api/admin/vehicules/{id}/statut", vehicle.getId())
                        .header("Authorization", "Bearer " + token)
                        .param("statut", "ACTIF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACTIF"));
        if (!"ACTIF".equals(vehicleRepository.findById(vehicle.getId()).orElseThrow().getStatut())) {
            throw new AssertionError("Le statut ACTIF doit être persisté après l’appel PATCH");
        }
        mockMvc.perform(get("/api/admin/vehicules/{id}", vehicle.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACTIF"));
    }

    @Test
    @Transactional
    void adminCanCreateAndReloadUserRentalHistoryForAnExistingVehicle() throws Exception {
        String adminEmail = "rental-admin-" + UUID.randomUUID() + "@gmt.test";
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setRole("ADMIN");
        admin.setActif(true);
        admin.setMotDePasse(passwordEncoder.encode("rental-test-password"));
        userRepository.save(admin);

        User customer = new User();
        customer.setEmail("rental-user-" + UUID.randomUUID() + "@gmt.test");
        customer.setRole("USER");
        customer.setActif(true);
        userRepository.save(customer);

        Category category = new Category();
        category.setNom("Rental " + UUID.randomUUID());
        category = categoryRepository.save(category);

        Vehicle vehicle = new Vehicle();
        vehicle.setMarque("Volkswagen");
        vehicle.setModele("Golf 8");
        vehicle.setPlaces(5);
        vehicle.setPrixParJour(new BigDecimal("200"));
        vehicle.setStatut("ACTIF");
        vehicle.setCategorie(category);
        vehicle = vehicleRepository.save(vehicle);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","motDePasse":"rental-test-password"}
                                """.formatted(adminEmail)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(loginResponse);
        if (!matcher.find()) throw new AssertionError("La réponse de connexion ne contient pas de jeton JWT");
        String auth = "Bearer " + matcher.group(1);

        String historyUrl = "/api/admin/users/{userId}/rentals";
        mockMvc.perform(get(historyUrl, customer.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(historyUrl, customer.getId()).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        String start = "2026-10-01";
        String end = "2026-10-05";
        mockMvc.perform(post(historyUrl, customer.getId())
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId":%d,"dateDebut":"%s","dateFin":"%s","prixPaye":800,"devise":"DT"}
                                """.formatted(vehicle.getId(), start, end)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicle").value("Volkswagen Golf 8"))
                .andExpect(jsonPath("$.dateDebut").value(start))
                .andExpect(jsonPath("$.dateFin").value(end))
                .andExpect(jsonPath("$.prixPaye").value(800))
                .andExpect(jsonPath("$.devise").value("DT"));

        RentalHistory saved = rentalHistoryRepository.findAllByUserIdOrderByDateDebutDescIdDesc(customer.getId()).getFirst();
        assertEquals(customer.getId(), saved.getUser().getId());
        assertEquals(vehicle.getId(), saved.getVehicleEntity().getId());
        assertEquals(0, saved.getPrixPaye().compareTo(new BigDecimal("800")));
        assertEquals("DT", saved.getDevise());

        mockMvc.perform(get(historyUrl, customer.getId()).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(saved.getId()))
                .andExpect(jsonPath("$[0].vehicle").value("Volkswagen Golf 8"));
        mockMvc.perform(post(historyUrl, customer.getId())
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId":999999,"dateDebut":"2026-10-01","dateFin":"2026-10-05","prixPaye":800,"devise":"DT"}
                                """))
                .andExpect(status().isNotFound());
    }
}
