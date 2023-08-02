package sample.cafekiosk.spring.api.service.product.request;

import lombok.Builder;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

public class ProductCreateServiceRequest {

    private ProductType type;

    private ProductSellingStatus sellingStatus;

    // 만약 도메인 정책으로 name -> 상품 이름은 20자 제한이 있다면 controller단에서 검증하는것이 맞을까?
    // 이런 도메인 정책은 도메인단에서 검증해주는 것이 좋다.
    // 검증에 관한 책임도 레이어 별로 나눠서 만들어보자.
    private String name;

    private int price;

    @Builder
    public ProductCreateServiceRequest(ProductType type, ProductSellingStatus sellingStatus, String name, int price) {
        this.type = type;
        this.sellingStatus = sellingStatus;
        this.name = name;
        this.price = price;
    }

    public Product toEntity(String nextProductNumber) {
        return Product.builder()
                .productNumber(nextProductNumber)
                .type(type)
                .sellingStatus(sellingStatus)
                .name(name)
                .price(price)
                .build();
    }
}
