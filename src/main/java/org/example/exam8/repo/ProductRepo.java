package org.example.exam8.repo;

import org.example.exam8.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface ProductRepo extends JpaRepository<Product, UUID> {

    @Query(nativeQuery = true, value = "select* from product where category_id =:uuid")
    List<Product> findAllByCategoryId(UUID uuid);

    List<Product> findAllByCategoryIdAndNameContainingIgnoreCase(UUID categoryId, String name);
}
