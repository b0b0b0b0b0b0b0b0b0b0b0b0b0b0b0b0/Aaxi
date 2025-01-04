package com.bobobo.plugins.axi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Axi extends JavaPlugin {

    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        getLogger().info("Axi плагин запущен. Эмуляция проверок мода началась.");

        // Создание конфигурации
        setupConfig();

        // Эмуляция запросов
        emulateMetaCheck();
        emulateLicenseCheck();
        emulateServerCheck("exampleServer", "localhost");
    }

    private void setupConfig() {
        try {
            // Создаём папку плагина, если она не существует
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            // Создаём конфигурационный файл, если он не существует
            configFile = new File(getDataFolder(), "config.yml");

            if (!configFile.exists()) {
                getLogger().info("Конфигурационный файл не найден, создаю новый...");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                    writer.write("uuid: " + UUID.randomUUID().toString());
                    writer.newLine();
                }
            }

            // Загружаем конфигурацию
            config = YamlConfiguration.loadConfiguration(configFile);

            // Проверяем, есть ли UUID
            if (!config.contains("uuid")) {
                UUID generatedUUID = UUID.randomUUID();
                config.set("uuid", generatedUUID.toString());
                config.save(configFile);
                getLogger().info("Сгенерирован новый UUID: " + generatedUUID);
            } else {
                getLogger().info("Найден UUID: " + config.getString("uuid"));
            }
        } catch (Exception e) {
            getLogger().severe("Ошибка при создании конфигурации: " + e.getMessage());
        }
    }

    private String getUUID() {
        return config.getString("uuid", UUID.randomUUID().toString());
    }

    private void logRequestDetails(HttpURLConnection conn) {
        try {
            getLogger().info("URL запроса: " + conn.getURL().toString());
            getLogger().info("Метод запроса: " + conn.getRequestMethod());
            getLogger().info("Заголовки запроса:");
            conn.getRequestProperties().forEach((key, value) -> getLogger().info(key + ": " + String.join(", ", value)));
        } catch (Exception e) {
            getLogger().severe("Ошибка при логировании запроса: " + e.getMessage());
        }
    }

    private void logResponseDetails(HttpURLConnection conn) {
        try {
            getLogger().info("Заголовки ответа:");
            for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
                getLogger().info(entry.getKey() + ": " + String.join(", ", entry.getValue()));
            }
        } catch (Exception e) {
            getLogger().severe("Ошибка при логировании ответа: " + e.getMessage());
        }
    }

    private void decodeAndLogJWT(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                getLogger().info("Дешифрованный JWT: " + payload);
            } else {
                getLogger().warning("Неверный формат JWT: " + jwt);
            }
        } catch (Exception e) {
            getLogger().severe("Ошибка при дешифровке JWT: " + e.getMessage());
        }
    }

    private void emulateMetaCheck() {
        new Thread(() -> {
            try {
                URL url = new URL("https://axiom.moulberry.com/api/mcauth/meta");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Axiom/4.4.0");
                conn.setRequestMethod("GET");

                logRequestDetails(conn);

                int responseCode = conn.getResponseCode();
                getLogger().info("Meta Check - Код ответа: " + responseCode);

                logResponseDetails(conn);

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    String response = in.lines().reduce("", (acc, line) -> acc + line + "\n");
                    getLogger().info("Meta Check - Ответ: " + response);
                    in.close();
                }
                conn.disconnect();
            } catch (Exception e) {
                getLogger().severe("Ошибка при эмуляции meta запроса: " + e.getMessage());
            }
        }).start();
    }

    private void emulateLicenseCheck() {
        new Thread(() -> {
            try {
                String uuid = getUUID();
                URL url = new URL("https://axiom.moulberry.com/api/mcauth/has_commercial_license?uuid=" + uuid);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Axiom/4.4.0");
                conn.setRequestMethod("GET");

                logRequestDetails(conn);

                int responseCode = conn.getResponseCode();
                getLogger().info("License Check - Код ответа: " + responseCode);

                logResponseDetails(conn);

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    String response = in.lines().reduce("", (acc, line) -> acc + line + "\n");
                    getLogger().info("License Check - Ответ: " + response);
                    decodeAndLogJWT(response);
                    in.close();
                }
                conn.disconnect();
            } catch (Exception e) {
                getLogger().severe("Ошибка при эмуляции проверки лицензии: " + e.getMessage());
            }
        }).start();
    }

    private void emulateServerCheck(String server, String host) {
        new Thread(() -> {
            try {
                String uuid = getUUID();
                URL url = new URL("https://axiom.moulberry.com/api/mcauth/connect?uuid=" + uuid + "&server=" + server + "&host=" + host);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Axiom/4.4.0");
                conn.setRequestMethod("GET");

                logRequestDetails(conn);

                int responseCode = conn.getResponseCode();
                getLogger().info("Server Check - Код ответа: " + responseCode);

                logResponseDetails(conn);

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    String response = in.lines().reduce("", (acc, line) -> acc + line + "\n");
                    getLogger().info("Server Check - Ответ: " + response);
                    decodeAndLogJWT(response);
                    in.close();
                }
                conn.disconnect();
            } catch (Exception e) {
                getLogger().severe("Ошибка при эмуляции проверки сервера: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onDisable() {
        getLogger().info("Axi плагин выключен.");
    }
}
