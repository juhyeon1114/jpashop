package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Address;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("상품주문")
    public void 상품주문() throws Exception {
        // given
        int PRICE = 10000;
        int STOCK_CNT = 10;
        int ORDER_CNT = 2;

        Member member = createMember();
        Book book = createBook("JPA", PRICE, STOCK_CNT);

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), ORDER_CNT);

        // then
        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.ORDER, order.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1, order.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 한다.");
        assertEquals(PRICE * ORDER_CNT, order.getTotalPrice() , "주문 가격은 가격 * 수량이다");
        assertEquals(STOCK_CNT - ORDER_CNT, book.getStockQuantity(), "주문 수량만큼 재고가 줄어야 한다.");
    }

    @Test
    @DisplayName("상품주문_재고수량초과")
    public void 상품주문_재고수량초과() throws Exception {
        // given
        int PRICE = 10000;
        int STOCK_CNT = 10;
        int ORDER_CNT = 11;

        Member member = createMember();
        Book book = createBook("JPA", PRICE, STOCK_CNT);

        assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), book.getId(), ORDER_CNT);
        },  "재고보다 많은 주문이 들어오면 예외 발생");
    }

    @Test
    @DisplayName("주문취소")
    public void 주문취소() throws Exception {
        // given
        int PRICE = 10000;
        int STOCK_CNT = 10;
        int ORDER_CNT = 10;

        Member member = createMember();
        Book book = createBook("JPA", PRICE, STOCK_CNT);
        Long orderId = orderService.order(member.getId(), book.getId(), ORDER_CNT);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, order.getStatus(), "주문 취소 시 주문 상태는 CANCEL");
        assertEquals(STOCK_CNT, book.getStockQuantity(), "주문 취소 시 재고수량 복구");
    }

    private Book createBook(String name, int PRICE, int STOCK_CNT) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(PRICE);
        book.setStockQuantity(STOCK_CNT);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }

}