package com.capgemini.training.middleware.adapter.out.feign;
import com.capgemini.training.middleware.adapter.out.feign.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@FeignClient(name = "system-api", url = "${system-api.url:http://localhost:8081}",
    configuration = FeignClientConfig.class)
public interface SystemApiClient {
    @GetMapping("/api/v1/products")
    PageDTO<ProductDTO> getProducts(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size);
    @GetMapping("/api/v1/products/{id}")   ProductDTO getProductById(@PathVariable Long id);
    @PostMapping("/api/v1/products")       ProductDTO createProduct(@RequestBody CreateProductDTO dto);
    @GetMapping("/api/v1/categories")      List<CategoryDTO> getCategories();
    @GetMapping("/api/v1/categories/{id}") CategoryDTO getCategoryById(@PathVariable Long id);
}
