package gruppe9ek.kino.web;

import gruppe9ek.kino.entity.ProductSale;
import gruppe9ek.kino.service.ProductSaleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-sales")
public class ProductSaleController {
    private final ProductSaleService productSaleService;

    public ProductSaleController(ProductSaleService productSaleService) {
        this.productSaleService = productSaleService;
    }

    @GetMapping
    public List<ProductSale> getAll() {
        return productSaleService.all();
    }

    @GetMapping("/{id}")
    public ProductSale getById(@PathVariable Integer id) {
        return productSaleService.byId(id);
    }

    @PostMapping
    public ProductSale create(@RequestBody ProductSale productSale) {
        return productSaleService.create(productSale);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        productSaleService.delete(id);
    }
}
