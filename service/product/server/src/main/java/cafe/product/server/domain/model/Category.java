package cafe.product.server.domain.model;

import cafe.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "P_CATEGORY")
@Getter
@NoArgsConstructor
@SQLRestriction("id_deleted = false")
@SQLDelete(sql = "UPDATE p_category SET is_deleted = true where category_id = ?")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)   // N + 1 문제 완화
    private List<Category> subCategories = new ArrayList<>();

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    public void update (String name, Category category) {
        this.name = name;
        this.parent = category;
    }

    public void addSubCategory(Category subCategory) { this.subCategories.add(subCategory); }

    public void removeSubCategory(Category subCategory) { this.subCategories.remove(subCategory); }
}
