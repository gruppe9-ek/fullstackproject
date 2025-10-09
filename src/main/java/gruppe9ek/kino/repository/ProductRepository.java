package gruppe9ek.kino.repository;

import gruppe9ek.kino.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
