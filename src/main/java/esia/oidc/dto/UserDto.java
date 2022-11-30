package esia.oidc.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserDto {
    private Identity identity;
    private User user;
    private List<Companies> companies;

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    private static class Identity {
        private Integer id;
        private Integer userId;
        private String provider;
        private String uid;
        private String email;
        private Boolean allowPasswordChange;
        private String createdAt;
        private String updatedAt;
    }

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    private static class User {
        private Integer id;
        private String email;
        private String createdAt;
        private String updatedAt;
        private String lastName;
        private String firstName;
        private String patronymic;
        private String phone;
        private Integer counterpartyId;
        private String esiaOid;
        private String esiaSnils;
        private String esiaFirstName;
        private String esiaMiddleName;
        private String esiaLastName;
        private String esiaEmail;
        private String esiaMobile;
        private String esiaBirthdate;
        private String esiaBirthplace;
        private String esiaGender;
        private Boolean esiaTrusted;
        private String esiaCitizenship;
        private String esiaInn;
        private String esiaUpdatedOn;
        private String esiaStatus;
        private Boolean esiaVerifying;
        private EsiaInfo esiaInfo;
        private EsiaCredentials esiaCredentials;
        private EsiaExtra esiaExtra;

        @Getter
        @Setter
        @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
        private static class EsiaInfo {
            private String email;
            private String snils;
            private String lastName;
            private String firstName;
            private String middleName;
        }

        @Getter
        @Setter
        @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
        private static class EsiaCredentials {
            private String token;
            private Boolean expires;
            private Integer expiresAt;
            private String refreshToken;
        }

        @Getter
        @Setter
        @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
        private static class EsiaExtra {
            private RawInfo rawInfo;

            @Getter
            @Setter
            private static class RawInfo {
                private String inn;
                private String eTag;
                private String email;
                private String snils;
                private String gender;
                private String mobile;
                private Integer rIdDoc;
                private String status;
                private Boolean trusted;
                private String lastName;
                private String birthDate;
                private String firstName;
                private Integer updateOn;
                private Boolean verifying;
                private String birthPlace;
                private String middleName;
                private List<String> stateFacts;
                private String citizenship;
                private Boolean containsUpCfmCode;
                private Boolean rfgUOperatorCheck;
            }
        }
    }

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    private static class Companies {
        private String id;
        private String esiaKind;
        private String esiaOid;
        private String esiaFullName;
        private String esiaShortName;
        private String discardedAt;
        private String createdAt;
        private String updatedAt;
        private String inn;
        private String kpp;
        private String legalAddress;
        private String source;
        private String state;
        private String lastValidationLogId;
        private String displayName;
        private String kind;
        private String logData;
    }
}
