package org.example.exam8.config;

import lombok.RequiredArgsConstructor;
import org.example.exam8.entity.Income;
import org.example.exam8.entity.Product;
import org.example.exam8.entity.Role;
import org.example.exam8.entity.User;
import org.example.exam8.entity.enums.RoleName;
import org.example.exam8.repo.IncomeRepo;
import org.example.exam8.repo.ProductRepo;
import org.example.exam8.repo.RoleRepo;
import org.example.exam8.repo.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepo productRepo;
    private final IncomeRepo incomeRepo;
    @Override
    public void run(String... args) throws Exception {

    }

    private void addIncome() {
        Product product = productRepo.findById(UUID.fromString("48a8ce8b-7e10-436b-b77c-562d1040a7f5")).get();
        Income income1 = Income.builder()
                .amount(6)
                .price(270)
                .product(product)
                .build();
        Income income2 = Income.builder()
                .amount(5)
                .price(230)
                .product(product)
                .build();
        Income income3 = Income.builder()
                .amount(5)
                .price(200)
                .product(product)
                .build();
        Income income4 = Income.builder()
                .amount(10)
                .price(190)
                .product(product)
                .build();
        incomeRepo.saveAll(List.of(income1,income3, income2,income4));
    }

    private void generateUsers() {
        Role manager = new Role(1, RoleName.ROLE_MANAGER);
        Role  admin = new Role(2, RoleName.ROLE_ADMIN);

        roleRepo.save(manager);
        roleRepo.save(admin);

        User user1 = User.builder()
                .roles(List.of(manager))
                .firstName("Baxtiyor")
                .lastName("Sadulloyev")
                .number("978644400")
                .password(passwordEncoder.encode("123"))
                .build();
        User user2 = User.builder()
                .roles(List.of(admin))
                .firstName("Asadbek")
                .lastName("Sadulloyev")
                .number("934510104")
                .password(passwordEncoder.encode("123"))
                .build();
        userRepo.save(user1);
        userRepo.save(user2);
    }
}
