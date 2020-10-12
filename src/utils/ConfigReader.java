package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ConfigReader {
    public static List<String> importNames(String file_path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        List<String> available_names = new ArrayList<>();

        String name = br.readLine();
        while (name != null) {
            available_names.add(name);
            name = br.readLine();
        }

        br.close();
        return available_names;
    }

    public static Queue<String> importRoles(String file_path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        Queue<String> roles = new LinkedList<>();

        String line = br.readLine();
        while (line != null) {
            int colon_index =  line.indexOf(':');
            if(colon_index != -1) {
                String[] line_words = line.split(":");

                String role = line_words[0];
                int n_players_role = Integer.parseInt(line_words[1]);

                while (n_players_role != 0) {
                    roles.add(role);
                    n_players_role--;
                }
                line = br.readLine();
            }
        }

        return roles;
    }
}
