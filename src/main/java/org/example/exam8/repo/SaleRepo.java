package org.example.exam8.repo;

import jakarta.transaction.Transactional;
import org.example.exam8.dao.ReqSale;
import org.example.exam8.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SaleRepo extends JpaRepository<Sale, UUID> {

    @Transactional
    @Query(nativeQuery = true,value = """
        select * from sale where product_id=:productId
        """)
    List<Sale> findAllByProductId(UUID productId);

    @Transactional
    @Query(nativeQuery = true, value = """
        select p.name as name, sum(s.amount) as amount, (p.selling*sum(s.amount)) as summa
        from sale s
            join product p on s.product_id = p.id
        group by p.name, p.selling
""")
    List<Object[]> findSalesData();


    @Transactional
    @Query(nativeQuery = true,value = """
        SELECT
            p.name AS name,
            (COALESCE(income.totalAmount, 0) - COALESCE(sale.totalAmount, 0)) AS amount
        FROM
            product p
                LEFT JOIN (
                SELECT
                    product_id,
                    SUM(amount) AS totalAmount
                FROM
                    income
                GROUP BY
                    product_id
            ) AS income ON p.id = income.product_id
                LEFT JOIN (
                SELECT
                    product_id,
                    SUM(amount) AS totalAmount
                FROM
                    sale
                GROUP BY
                    product_id
            ) AS sale ON p.id = sale.product_id order by amount;
""")
    List<Object[]> findBalanceData();


    @Transactional
    @Query(nativeQuery = true,value = """
        SELECT
            name,
            profit
        FROM (
                 SELECT
                     p.name AS name,
                     COALESCE(sale.totalValue, 0) - COALESCE(income.totalValue, 0) AS profit
                 FROM
                     product p
                         LEFT JOIN (
                         SELECT
                             s.product_id,
                             SUM(s.price * s.amount) AS totalValue
                         FROM
                             sale s
                         GROUP BY
                             s.product_id
                     ) AS sale ON p.id = sale.product_id
                         LEFT JOIN (
                         SELECT
                             i.product_id,
                             SUM(i.price * i.amount) AS totalValue
                         FROM
                             income i
                         GROUP BY
                             i.product_id
                     ) AS income ON p.id = income.product_id
             ) AS subquery
        WHERE
            profit <> 0
        ORDER BY
            profit DESC; 
""")
    List<Object[]> findProfits();

    @Transactional
    @Query(nativeQuery = true,value = """
    select coalesce(sum(amount),0) from sale where product_id=:productId
""")
    Integer findAllAmountByProductId(UUID productId);
}
