package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Address;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Objective: ManyToOne, OneToOne 관계 최적화
 * <p>
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1: 엔티티 직접 노출
     * 이슈 1: 양방향 관계 문제. 서로가 서로를 호출해서 무한루프에 빠지는 현상
     * 해결: 양방향 관계 엔티티중 한 곳에 @JsonIgnore 를 붙여줌.
     *
     * 이슈 2: Order엔티티엔 Member엔티티가 포함되어 있고, 이를 Json라이브러리에서 프록시 객체로 초기화한다. 이 때 초기값이 없어서 생기는 문제.
     * 해결: Hibernate5Module 모듈 등록, LAZY=null 처리. Json라이브러리에게 초기화되자 않은 프록시 객체는 노출하지 않도록 함.
     * 
     * 문제점: 엔티티 직접 반환
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        return all;
    }

    /**
     * V2: 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     *
     * 문제점: 지연로딩으로 쿼리 N번 호출
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }

    /**
     * V3. fetch join으로 쿼리 1번 호출
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }

    /**
     * V4: 엔티티에서 DTO로 변환하는 코드 없이, JPA에서 DTO로 바로 조회
     * HOW: select 절에서 원하는 데이터만 선택해서 조회
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }

    }
}
