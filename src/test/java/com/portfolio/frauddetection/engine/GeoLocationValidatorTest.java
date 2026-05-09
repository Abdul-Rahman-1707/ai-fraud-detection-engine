package com.portfolio.frauddetection.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class GeoLocationValidatorTest {

    private GeoLocationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GeoLocationValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"NG", "GH", "RO", "UA", "RU", "PH", "ID", "VN", "BD", "CM"})
    void isHighRiskCountry_shouldReturnTrue_forHighRiskCountries(String country) {
        assertThat(validator.isHighRiskCountry(country)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"US", "GB", "DE", "FR", "CA", "AU", "JP"})
    void isHighRiskCountry_shouldReturnFalse_forLowRiskCountries(String country) {
        assertThat(validator.isHighRiskCountry(country)).isFalse();
    }

    @Test
    void isHighRiskCountry_shouldBeCaseInsensitive() {
        assertThat(validator.isHighRiskCountry("ng")).isTrue();
        assertThat(validator.isHighRiskCountry("Ng")).isTrue();
    }

    @Test
    void isIpCountryMismatch_shouldReturnFalse_whenIpMatchesCountry() {
        assertThat(validator.isIpCountryMismatch("1.2.3.4", "US")).isFalse();
    }

    @Test
    void isIpCountryMismatch_shouldReturnTrue_whenIpDoesNotMatchCountry() {
        assertThat(validator.isIpCountryMismatch("105.112.0.1", "US")).isTrue();
    }

    @Test
    void isIpCountryMismatch_shouldReturnFalse_whenInputsAreNull() {
        assertThat(validator.isIpCountryMismatch(null, "US")).isFalse();
        assertThat(validator.isIpCountryMismatch("1.2.3.4", null)).isFalse();
    }

    @Test
    void isIpCountryMismatch_shouldReturnFalse_whenCountryNotInMap() {
        assertThat(validator.isIpCountryMismatch("1.2.3.4", "JP")).isFalse();
    }

    @Test
    void calculateGeoRisk_shouldReturnZero_forSafeTransaction() {
        double risk = validator.calculateGeoRisk("US", "1.2.3.4", "US");
        assertThat(risk).isEqualTo(0.0);
    }

    @Test
    void calculateGeoRisk_shouldIncreaseRisk_forHighRiskCountry() {
        double risk = validator.calculateGeoRisk("NG", "105.0.0.1", "NG");
        assertThat(risk).isGreaterThanOrEqualTo(0.3);
    }

    @Test
    void calculateGeoRisk_shouldIncreaseRisk_forCountryMismatchWithProfile() {
        double risk = validator.calculateGeoRisk("GB", "51.0.0.1", "US");
        assertThat(risk).isGreaterThanOrEqualTo(0.2);
    }

    @Test
    void calculateGeoRisk_shouldStackMultipleRiskFactors() {
        double risk = validator.calculateGeoRisk("NG", "1.2.3.4", "US");
        assertThat(risk).isGreaterThanOrEqualTo(0.5);
    }

    @Test
    void calculateGeoRisk_shouldCapAtOne() {
        double risk = validator.calculateGeoRisk("NG", "1.2.3.4", "GB");
        assertThat(risk).isLessThanOrEqualTo(1.0);
    }
}
