package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.ProductSale;
import gruppe9ek.kino.repository.ProductSaleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductSaleService {
    private final ProductSaleRepository productSales;

    public ProductSaleService(ProductSaleRepository productSales) {
        this.productSales = productSales;
    }

    public List<ProductSale> all() {
        return productSales.findAll();
    }

    public ProductSale byId(Integer id) {
        return productSales.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produktsalg blev ikke fundet"));
    }

    public ProductSale create(ProductSale ps) {
        ps.setSaleId(null);
        return productSales.save(ps);
    }

    public void delete(Integer id) {
        productSales.deleteById(id);
    }
}
