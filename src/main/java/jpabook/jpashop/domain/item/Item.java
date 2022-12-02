package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<Category>();

    /**
     * 재고 증가
     */
    public void addStock(int q) {
        this.stockQuantity += q;
    }

    /**
     * 재고 감소
     */
    public void removeStock(int q) throws NotEnoughStockException {
        int resetStock = this.stockQuantity - q;
        if (resetStock < 0) {
            throw new NotEnoughStockException("Need more stock");
        }
        this.stockQuantity -= q;
    }
}
