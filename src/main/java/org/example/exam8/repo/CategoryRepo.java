package org.example.exam8.repo;

import jakarta.transaction.Transactional;
import org.example.exam8.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface CategoryRepo extends JpaRepository<Category, UUID> {
    @Transactional
    @Query(nativeQuery = true,value = """
        select * from category
                 where lower(name) like lower(concat('%',:categoryName,'%'))
                 order by name
    """)
    public List<Category> findAllByName(String categoryName);


    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = """
    select *from category order by name
""")
    List<Category> findAllOrderByName();
}
