package tech.grove.birch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.Range;
import tech.grove.birch.delegates.ThrowingFunction;
import tech.grove.birch.serialization.FluentMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Test {

    public static void main(String[] args) {


        var instance = new TestRecord(1, "afsdasd");
        var list     = new ArrayList<TestRecord>();

        for (int i = 0; i < 1000000; i++) {
            list.add(new TestRecord(i, String.valueOf(i)));
        }

        var mapper = new ObjectMapper();
//
//        test("Warmup: ", list, x -> mapper.readValue(mapper.writeValueAsString(x), TestRecord.class));
//
//        test("Warmup: ", list, x -> FluentMapper.instance(x).asJson().asType(TestRecord.class).get());
//
//        test("Native: ", list, x -> mapper.readValue(mapper.writeValueAsString(x), TestRecord.class));

        while(true){
//        test("Native: ", list, x -> mapper.readValue(mapper.writeValueAsString(x), TestRecord.class));
            test("Fluent: ", list, x -> FluentMapper.instance(x).asJson().asType(TestRecord.class).get());
        }
    }

    private static void test(String mode, List<TestRecord> input, ThrowingFunction<TestRecord, TestRecord, IOException> transformer) {
        var timestamp = System.currentTimeMillis();

        for (int i = 0; i < input.size(); i++) {
            try {
                input.set(i, transformer.apply(input.get(i)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println(mode + " took " + Duration.ofMillis(System.currentTimeMillis() - timestamp));
    }

    private record TestRecord(int index, String name, List<UUID> uuids) {
        public TestRecord(int index, String name){
            this(index, name, IntStream.iterate(0,x->x+1).limit(10).boxed().map(x->UUID.randomUUID()).toList());
        }
    }
}
