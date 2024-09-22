package sakujj.json.mapper.serializer;

import com.google.gson.GsonBuilder;
import org.example.model.Customer;
import org.example.model.Order;
import org.example.model.Product;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializerTests {

    @MethodSource
    @ParameterizedTest
    public void shouldSerializerCorrectly(Object objToSerialize) {
        // given
        String expected = new GsonBuilder()
                .serializeNulls() // Теперь gson не игнорирует нулевые значения в Map<?,?>
                .create()
                .toJson(objToSerialize);

        // when
        String actual = Serializer.serialize(objToSerialize);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    static List<Object> shouldSerializerCorrectly() {
        List<Product> products1 = new ArrayList<>(List.of(
                Product.builder()
                        .id(UUID.fromString("6179d7af-7e05-4967-94d4-854aa1e33273"))
                        .price(400.0)
                        .name("Телевизор-AA")
                        .build(),
                Product.builder()
                        .id(UUID.fromString("af388cd1-0db4-4b6d-b743-dd54b2eef9e0"))
                        .price(5500.0)
                        .name("Телевизор-XX")
                        .build()));
        products1.add(null);
        products1.add(null);

        Map<Integer, Product> unrelatedMap1 = new HashMap<>(
                Map.of(
                        54, Product.builder()
                                .id(UUID.fromString("657e6e69-287c-43a1-83ba-5cca2468da5a"))
                                .price(3000.0)
                                .name("Телеграф-XQQQQ")
                                .build()
                )
        );
        unrelatedMap1.put(0, null);
        unrelatedMap1.put(10, null);

        Customer customer1 = Customer.builder()
                .id(UUID.fromString("0c7c0228-d951-4bff-a68e-5f2b26322bba"))
                .firstName("Martin")
                .lastName("Reuben")
                .dateBirth(LocalDate.MIN)
                .orders(
                        List.of(
                                Order.builder()
                                        .id(UUID.fromString("cfbcbe60-ed4f-4d25-87bf-f0d76122ac9b"))
                                        .createDate(LocalDate.MIN)
                                        .products(
                                                products1
                                        )
                                        .unrelatedMap(
                                                unrelatedMap1
                                        )
                                        .build(),
                                Order.builder()
                                        .id(UUID.fromString("e404023d-ea10-4dea-97d8-5bbe8d523b94"))
                                        .createDate(LocalDate.MIN)
                                        .products(List.of(
                                                Product.builder()
                                                        .id(UUID.fromString("6179d7bf-7e05-4967-94d4-854aa1e33273"))
                                                        .price(300.0)
                                                        .name("Телевизор")
                                                        .build(),
                                                Product.builder()
                                                        .id(UUID.fromString("be2a5308-f8ce-419a-aa46-b3767e6ed94a"))
                                                        .price(301.0)
                                                        .name("Телефон")
                                                        .build(),
                                                Product.builder()
                                                        .id(UUID.fromString("eac43849-d373-4c72-8a10-5e9344973cc1"))
                                                        .price(3000.0)
                                                        .name("Телеграф")
                                                        .build()
                                        ))
                                        .unrelatedMap(Map.of(
                                                1, Product.builder()
                                                        .id(UUID.fromString("b41a524a-3752-4692-9f9c-f5b423b9b6f5"))
                                                        .price(302.0)
                                                        .name("Телефон-XX")
                                                        .build(),
                                                100, Product.builder()
                                                        .id(UUID.fromString("00da80aa-9963-4137-8738-6cfd41c87960"))
                                                        .price(999.0)
                                                        .name("Телефон-XZXZX")
                                                        .build()
                                        ))
                                        .build(),
                                Order.builder()
                                        .id(UUID.fromString("e404023d-ea10-4dea-97d8-5bbe8d523b94"))
                                        .createDate(LocalDate.MIN)
                                        .products(List.of())
                                        .unrelatedMap(Map.of())
                                        .build()
                        )
                )
                .build();

        return List.of(
                customer1
        );
    }
}
