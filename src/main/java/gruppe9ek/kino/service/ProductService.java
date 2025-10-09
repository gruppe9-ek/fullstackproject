package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.Product;
import gruppe9ek.kino.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public List<Product> all() {
        return products.findAll();
    }

    public Product byId(Integer id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produkt blev ikke fundet"));
    }

    public Product create(Product p) {
        p.setProductId(null);
        return products.save(p);
    }

    public Product update(Integer id, Product p) {
        if (!products.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produkt blev ikke fundet");
        }
        p.setProductId(id);
        return products.save(p);
    }

    public void delete(Integer id) {
        products.deleteById(id);
    }
}
