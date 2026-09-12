package com.campus.lostfound.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
// Table name "matches" ("match" can clash with SQL keywords).
// The unique constraint prevents duplicate Match rows for the same pair
// if the matching engine runs more than once.
@Table(name = "matches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lost_item_id", "found_item_id"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private Item lostItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id", nullable = false)
    private Item foundItem;

    // The score produced by the rule-based matching engine.
    @Column(nullable = false)
    private int score;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}