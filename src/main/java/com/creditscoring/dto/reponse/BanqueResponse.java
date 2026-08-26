package com.creditscoring.dto.reponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanqueResponse {

    private Long id;
    private String nom;
    private String codeBanque;
    private String adresse;
    private String telephone;
    private String email;
    private String siteWeb;
}