package com.creditscoring.service;

import com.creditscoring.entity.*;
import com.creditscoring.enums.*;
import com.creditscoring.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final UtilisateurRepository utilisateurRepository;
    private final DemandeCreditRepository demandeCreditRepository;
    private final ContratRepository contratRepository;
    private final EcheanceRepository echeanceRepository;
    private final PaiementRepository paiementRepository;

    @Override
    public String repondre(String message, String email) {

        if (message == null || message.trim().isEmpty()) {
            return "Veuillez écrire votre question.";
        }

        Optional<Utilisateur> utilisateurOpt =
                utilisateurRepository.findByEmail(email);

        if (utilisateurOpt.isEmpty()) {
            return "Utilisateur introuvable.";
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        String role = utilisateur.getRole() != null
                ? utilisateur.getRole().getNom().toUpperCase(Locale.ROOT)
                : "";

        String question = normaliser(message);

        if (estSalutation(question)) {
            return "Bonjour " + utilisateur.getPrenom()
                    + " ! 👋 Je suis l'assistant CREDITNOVA. "
                    + "Je peux vous aider avec vos demandes de crédit, "
                    + "contrats, échéances, paiements, simulations et statistiques.";
        }

        if (question.contains("merci")) {
            return "Avec plaisir ! 😊";
        }

        switch (role) {

            case "CLIENT":
                return traiterClient(question, (Client) utilisateur);

            case "CONSEILLER":
                return traiterConseiller(question, (Conseiller) utilisateur);

            case "RESPONSABLE_CREDIT":
                return traiterResponsable(question,
                        (ResponsableCredit) utilisateur);

            case "ADMIN":
                return traiterAdmin(question);

            default:
                return "Votre rôle n'est pas reconnu par l'assistant.";
        }
    }

    // =========================================================
    // CLIENT
    // =========================================================

    private String traiterClient(String question, Client client) {

        List<DemandeCredit> demandes =
                demandeCreditRepository.findByClientId(client.getId());

        // -----------------------------------------------------
        // PROFIL
        // -----------------------------------------------------

        if (contient(question, "mon profil", "mes informations",
                "mes infos", "qui suis-je")) {

            return "Voici vos informations :\n"
                    + "Nom : " + client.getNom() + " "
                    + client.getPrenom() + "\n"
                    + "Email : " + client.getEmail() + "\n"
                    + "Profession : " + valeur(client.getProfession()) + "\n"
                    + "Revenu mensuel : "
                    + formatMontant(client.getRevenuMensuel()) + "\n"
                    + "Dettes existantes : "
                    + formatMontant(client.getDettesExistantes()) + "\n"
                    + "Ancienneté emploi : "
                    + valeur(client.getAncienneteEmploi()) + " ans\n"
                    + "Personnes à charge : "
                    + valeur(client.getNombrePersonnesACharge());
        }

        // -----------------------------------------------------
        // NOMBRE DE DEMANDES
        // -----------------------------------------------------

        if (contient(question,
                "combien de demandes",
                "nombre de demandes",
                "mes demandes")) {

            return "Vous avez actuellement "
                    + demandes.size()
                    + " demande(s) de crédit.";
        }

        // -----------------------------------------------------
        // DERNIERE DEMANDE
        // -----------------------------------------------------

        if (contient(question,
                "derniere demande",
                "dernière demande",
                "ma derniere demande",
                "ma dernière demande")) {

            if (demandes.isEmpty()) {
                return "Vous n'avez aucune demande de crédit.";
            }

            DemandeCredit derniere = demandes.stream()
                    .filter(d -> d.getDateDemande() != null)
                    .max((d1, d2) ->
                            d1.getDateDemande()
                                    .compareTo(d2.getDateDemande()))
                    .orElse(demandes.get(demandes.size() - 1));

            return detailsDemande(derniere);
        }

        // -----------------------------------------------------
        // STATUT
        // -----------------------------------------------------

        if (contient(question,
                "statut",
                "etat",
                "état",
                "ou en est",
                "où en est")) {

            if (demandes.isEmpty()) {
                return "Vous n'avez aucune demande de crédit.";
            }

            StringBuilder response =
                    new StringBuilder("Voici le statut de vos demandes :\n\n");

            for (DemandeCredit d : demandes) {

                response.append("Demande #")
                        .append(d.getId())
                        .append(" → ")
                        .append(d.getStatut())
                        .append("\n");
            }

            return response.toString();
        }

        // -----------------------------------------------------
        // SCORE
        // -----------------------------------------------------

        if (contient(question,
                "score",
                "scoring",
                "score de confiance")) {

            if (client.getScoreConfiance() == null) {
                return "Votre score de confiance n'est pas encore disponible.";
            }

            return "Votre score de confiance est : "
                    + client.getScoreConfiance();
        }

        // -----------------------------------------------------
        // DEMANDES REFUSEES
        // -----------------------------------------------------

        if (contient(question,
                "refuse",
                "refusée",
                "refusee",
                "pourquoi refus")) {

            List<DemandeCredit> refusees = demandes.stream()
                    .filter(d -> d.getStatut() == StatutDemande.REFUSEE)
                    .toList();

            if (refusees.isEmpty()) {
                return "Vous n'avez actuellement aucune demande refusée.";
            }

            StringBuilder response =
                    new StringBuilder("Vos demandes refusées :\n\n");

            for (DemandeCredit d : refusees) {

                response.append("Demande #")
                        .append(d.getId())
                        .append("\nMotif : ")
                        .append(valeur(d.getMotifRefus()))
                        .append("\n\n");
            }

            return response.toString();
        }

        // -----------------------------------------------------
        // CONTRATS
        // -----------------------------------------------------

        if (contient(question,
                "contrat",
                "mes contrats")) {

            List<Contrat> contrats =
                    contratsClient(client);

            if (contrats.isEmpty()) {
                return "Vous n'avez actuellement aucun contrat.";
            }

            StringBuilder response =
                    new StringBuilder("Voici vos contrats :\n\n");

            for (Contrat c : contrats) {

                response.append("Contrat #")
                        .append(c.getId())
                        .append("\nNuméro : ")
                        .append(valeur(c.getNumeroContrat()))
                        .append("\nMontant : ")
                        .append(formatMontant(c.getMontant()))
                        .append("\nMensualité : ")
                        .append(formatMontant(c.getMensualite()))
                        .append("\nStatut : ")
                        .append(c.getStatut())
                        .append("\n\n");
            }

            return response.toString();
        }

        // -----------------------------------------------------
        // ECHEANCES
        // -----------------------------------------------------

        if (contient(question,
                "echeance",
                "échéance",
                "mensualite",
                "mensualité",
                "prochaine echeance",
                "prochaine échéance")) {

            List<Contrat> contrats =
                    contratsClient(client);

            if (contrats.isEmpty()) {
                return "Vous n'avez aucun contrat avec des échéances.";
            }

            StringBuilder response =
                    new StringBuilder("Voici vos prochaines échéances :\n\n");

            for (Contrat contrat : contrats) {

                List<Echeance> echeances =
                        echeanceRepository.findByContratId(contrat.getId());

                Optional<Echeance> prochaine =
                        echeances.stream()
                                .filter(e ->
                                        e.getDateEcheance() != null
                                                && e.getDateEcheance()
                                                .compareTo(LocalDate.now()) >= 0
                                                && e.getStatut()
                                                != StatutEcheance.PAYEE)
                                .min((e1, e2) ->
                                        e1.getDateEcheance()
                                                .compareTo(
                                                        e2.getDateEcheance()));

                if (prochaine.isPresent()) {

                    Echeance e = prochaine.get();

                    response.append("Contrat #")
                            .append(contrat.getId())
                            .append("\n")
                            .append("Échéance #")
                            .append(e.getNumero())
                            .append("\n")
                            .append("Montant : ")
                            .append(formatMontant(e.getMontant()))
                            .append("\n")
                            .append("Date : ")
                            .append(e.getDateEcheance())
                            .append("\n")
                            .append("Statut : ")
                            .append(e.getStatut())
                            .append("\n\n");
                }
            }

            return response.toString();
        }

        // -----------------------------------------------------
        // PAIEMENTS
        // -----------------------------------------------------

        if (contient(question,
                "paiement",
                "paiements",
                "payement",
                "payements")) {

            List<Contrat> contrats =
                    contratsClient(client);

            if (contrats.isEmpty()) {
                return "Vous n'avez aucun contrat.";
            }

            StringBuilder response =
                    new StringBuilder("Vos paiements :\n\n");

            boolean trouve = false;

            for (Contrat contrat : contrats) {

                List<Paiement> paiements =
                        paiementRepository
                                .findByContratId(contrat.getId());

                for (Paiement p : paiements) {

                    trouve = true;

                    response.append("Paiement #")
                            .append(p.getId())
                            .append("\nMontant : ")
                            .append(formatMontant(p.getMontant()))
                            .append("\nDate : ")
                            .append(valeur(p.getDatePaiement()))
                            .append("\nMéthode : ")
                            .append(valeur(p.getMethodePaiement()))
                            .append("\nStatut : ")
                            .append(p.getStatut())
                            .append("\n\n");
                }
            }

            if (!trouve) {
                return "Aucun paiement enregistré.";
            }

            return response.toString();
        }

        // -----------------------------------------------------
        // DOCUMENTS
        // -----------------------------------------------------

        if (contient(question,
                "document",
                "documents",
                "piece",
                "pièce",
                "pieces",
                "pièces")) {

            if (demandes.isEmpty()) {
                return "Vous n'avez aucune demande associée.";
            }

            int totalDocuments = demandes.stream()
                    .mapToInt(d ->
                            d.getDocuments() == null
                                    ? 0
                                    : d.getDocuments().size())
                    .sum();

            return "Vos demandes contiennent actuellement "
                    + totalDocuments
                    + " document(s) enregistré(s).";
        }

        return "Je peux vous aider avec :\n"
                + "• vos demandes de crédit\n"
                + "• le statut de vos demandes\n"
                + "• votre score\n"
                + "• vos contrats\n"
                + "• vos échéances\n"
                + "• vos paiements\n"
                + "• vos documents\n\n"
                + "Posez-moi votre question naturellement.";
    }

    // =========================================================
    // CONSEILLER
    // =========================================================

    private String traiterConseiller(
            String question,
            Conseiller conseiller) {

        List<DemandeCredit> demandes =
                demandeCreditRepository
                        .findByConseillerId(conseiller.getId());

        if (contient(question,
                "mes demandes",
                "demandes affectees",
                "demandes affectées")) {

            if (demandes.isEmpty()) {
                return "Aucune demande ne vous est actuellement affectée.";
            }

            StringBuilder response =
                    new StringBuilder("Vos demandes affectées :\n\n");

            for (DemandeCredit d : demandes) {

                response.append(detailsDemande(d))
                        .append("\n----------------\n");
            }

            return response.toString();
        }

        if (contient(question,
                "combien de demandes",
                "nombre de demandes")) {

            return "Vous avez "
                    + demandes.size()
                    + " demande(s) affectée(s).";
        }

        if (contient(question,
                "en analyse",
                "analyse")) {

            long nombre = demandes.stream()
                    .filter(d ->
                            d.getStatut() == StatutDemande.EN_ANALYSE)
                    .count();

            return "Vous avez "
                    + nombre
                    + " demande(s) actuellement en analyse.";
        }

        if (contient(question,
                "approuve",
                "approuvée",
                "approuvee")) {

            long nombre = demandes.stream()
                    .filter(d ->
                            d.getStatut() == StatutDemande.APPROUVEE)
                    .count();

            return "Vous avez "
                    + nombre
                    + " demande(s) approuvée(s).";
        }

        if (contient(question,
                "refuse",
                "refusée",
                "refusee")) {

            long nombre = demandes.stream()
                    .filter(d ->
                            d.getStatut() == StatutDemande.REFUSEE)
                    .count();

            return "Vous avez "
                    + nombre
                    + " demande(s) refusée(s).";
        }

        if (contient(question,
                "mon profil",
                "mes informations")) {

            return "Conseiller : "
                    + conseiller.getPrenom()
                    + " "
                    + conseiller.getNom()
                    + "\nMatricule : "
                    + conseiller.getMatricule()
                    + "\nSpécialité : "
                    + valeur(conseiller.getSpecialite())
                    + "\nDemandes en cours : "
                    + valeur(conseiller.getNombreDemandesEnCours())
                    + "\nLimite d'autorisation : "
                    + formatMontant(conseiller.getLimiteAutorisation());
        }

        return "Je peux vous aider avec vos demandes affectées, "
                + "leur statut, les demandes en analyse, "
                + "les demandes approuvées ou refusées "
                + "et vos informations de conseiller.";
    }

    // =========================================================
    // RESPONSABLE CREDIT
    // =========================================================

    private String traiterResponsable(
            String question,
            ResponsableCredit responsable) {

        List<DemandeCredit> demandes =
                demandeCreditRepository
                        .findByResponsableId(responsable.getId());

        if (contient(question,
                "mes demandes",
                "demandes affectees",
                "demandes affectées")) {

            if (demandes.isEmpty()) {
                return "Aucune demande ne vous est actuellement affectée.";
            }

            StringBuilder response =
                    new StringBuilder(
                            "Vos demandes affectées :\n\n");

            for (DemandeCredit d : demandes) {

                response.append(detailsDemande(d))
                        .append("\n----------------\n");
            }

            return response.toString();
        }

        if (contient(question,
                "combien de demandes",
                "nombre de demandes")) {

            return "Vous avez "
                    + demandes.size()
                    + " demande(s) affectée(s).";
        }

        if (contient(question,
                "en attente",
                "attente",
                "decision",
                "décision")) {

            long nombre = demandes.stream()
                    .filter(d ->
                            d.getStatut() == StatutDemande.EN_ATTENTE)
                    .count();

            return "Vous avez "
                    + nombre
                    + " demande(s) en attente de décision.";
        }

        if (contient(question,
                "approuve",
                "approuvée",
                "approuvee")) {

            long nombre = demandes.stream()
                    .filter(d ->
                            d.getStatut() == StatutDemande.APPROUVEE)
                    .count();

            return "Vous avez "
                    + nombre
                    + " demande(s) approuvée(s).";
        }

        if (contient(question,
                "contrat",
                "contrats")) {

            List<Contrat> contrats = contratsResponsable(responsable);

            if (contrats.isEmpty()) {
                return "Vous n'avez aucun contrat associé.";
            }

            StringBuilder response =
                    new StringBuilder("Vos contrats :\n\n");

            for (Contrat c : contrats) {

                response.append("Contrat #")
                        .append(c.getId())
                        .append("\nNuméro : ")
                        .append(valeur(c.getNumeroContrat()))
                        .append("\nMontant : ")
                        .append(formatMontant(c.getMontant()))
                        .append("\nMensualité : ")
                        .append(formatMontant(c.getMensualite()))
                        .append("\nStatut : ")
                        .append(c.getStatut())
                        .append("\n\n");
            }

            return response.toString();
        }

        if (contient(question,
                "mon profil",
                "mes informations")) {

            return "Responsable Crédit : "
                    + responsable.getPrenom()
                    + " "
                    + responsable.getNom()
                    + "\nMatricule : "
                    + responsable.getMatricule()
                    + "\nLimite d'autorisation : "
                    + formatMontant(
                    responsable.getLimiteAutorisation());
        }

        return "Je peux vous aider avec les demandes qui vous sont "
                + "affectées, les décisions, les contrats et "
                + "les informations de votre portefeuille.";
    }

    // =========================================================
    // ADMIN
    // =========================================================

    private String traiterAdmin(String question) {

        if (contient(question,
                "combien de demandes",
                "nombre de demandes",
                "total demandes")) {

            return "Nombre total de demandes : "
                    + demandeCreditRepository.nombreDemandes();
        }

        if (contient(question,
                "approuve",
                "approuvées",
                "approuvees",
                "credits acceptes",
                "crédits acceptés")) {

            return "Nombre de crédits approuvés : "
                    + demandeCreditRepository.nombreCreditsApprouves();
        }

        if (contient(question,
                "refuse",
                "refusées",
                "refusees")) {

            return "Nombre de crédits refusés : "
                    + demandeCreditRepository.nombreCreditsRefuses();
        }

        if (contient(question,
                "en analyse")) {

            return "Nombre de demandes en analyse : "
                    + demandeCreditRepository.nombreCreditsEnAnalyse();
        }

        if (contient(question,
                "montant total accorde",
                "montant total accordé",
                "total accorde",
                "total accordé")) {

            return "Montant total accordé : "
                    + formatMontant(
                    demandeCreditRepository
                            .montantTotalAccorde());
        }

        if (contient(question,
                "montant moyen",
                "moyenne")) {

            return "Montant moyen accordé : "
                    + formatMontant(
                    demandeCreditRepository
                            .montantMoyenAccordeDashboard());
        }

        if (contient(question,
                "aujourd'hui",
                "aujourdhui",
                "aujourd hui")) {

            return "Demandes reçues aujourd'hui : "
                    + demandeCreditRepository.demandesAujourdHui();
        }

        if (contient(question,
                "cette semaine")) {

            return "Demandes cette semaine : "
                    + demandeCreditRepository.demandesCetteSemaine();
        }

        if (contient(question,
                "ce mois",
                "ce mois-ci",
                "ce mois ci")) {

            return "Demandes ce mois : "
                    + demandeCreditRepository.demandesCeMois();
        }

        if (contient(question,
                "profil",
                "mes informations")) {

            return "Vous êtes connecté en tant qu'administrateur. "
                    + "Vous disposez d'une vue globale sur "
                    + "les demandes et les indicateurs de CREDITNOVA.";
        }

        return "Je peux vous fournir les indicateurs globaux "
                + "de CREDITNOVA :\n"
                + "• nombre de demandes\n"
                + "• crédits approuvés\n"
                + "• crédits refusés\n"
                + "• demandes en analyse\n"
                + "• montant total accordé\n"
                + "• montant moyen accordé\n"
                + "• activité du jour, de la semaine et du mois.";
    }

    // =========================================================
    // SECURITE / DONNEES
    // =========================================================

    private List<Contrat> contratsClient(Client client) {

        return contratRepository.findAll()
                .stream()
                .filter(c ->
                        c.getDemandeCredit() != null
                                && c.getDemandeCredit().getClient() != null
                                && c.getDemandeCredit()
                                .getClient()
                                .getId()
                                .equals(client.getId()))
                .toList();
    }

    private List<Contrat> contratsResponsable(
            ResponsableCredit responsable) {

        return contratRepository.findAll()
                .stream()
                .filter(c ->
                        c.getDemandeCredit() != null
                                && c.getDemandeCredit()
                                .getResponsable() != null
                                && c.getDemandeCredit()
                                .getResponsable()
                                .getId()
                                .equals(responsable.getId()))
                .toList();
    }

    // =========================================================
    // FORMATAGE
    // =========================================================

    private String detailsDemande(DemandeCredit d) {

        StringBuilder response =
                new StringBuilder();

        response.append("Demande #")
                .append(d.getId())
                .append("\n");

        response.append("Type : ")
                .append(valeur(d.getTypeCredit()))
                .append("\n");

        response.append("Montant demandé : ")
                .append(formatMontant(d.getMontant()))
                .append("\n");

        response.append("Durée : ")
                .append(valeur(d.getDuree()))
                .append(" mois\n");

        response.append("Statut : ")
                .append(d.getStatut())
                .append("\n");

        if (d.getMontantAccorde() != null) {
            response.append("Montant accordé : ")
                    .append(formatMontant(d.getMontantAccorde()))
                    .append("\n");
        }

        if (d.getTauxInteret() != null) {
            response.append("Taux d'intérêt : ")
                    .append(d.getTauxInteret())
                    .append("%\n");
        }

        if (d.getDateDemande() != null) {
            response.append("Date : ")
                    .append(d.getDateDemande())
                    .append("\n");
        }

        if (d.getMotifRefus() != null
                && !d.getMotifRefus().isBlank()) {

            response.append("Motif du refus : ")
                    .append(d.getMotifRefus())
                    .append("\n");
        }

        return response.toString();
    }

    private String normaliser(String texte) {

        return texte
                .toLowerCase(Locale.ROOT)
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("ë", "e")
                .replace("à", "a")
                .replace("â", "a")
                .replace("î", "i")
                .replace("ï", "i")
                .replace("ô", "o")
                .replace("ù", "u")
                .replace("û", "u")
                .replace("ü", "u")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean estSalutation(String question) {

        return contient(question,
                "bonjour",
                "salut",
                "hello",
                "bonsoir",
                "coucou");
    }

    private boolean contient(String question,
                             String... mots) {

        for (String mot : mots) {

            String normalise = normaliser(mot);

            if (question.contains(normalise)) {
                return true;
            }
        }

        return false;
    }

    private String valeur(Object valeur) {

        return valeur == null
                ? "Non renseigné"
                : valeur.toString();
    }

    private String formatMontant(Double montant) {

        if (montant == null) {
            return "Non renseigné";
        }

        return String.format(Locale.US,
                "%.2f TND",
                montant);
    }
}