package com.portfolio.frauddetection.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class GeoLocationValidator {

    private static final Map<String, Set<String>> COUNTRY_IP_PREFIXES = Map.of(
            "US", Set.of("1.", "2.", "3.", "4.", "5.", "6.", "7.", "8."),
            "GB", Set.of("51.", "52.", "53."),
            "DE", Set.of("46.", "80.", "81."),
            "IN", Set.of("14.", "27.", "43.", "49."),
            "NG", Set.of("41.", "105.", "154.")
    );

    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of(
            "NG", "GH", "CM", "RO", "UA", "RU", "PH", "ID", "VN", "BD"
    );

    public boolean isHighRiskCountry(String countryCode) {
        return HIGH_RISK_COUNTRIES.contains(countryCode.toUpperCase());
    }

    public boolean isIpCountryMismatch(String ipAddress, String declaredCountry) {
        if (ipAddress == null || declaredCountry == null) return false;

        Set<String> expectedPrefixes = COUNTRY_IP_PREFIXES.get(declaredCountry.toUpperCase());
        if (expectedPrefixes == null) return false;

        boolean matches = expectedPrefixes.stream().anyMatch(ipAddress::startsWith);
        if (!matches) {
            log.warn("IP-country mismatch: IP {} does not match declared country {}", ipAddress, declaredCountry);
        }
        return !matches;
    }

    public double calculateGeoRisk(String country, String ipAddress, String userPrimaryCountry) {
        double risk = 0.0;

        if (isHighRiskCountry(country)) {
            risk += 0.3;
        }

        if (userPrimaryCountry != null && !country.equalsIgnoreCase(userPrimaryCountry)) {
            risk += 0.2;
        }

        if (isIpCountryMismatch(ipAddress, country)) {
            risk += 0.25;
        }

        return Math.min(risk, 1.0);
    }
}
