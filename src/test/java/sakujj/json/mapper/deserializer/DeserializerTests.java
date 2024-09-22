package sakujj.json.mapper.deserializer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.example.model.Customer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sakujj.json.mapper.testmodel.ClassC;
import sakujj.json.mapper.testmodel.ClassD;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class DeserializerTests {

    @Nested
    class deserializeObject {

        @MethodSource
        @ParameterizedTest
        public void shouldDeserializeCorrectly(String json, Class<?> clazz) {
            // given
            Object expected = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new JsonDeserializer<>() {
                        @Override
                        public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                            return LocalDate.parse(json.getAsJsonPrimitive().getAsString());
                        }
                    })
                    .create().fromJson(json, clazz);

            // when
            Object actual = Deserializer.deserialize(json, clazz);

            // then
            assertThat(actual).isEqualTo(expected);

        }

        static Stream<Arguments> shouldDeserializeCorrectly() {
            return Stream.of(
                    arguments("""
                            {
                              "integers" : [
                                1, null, 2, 3
                              ],
                                                        
                              "classESet" : [
                                {
                                  "stringList" : ["1x1", "1x2", "1x3", null],
                                  "classFList" : [
                                    {
                                      "classCList" : [
                                        {
                                          "integer": null,
                                          "strrr" : "11"
                                        },
                                        null,
                                        null,
                                        {
                                          "integer": 12,
                                          "strrr" : null
                                        }
                                      ]
                                    }
                                  ]
                                },
                                {
                                  "stringList" : ["2x1", "2x2"],
                                  "classFList" : []
                                }
                              ]
                            }
                            """, ClassD.class),
                    arguments("""
                            {
                                "id": "44ca438a-ce74-4a78-8f4d-f7e0a80740d7",
                                "firstName": "Reuben",
                                "lastName": "Martin",
                                "dateBirth": "2003-11-03",
                                "orders": [
                                    {
                                        "id": "25f6836f-0f32-4d04-9d8d-e4f7fb042f38",
                                        "products": [
                                            {
                                                "id": "46e845d5-06dd-4cbd-8da6-c1da6cad26c7",
                                                "name": "Телефон",
                                                "price":  100.0
                                            },
                                            {
                                                "id": "9f1695f7-3305-40b5-92d6-f3c646bc8f22",
                                                "name": "Машина",
                                                "price":  100.0
                                            }
                                        ],
                                        "createDate": "2023-10-24",
                                        "unrelatedMap": null
                                    }
                                ]
                            }
                            """, Customer.class)
            );
        }
    }

    @Nested
    class deserializeCollection {

        @MethodSource
        @ParameterizedTest
        public void shouldDeserializeCorrectly(String json,
                                               Class<?> typeArgumentClazz,
                                               Class<?> collectionClazz,
                                               Type typeForGson) {
            // given
            Object expected = new Gson().fromJson(json, typeForGson);

            // when
            Object actual = Deserializer.deserializeCollection(json, typeArgumentClazz, collectionClazz);

            // then
            assertThat(actual).isEqualTo(expected);
        }

        static Stream<Arguments> shouldDeserializeCorrectly() {
            return Stream.of(
                    arguments("""
                                    [
                                    1,
                                    2,
                                    3
                                    ]
                                    """, Integer.class, List.class,
                            new TypeToken<List<Integer>>(){}.getType()),
                    arguments("""
                                    [
                                    5,
                                    2,
                                    3
                                    ]
                                    """, Integer.class, Set.class,
                            new TypeToken<Set<Integer>>(){}.getType()),
                    arguments("""
                                    [
                                    {
                                        "integer" : 1,
                                        "strrr" : "1"
                                    },
                                    {
                                        "integer" : 2,
                                        "strrr" : "2"
                                    },
                                    {
                                        "integer" : 3,
                                        "strrr" : "3"
                                    }
                                    ]
                                    """, ClassC.class, Set.class,
                            new TypeToken<Set<ClassC>>(){}.getType()),
                    arguments("""
                            [
                                {
                                    "classESet": null,
                                    "integers" : null
                                },
                                {
                                    "classESet": [
                                        {
                                            "stringList": [
                                            "[ str 1-1]",
                                            " [str 1-2] "
                                            ],
                                            "classFList": [
                                                null,
                                                {
                                                    "classCList" : [
                                                        {
                                                            "integer" : 11,
                                                            "strrr": " ss sss "
                                                        },
                                                        null,
                                                        {
                                                            "integer" : 12,
                                                            "strrr": "12string "
                                                        }
                                                    ]
                                                }
                                            ]
                                        },
                                        {
                                            "stringList": [
                                                "[ str 2-1]",
                                                " [str 2-2] ",
                                                "[str 2-3]"
                                            ],
                                            "classFList": null
                                        },
                                        {
                                            "stringList": [
                                                "[ str 3-1]",
                                                " [str 3-2] "
                                            ],
                                            "classFList": []
                                        }
                                    ],
                                    "integers" : [1, 3, 5, 7, 9]
                                },
                                null
                            ]""", ClassD.class, List.class,
                            new TypeToken<List<ClassD>>(){}.getType()
                            )
            );
        }
    }

    @Nested
    class deserializeLiteral {

        @MethodSource
        @ParameterizedTest
        public void shouldDeserializeCorrectly(String json, Class<?> clazz) {
            // given
            Object expected = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new JsonDeserializer<>() {
                        @Override
                        public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                            return LocalDate.parse(json.getAsJsonPrimitive().getAsString());
                        }
                    })
                    .create()
                    .fromJson(json, clazz);

            // when
            Object actual = Deserializer.deserialize(json, clazz);

            // then
            assertThat(actual).isEqualTo(expected);

        }

        static Stream<Arguments> shouldDeserializeCorrectly() {
            return Stream.of(
                    arguments("   2  ", Integer.class),
                    arguments("   \"2007-11-16\" ", LocalDate.class),
                    arguments("   null  ", Integer.class, null),
                    arguments("   \"hello world\" ", String.class),
                    arguments("  \"44ca438a-ce74-4a78-8f4d-f7e0a80740d7\" ", UUID.class)
            );
        }
    }
}
