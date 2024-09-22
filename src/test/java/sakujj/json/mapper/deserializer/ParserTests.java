package sakujj.json.mapper.deserializer;

import com.google.gson.Gson;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sakujj.json.mapper.deserializer.model.JsonList;
import sakujj.json.mapper.deserializer.model.JsonLiteral;
import sakujj.json.mapper.deserializer.model.JsonObject;
import sakujj.json.mapper.testmodel.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ParserTests {

    @Nested
    class getEndIndexForTerminalClassValue {
        @MethodSource
        @ParameterizedTest
        public void shouldReturnCorrectIndex(String src, int fromValue, int expected) {
            // given, when
            int actual = Parser.getEndIndexExclusiveForTerminalClassValue(src, fromValue);

            // then
            assertThat(actual).isEqualTo(expected);
        }

        static Stream<Arguments> shouldReturnCorrectIndex() {
            return Stream.of(
                    arguments("fksdj   }", 0, 5),
                    arguments("fksdj},", 2, 5),
                    arguments("fksdjdf, ", 2, 7),
                    arguments("fksdjdf}", 2, 7),
                    arguments("fksdjdf ", 2, 7),
                    arguments("fksdjdf,", 2, 7),
                    arguments("fksdjdf\t", 2, 7),
                    arguments("fksdjdf\n", 2, 7),
                    arguments("fksdjdf\r\n", 2, 7)
            );
        }

        @MethodSource
        @ParameterizedTest
        public void shouldThrowException(String src, int fromValue) {
            // given, when, then
            assertThatThrownBy(() -> Parser.getEndIndexExclusiveForTerminalClassValue(src, fromValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        static Stream<Arguments> shouldThrowException() {
            return Stream.of(
                    arguments("fksdjsdf", 0),
                    arguments("fksdjsdf", 0)
            );
        }
    }

    @Nested
    class parseJsonLiteralValue {
        @MethodSource
        @ParameterizedTest
        public void shouldParseCorrectly(String src, int fromIndex, Class<?> clazz,
                                         JsonLiteral<?> expectedLiteral, int expectedIndex) {
            // given, when
            var actual = Parser.parseJsonValue(src, fromIndex, clazz);

            // then
            assertThat(actual.getKey()).isEqualTo(expectedLiteral);
            assertThat(actual.getValue()).isEqualTo(expectedIndex);
        }

        static Stream<Arguments> shouldParseCorrectly() {
            return Stream.of(
                    arguments("234    ", 0, Integer.class, new JsonLiteral<>(234, Integer.class), 3),
                    arguments("244    ", 0, int.class, new JsonLiteral<>(244, int.class), 3),
                    arguments("   \"string\"   ", 3, String.class, new JsonLiteral<>("string", String.class), 11),
                    arguments(
                            "   \"23a294a8-c3ae-48e3-a0a7-9ae19d24ff41\"   ",
                            3,
                            UUID.class,
                            new JsonLiteral<>(
                                    UUID.fromString("23a294a8-c3ae-48e3-a0a7-9ae19d24ff41"),
                                    UUID.class
                            ),
                            41)
            );
        }

    }


    @Nested
    class parseObject {
        @MethodSource
        @ParameterizedTest
        public void shouldParseCorrectly(String src, JsonObject<?> expected) {
            // given, when
            var actual = Parser.parseJson(src, expected.getClazz());

            // then
            assertThat(actual).isEqualTo(expected);
        }

        static Stream<Arguments> shouldParseCorrectly() {
            var jsonObjClassC = new JsonObject<>(ClassC.class);
            jsonObjClassC.setProperty("integer", new JsonLiteral<>(21, Integer.class));
            jsonObjClassC.setProperty("strrr", new JsonLiteral<>("  class c info ", String.class));

            var jsonObjClassB = new JsonObject<>(ClassB.class);
            jsonObjClassB.setProperty("fieldDouble", new JsonLiteral<>(123.42323, Double.class));
            jsonObjClassB.setProperty("string", new JsonLiteral<>("some info class b ", String.class));
            jsonObjClassB.setProperty("classC", jsonObjClassC);

            var jsonObjClassA = new JsonObject<>(ClassA.class);
            jsonObjClassA.setProperty("field1", new JsonLiteral<>(662, Integer.class));
            jsonObjClassA.setProperty("uuid", new JsonLiteral<>(
                    UUID.fromString("58bb62eb-3b35-4f3e-8302-39122dc94a76"),
                    UUID.class));
            jsonObjClassA.setProperty("name", new JsonLiteral<>("xxx-name", String.class));
            jsonObjClassA.setProperty("classB", jsonObjClassB);

            return Stream.of(
                    arguments("""
                                    {
                                        "field1" : 662,
                                        "classB" : {
                                                "fieldDouble" : 123.42323,
                                                "string" : "some info class b ",
                                                "classC": {
                                                    "integer" : 21,
                                                    "strrr" : "  class c info "
                                                }
                                            },
                                        "uuid" : "58bb62eb-3b35-4f3e-8302-39122dc94a76",
                                        "name" : "xxx-name"
                                    }""",
                            jsonObjClassA
                    )

            );
        }

    }

    @Nested
    class parseCollection {
        @MethodSource
        @ParameterizedTest
        public void shouldParseCorrectly(String src, JsonList<?, ?> expected) {
            // given, when
            var actual = Parser.parseJson(src, expected.getClazz());

            // then
            assertThat(actual).isEqualTo(expected);
        }

        static Stream<Arguments> shouldParseCorrectly() {
            var jsonObjClassC1 = new JsonObject<>(ClassC.class);
            jsonObjClassC1.setProperty("integer", new JsonLiteral<>(11, Integer.class));
            jsonObjClassC1.setProperty("strrr", new JsonLiteral<>(" ss sss ", String.class));
            var jsonObjClassC2 = new JsonObject<>(ClassC.class);
            jsonObjClassC2.setProperty("integer", new JsonLiteral<>(12, Integer.class));
            jsonObjClassC2.setProperty("strrr", new JsonLiteral<>("12string ", String.class));

            var jsonClassCList = new JsonList<>(ClassC.class);
            jsonClassCList.add(jsonObjClassC1);
            jsonClassCList.add(null);
            jsonClassCList.add(jsonObjClassC2);

            var jsonClassFObject = new JsonObject<>(ClassF.class);
            jsonClassFObject.setProperty("classCList", jsonClassCList);

            var jsonClassFList1 = new JsonList<>(ClassF.class);
            jsonClassFList1.add(null);
            jsonClassFList1.add(jsonClassFObject);

            var jsonClassFList3 = new JsonList<>(ClassF.class);

            var stringList1 = new JsonList<>(String.class);
            stringList1.add(new JsonLiteral<>("[ str 1-1]", String.class));
            stringList1.add(new JsonLiteral<>(" [str 1-2] ", String.class));

            var stringList2 = new JsonList<>(String.class);
            stringList2.add(new JsonLiteral<>("[ str 2-1]", String.class));
            stringList2.add(new JsonLiteral<>(" [str 2-2] ", String.class));
            stringList2.add(new JsonLiteral<>("[str 2-3]", String.class));

            var stringList3 = new JsonList<>(String.class);
            stringList3.add(new JsonLiteral<>("[ str 3-1]", String.class));
            stringList3.add(new JsonLiteral<>(" [str 3-2] ", String.class));

            var classEObj1 = new JsonObject<>(ClassE.class);
            classEObj1.setProperty("stringList", stringList1);
            classEObj1.setProperty("classFList", jsonClassFList1);

            var classEObj2 = new JsonObject<>(ClassE.class);
            classEObj2.setProperty("stringList", stringList2);
            classEObj2.setProperty("classFList", null);

            var classEObj3 = new JsonObject<>(ClassE.class);
            classEObj3.setProperty("stringList", stringList3);
            classEObj3.setProperty("classFList", jsonClassFList3);

            var classESet2 = new JsonList<>(ClassE.class);
            classESet2.addAll(List.of(classEObj1, classEObj2, classEObj1));

            var integers2 = new JsonList<>(Integer.class);
            integers2.addAll(List.of(
                    new JsonLiteral<>(1, Integer.class),
                    new JsonLiteral<>(3, Integer.class),
                    new JsonLiteral<>(5, Integer.class),
                    new JsonLiteral<>(7, Integer.class),
                    new JsonLiteral<>(9, Integer.class)
            ));

            var classDObject1 = new JsonObject<>(ClassD.class);
            classDObject1.setProperty("classESet", null);
            classDObject1.setProperty("integers", null);

            var classDObject2 = new JsonObject<>(ClassD.class);
            classDObject2.setProperty("classESet", classESet2);
            classDObject2.setProperty("integers", integers2);

            var collection = new JsonList<>(ClassD.class);
            collection.add(classDObject1);
            collection.add(classDObject2);
            collection.add(null);

            return Stream.of(
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
                                    ]""",
                            collection
                    )

            );
        }

    }


    @Nested
    class parseFirstJsonKey {
        @MethodSource
        @ParameterizedTest
        public void shouldParseCorrectly(String src, int fromIndex,
                                         String expectedKey, int expectedIndex) {
            // given, when
            var actual = Parser.parseFirstJsonKey(src, fromIndex);

            // then
            assertThat(actual.getKey()).isEqualTo(expectedKey);
            assertThat(actual.getValue()).isEqualTo(expectedIndex);
        }

        static Stream<Arguments> shouldParseCorrectly() {
            return Stream.of(
                    arguments("      \"234\"    ", 0, "234", 11),
                    arguments("      \"fjsdfkl\"    ", 0, "fjsdfkl", 15)
            );
        }

    }

    @Nested
    class parseTerminalValue {
        @MethodSource
        @ParameterizedTest
        public void shouldParseCorrectly(String src, Class<?> clazz, JsonLiteral<?> expectedLiteral) {
            // given, when
            var actual = Parser.parseJson(src, clazz);

            // then
            assertThat(actual).isEqualTo(expectedLiteral);
        }

        static Stream<Arguments> shouldParseCorrectly() {
            return Stream.of(
                    arguments("   24324   ", Integer.class, new JsonLiteral<>(24324, Integer.class)),
                    arguments("  \"strX\"  ", String.class, new JsonLiteral<>("strX", String.class))
            );
        }
    }
}
