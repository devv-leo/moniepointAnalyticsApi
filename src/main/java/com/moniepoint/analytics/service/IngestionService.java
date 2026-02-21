package com.moniepoint.analytics.service;

import com.moniepoint.analytics.model.Activity;
import com.moniepoint.analytics.repository.ActivityRepository;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final ActivityRepository repository;

    @Value("${app.ingestion.data-dir:./data}")
    private String dataDir;

    @PostConstruct
    public void init() {
        // So I'm kicking off the ingestion here as soon as the app wakes up.
        // It's probably better than waiting for a manual trigger since the data is
        // static.
        log.info("Starting data ingestion from {}", dataDir);
        long start = System.currentTimeMillis();

        try (Stream<Path> paths = Files.walk(Paths.get(dataDir))) {
            // I'm just walking through everything in the data folder.
            // Only grabbing .csv files because who knows what else might be in there.
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(this::processFile);
        } catch (IOException e) {
            log.error("Failed to read data directory. Make sure it exists at the root!", e);
        }

        long end = System.currentTimeMillis();
        log.info("Ingestion completed in {}ms. Total records: {}", (end - start), repository.count());
    }

    private void processFile(Path path) {
        log.info("Tackling file: {}", path.getFileName());
        List<Activity> batch = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(path.toFile()))) {
            String[] nextLine;
            reader.readNext(); // Skipping that header row, we don't need it.

            while ((nextLine = reader.readNext()) != null) {
                try {
                    Activity activity = ActivityImporter.parseRow(nextLine);
                    if (activity != null) {
                        batch.add(activity);
                    }
                } catch (Exception e) {
                    // I'm logging bad rows but not stopping.
                    // In a real scenario, one bad record shouldn't kill the whole import.
                    log.warn("Bummer! Malformed row in {}: {}. Error: {}", path.getFileName(),
                            String.join(",", nextLine), e.getMessage());
                }

                // Batching these saves us a ton of database roundtrips.
                // 1000 seems like a sweet spot between speed and memory.
                if (batch.size() >= 1000) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }
            // Don't forget the stragglers at the end!
            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }
        } catch (Exception e) {
            log.error("Total fail on file {}", path.getFileName(), e);
        }
    }
}
