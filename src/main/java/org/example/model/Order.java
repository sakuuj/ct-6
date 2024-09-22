package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class Order {
    private UUID id;
    private LocalDate createDate;
    private Map<Integer, Product> unrelatedMap;
    private List<Product> products;
}
