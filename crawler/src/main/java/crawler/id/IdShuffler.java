package crawler.id;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static crawler.Utils.writeJsonFile;

public class IdShuffler {

    private static final String[] idFiles = new String[] {
            "harvested-ids/ids_0612890da35a4487889703d83b19d74b.json",
            "harvested-ids/ids_07d3d844f85a4557aaf4ea78f1b130aa.json",
            "harvested-ids/ids_0b1865d5f51644209028ef4ab5a6d161.json",
            "harvested-ids/ids_0c8588ad7ace4d21910a83dc9a280fd8.json",
            "harvested-ids/ids_1977b081859d453ebdff2f39cbda04e5.json",
            "harvested-ids/ids_29b023f6d1fd46db97ef8a0767589bcb.json",
            "harvested-ids/ids_5538d435231846d38910054a44930194.json",
            "harvested-ids/ids_583790a333e149968c24a6ac99956cc0.json",
            "harvested-ids/ids_5f39ffa74c3c4d0a9945b42f159fc1a8.json",
            "harvested-ids/ids_64b5756deaa64660a09a8701b8400d2b.json",
            "harvested-ids/ids_7739c38808234a70a9bcd80ee993ce29.json",
            "harvested-ids/ids_9c397105c95843518e60e1c318cb877c.json",
            "harvested-ids/ids_a9020fee595c49dda8d86d32805f6cc6.json",
            "harvested-ids/ids_bd6cd404884e41dab48fe45a6267ffd8.json",
            "harvested-ids/ids_c5de28e083e0412f9472fd602e06236e.json",
            "harvested-ids/ids_debb69f3c8694c2e9a1c797ea50d29c1.json",
            "harvested-ids/ids_e58a48714f6641adbf58a1fe16bbd4f7.json"
    };
    public static void main(String[] args) {
        final IdShuffler shuffler = new IdShuffler();
        final Set<String> ids = new HashSet<>();
        Stream.of(idFiles).map(shuffler::getIdsFromFile).forEach(ids::addAll);
        System.out.println(ids.size());
        final List<String> list = new ArrayList<>(ids);
        Collections.shuffle(list);
        final Map<Integer, List<String>> groups = list.stream().collect(Collectors.groupingBy(id -> Integer.parseInt(id) % 6));
        final List<List<String>> partitions = new ArrayList<>(groups.values());
        final String[] members = new String[] { "lisa", "kyle", "jared", "david", "christian", "nardos" };
        IntStream.range(0, 6).forEach(i -> writeJsonFile(members[i], partitions.get(i)));
    }
    private Set<String> getIdsFromFile(final String fileName) {
        Set<String> ids = new HashSet<>();
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if(Objects.nonNull(inputStream)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final Gson gson = new Gson();
            ids = gson.fromJson(reader, new TypeToken<Set<String>>() {}.getType());
        }
        return ids;
    }
}
