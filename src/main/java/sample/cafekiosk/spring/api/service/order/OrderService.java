package sample.cafekiosk.spring.api.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    /**
     * 주문 생성 로직
     * 1. 주문 상품 번호로 상품을 조회한다.
     * 1-1. 상품 번호는 중복이 될 수 있다. -> findProductsBy() 에서 중복된 상품 번호로 중복된 상품 조회
     * 2. 주문하고자 하는 상품에는 재고가 있는 상품이 존재하기 때문에 재고가 있는 상품인지 체크한다.
     * 2-1. 재고가 있는 상품의 상품 번호로 재고 정보를 가져온다.(Stock)
     * 2-2. 재고 리스트를 순회하면 성능이 안나오므로 Map으로 만든다. Map<주문상품번호, 재고>
     * 3. 재고가 있는 상품의 리스트를 Map으로 변환해서 각 상품의 갯수를 집계한다.
     * 4. 재고를 차감한다.
     */
    /**
     * 재고 감소 -> 동시성 고민
     * optimistic lock / pessimistic lock / ...
     */
    public OrderResponse createOrder(OrderCreateRequest request, LocalDateTime registeredDateTime) {
        List<String> productNumbers = request.getProductNumbers();
        List<Product> products = findProductsBy(productNumbers); //(1-1)

        deductStockQuantities(products);

        Order order = Order.create(products, registeredDateTime);
        Order savedOrder = orderRepository.save(order);

        return OrderResponse.of(savedOrder);
    }

    public List<Product> findProductsBy(List<String> productNumbers) {
        List<Product> products = productRepository.findAllByProductNumberIn(productNumbers); // productNumbers = {"001", "001", "002"} 이거라면
        // products = { 아메리카노객체, 라떼객체 }
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductNumber, p -> p)); // 중복되는 주문번호로 상품 여러개 가져오기 위해
        // {
        //  {"001" : 아메리카노객체},
        //  {"002" : 라떼객체}
        // }

        return productNumbers.stream() //productNumbers = {"001", "001", "002"}로 중복 제거된 map 내부에서 각 product 객체들 가져옴
                .map(productMap::get)
                .collect(Collectors.toList());
        // {
        //  {"001" : 아메리카노객체},
        //  {"001" : 아메리카노객체},
        //  {"002" : 라떼객체}
        // }
    }

    private void deductStockQuantities(List<Product> products) {
        List<String> stockProductNumbers = extractStockProductNumbers(products);

        Map<String, Stock> stockMap = createStockMapBy(stockProductNumbers);
        Map<String, Long> productCountingMap = createCountingMapBy(stockProductNumbers);

        // 재고 차감 시도
        for (String stockProductNumber : new HashSet<>(stockProductNumbers)) {
            Stock stock = stockMap.get(stockProductNumber);
            int quantity = productCountingMap.get(stockProductNumber).intValue();

            if (stock.isQuantityLessThan(quantity)) {
                throw new IllegalArgumentException("재고가 부족한 상품이 있습니다.");
            }
            stock.deductQuantity(quantity);
        }
    }

    private static List<String> extractStockProductNumbers(List<Product> products) {
        // 재고 차감 체크가 필요한 상품들 filter (2)
        return products.stream()
                .filter(product -> ProductType.containsStockType(product.getType()))
                .map(Product::getProductNumber)
                .collect(Collectors.toList());
    }

    private Map<String, Stock> createStockMapBy(List<String> stockProductNumbers) {
        // 재고 엔티티 조회 (2-1)
        List<Stock> stocks = stockRepository.findAllByProductNumberIn(stockProductNumbers);
        return stocks.stream()
                .collect(Collectors.toMap(Stock::getProductNumber, s -> s));
    }


    private static Map<String, Long> createCountingMapBy(List<String> stockProductNumbers) {
        // 상품별 counting (3) Map<주문상품번호,주문상품번호 갯수>
        return stockProductNumbers.stream()
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
    }
}
