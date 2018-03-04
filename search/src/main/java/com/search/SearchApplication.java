package com.search;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.common.Product;
import com.common.ProductService;

@RestController
@SpringBootApplication(scanBasePackages = {"com", "hello"})
@RequestMapping("/search")
public class SearchApplication {

    /*
        Simple GET call to test this application.
     */
    @RequestMapping("/hello")
    @GetMapping
    public String hello() {
        return "Search: Hello";
    }

    /*
        Simple GET call to see whether we are able return a class as JSON.
     */
    @RequestMapping(value = "/product",
                    method = RequestMethod.GET,
                    produces = {"application/json"})
    public Product product() {
        return new Product(1, "Laptop", 45000d);
    }

    /*
        Get call using a Path param and return a Product as JSON.
     */
    @RequestMapping(value = "/product/{id}",
            method = RequestMethod.GET,
            produces = {"application/json"})
    public Product productWith(@PathVariable("id") long id) {
        Optional<Product> product = ProductService.getProducts()
                                                    .stream()
                                                        .filter(p -> p.getId() == id)
                                                            .findFirst();
        return product.get();
    }

    /*
        GET to return list of Products as JSON.
     */
    @RequestMapping(value = "/products",
            method = RequestMethod.GET,
            produces = {"application/json"})
    public List<Product> products() {
        return ProductService.getProducts();
    }

    /*
        POST call to insert a new Product.
     */
    @RequestMapping(value = "/product/create",
                    method = RequestMethod.POST,
                    consumes = {"application/json"}
                    )
    public String createProduct(@RequestBody Product product) {
        if (ProductService.createProduct(product)) {
            return "Created Product = " + product.toString();
        }

        return "Creation failed for Product = " + product.toString();
    }


    /**
     * Runs Spring Boot Module.
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
