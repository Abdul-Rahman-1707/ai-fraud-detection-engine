package com.portfolio.frauddetection.service;

import com.portfolio.frauddetection.model.*;
import com.portfolio.frauddetection.repository.TransactionRepository;
import com.portfolio.frauddetection.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@Profile("demo")
@RequiredArgsConstructor
@Slf4j
public class DataSeederService implements CommandLineRunner {

    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;
    private final Random random = new Random(42);

    private static final List<String> MERCHANTS = List.of(
            "Amazon", "Walmart", "Target", "Best Buy", "Apple Store",
            "Shell Gas", "Uber", "DoorDash", "Netflix", "Spotify"
    );

    private static final List<String> CATEGORIES = List.of(
            "RETAIL", "GROCERY", "ELECTRONICS", "FUEL", "TRANSPORT",
            "FOOD_DELIVERY", "ENTERTAINMENT", "SUBSCRIPTION"
    );

    private static final List<String> COUNTRIES = List.of("US", "US", "US", "GB", "DE", "NG", "IN", "RU");
    private static final List<String> CITIES = List.of(
            "New York", "Los Angeles", "Chicago", "London", "Berlin",
            "Lagos", "Mumbai", "Moscow"
    );

    @Override
    public void run(String... args) {
        if (transactionRepository.count() > 0) {
            log.info("Data already seeded, skipping...");
            return;
        }

        log.info("Seeding demo data...");
        seedUserProfiles();
        seedTransactions();
        log.info("Demo data seeded successfully");
    }

    private void seedUserProfiles() {
        for (int i = 1; i <= 10; i++) {
            UserProfile profile = UserProfile.builder()
                    .userId("user-" + String.format("%03d", i))
                    .avgTransactionAmount(randomAmount(50, 300))
                    .maxTransactionAmount(randomAmount(500, 2000))
                    .avgDailyTransactionCount(random.nextInt(5) + 1)
                    .primaryCountry("US")
                    .primaryCity(CITIES.get(random.nextInt(3)))
                    .commonMerchantCategories("RETAIL,GROCERY,ENTERTAINMENT")
                    .knownDeviceIds("device-" + i + "-a,device-" + i + "-b")
                    .knownIpAddresses("192.168.1." + i)
                    .totalTransactions((long) (random.nextInt(500) + 100))
                    .flaggedTransactions((long) random.nextInt(5))
                    .updatedAt(LocalDateTime.now())
                    .build();
            userProfileRepository.save(profile);
        }
        log.info("Seeded 10 user profiles");
    }

    private void seedTransactions() {
        int totalSeeded = 0;
        for (int day = 30; day >= 0; day--) {
            int txnsPerDay = random.nextInt(8) + 3;
            for (int j = 0; j < txnsPerDay; j++) {
                String userId = "user-" + String.format("%03d", random.nextInt(10) + 1);
                int countryIdx = random.nextInt(COUNTRIES.size());
                boolean isSuspicious = random.nextDouble() < 0.15;

                BigDecimal amount = isSuspicious ? randomAmount(3000, 15000) : randomAmount(10, 500);
                double fraudScore = isSuspicious ? 0.5 + random.nextDouble() * 0.5 : random.nextDouble() * 0.3;

                Transaction txn = Transaction.builder()
                        .userId(userId)
                        .amount(amount)
                        .currency("USD")
                        .merchantName(MERCHANTS.get(random.nextInt(MERCHANTS.size())))
                        .merchantCategory(CATEGORIES.get(random.nextInt(CATEGORIES.size())))
                        .cardLast4(String.format("%04d", random.nextInt(10000)))
                        .country(COUNTRIES.get(countryIdx))
                        .city(CITIES.get(countryIdx))
                        .ipAddress("192.168." + random.nextInt(255) + "." + random.nextInt(255))
                        .deviceId("device-" + (random.nextInt(10) + 1) + "-" + (random.nextBoolean() ? "a" : "c"))
                        .timestamp(LocalDateTime.now().minusDays(day).withHour(random.nextInt(24)).withMinute(random.nextInt(60)))
                        .type(TransactionType.PURCHASE)
                        .status(isSuspicious ? TransactionStatus.BLOCKED : TransactionStatus.APPROVED)
                        .fraudScore(fraudScore)
                        .fraudReason(isSuspicious ? "HIGH_AMOUNT;GEO_ANOMALY" : null)
                        .createdAt(LocalDateTime.now().minusDays(day))
                        .build();
                transactionRepository.save(txn);
                totalSeeded++;
            }
        }
        log.info("Seeded {} transactions across 30 days", totalSeeded);
    }

    private BigDecimal randomAmount(double min, double max) {
        double amount = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
