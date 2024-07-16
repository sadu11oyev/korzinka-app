package org.example.exam8.repo;

import jakarta.transaction.Transactional;
import org.example.exam8.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncomeRepo extends JpaRepository<Income, UUID> {
    @Transactional
    @Query(nativeQuery = true,value = """
        select * from income where product_id=:productId
        """)
    List<Income> findAllByProductId(UUID productId);

    @Transactional
    @Query(nativeQuery = true,value = """
        select  coalesce(sum(amount),0) from income where product_id=:productId
""")
    Integer findAllAmountProduct(UUID productId);
}
